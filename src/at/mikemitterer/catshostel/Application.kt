package at.mikemitterer.catshostel

import at.mikemitterer.catshostel.auth.createJWT
import at.mikemitterer.catshostel.auth.getPublicKey
import at.mikemitterer.catshostel.auth.stripPEMMarker
import at.mikemitterer.catshostel.di.appModule
import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persistence.CatDAO
import at.mikemitterer.catshostel.routes.basicRouter
import at.mikemitterer.catshostel.routes.catRouter
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.google.gson.Gson
import freemarker.cache.ClassTemplateLoader
import io.jsonwebtoken.Claims
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging
import io.ktor.features.*
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.gson.gson
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.*
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.locations.locations
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import org.apache.commons.lang3.exception.ExceptionUtils
import org.joda.time.DateTime
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

// fun main(args: Array<String>) {
//     embeddedServer(Netty, commandLineEnvironment(args)).start()
// }

// fun main(args: Array<String>) {
//     // For AWS ElasticBeanstalk, use port 5000
//     val server = embeddedServer (Netty, 8080) {
// //    val server = embeddedServer (Netty, 5000) {
//         routing {
//             get("/") {
//                 call.respondText("Greetings, Earthlings! Kotlin, Ktor and Gradle salute you now.", ContentType.Text.Html)
//             }
//         }
//     }
//     server.start(wait = true)
// }

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {
    val keycloakAddress = "http://localhost:9000"

    val keycloakProvider = OAuthServerSettings.OAuth2ServerSettings(
            name = "keycloak",
            authorizeUrl = "$keycloakAddress/auth/realms/ktor/protocol/openid-connect/auth",
            accessTokenUrl = "$keycloakAddress/auth/realms/ktor/protocol/openid-connect/token",
            clientId = "ktorClient",
            clientSecret = "123456",
            accessTokenRequiresBasicAuth = false,
            requestMethod = HttpMethod.Post, // must POST to token endpoint
            defaultScopes = listOf("roles")
    )
    val keycloakOAuth = "keycloakOAuth"

    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(AutoHeadResponse)

    install(CallLogging) {
        level = Level.INFO
        // filter { call -> call.request.path().startsWith("/protected") }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(io.ktor.websocket.WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    // This enables the use of sessions to keep information between requests/refreshes of the browser.
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    // This adds an interceptor that will create a specific session in each request if no session is available already.
    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<ChatSession>() == null) {
            call.sessions.set(ChatSession(generateNonce()))
        }
    }

    val publicKey = getPublicKey(javaClass.getResource("/rsakeys/jwt.pub.pem"))
    val jwtIssuer = "mmit"
    val jwtAudience = "account"
    val jwtRealm = jwtIssuer

    val algorithm = Algorithm.RSA256(publicKey, null)
    fun makeJwtVerifier(issuer: String, audience: String): JWTVerifier = JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate {
                if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null
            }
        }

        jwt("jwtAuth") {
            realm = jwtRealm
            verifier(makeJwtVerifier(jwtIssuer, jwtAudience))
            validate { credential ->
                val hasAudience = credential.payload.audience.contains(jwtAudience)
                val principal = JWTPrincipal(credential.payload)

                if (hasAudience) principal else null
            }
        }
    }

    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }

    install(Koin) {
        // Use SLF4J Koin Logger at Level.INFO
        slf4jLogger()

        // declare used modules
        modules(appModule)
    }

    // Weitere Infos: https://ktor.io/servers/features/status-pages.html#exceptions
    install(StatusPages) {
        exception<Throwable> { cause ->
            val gson = Gson()
            val message = mapOf<String, Any>(
                    "message" to (cause.message ?: "no message!"),
                    "stacktrace" to ExceptionUtils.getRootCauseStackTrace(cause).map { it.replace("\t", " - ") }
            )
            call.respond(HttpStatusCode.InternalServerError, gson.toJson(message)) // status.toJson()
        }
    }

    val client = HttpClient(Apache /* CIO */) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    val server = ChatServer()

    routing {
        get("/") {
            application.log.debug("Servus vom Server")
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3, 4, 5, 6))), ""))
        }

        post("/login") {
            with(call) {
                val params = receiveParameters()
                val username = requireNotNull(params["username"])
                val password = requireNotNull(params["password"])

                val credentials = UserPasswordCredential(username, password)
                call.respond(HttpStatusCode.OK, generateJWT(username))
            }
        }

        // Static feature. Try to access `/static/ktor_logo.svg`
        static("/static") {
            resources("static")
        }

        webSocket("/ws") {
            // First of all we get the session.
            val session = call.sessions.get<ChatSession>()

            // We check that we actually have a session. We should always have one,
            // since we have defined an interceptor before to set one.
            if (session == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session"))
                return@webSocket
            }

            // We notify that a member joined by calling the server handler [memberJoin]
            // This allows to associate the session id to a specific WebSocket connection.
            server.memberJoin(session.id, this)

            try {
                // We starts receiving messages (frames).
                // Since this is a coroutine. This coroutine is suspended until receiving frames.
                // Once the connection is closed, this consumeEach will finish and the code will continue.
                incoming.consumeEach { frame ->
                    // Frames can be [Text], [Binary], [Ping], [Pong], [Close].
                    // We are only interested in textual messages, so we filter it.
                    if (frame is Frame.Text) {
                        // Now it is time to process the text sent from the user.
                        // At this point we have context about this connection, the session, the text and the server.
                        // So we have everything we need.
                        server.receivedMessage(session.id, frame.readText())
                    }
                }
            } finally {
                // Either if there was an error, of it the connection was closed gracefully.
                // We notify the server that the member left.
                server.memberLeft(session.id, this)
            }
        }

        authenticate("myBasicAuth", optional = true) {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
            }
        }

        authenticate( "jwtAuth", optional = false) {
            get("/protected/route/jwt") {
                val principal = call.principal<JWTPrincipal>()!!
                val name = principal.payload.claims
                        .getValue("preferred_username")
                        .asString()

                call.respondText("Hello '${name.toString()}'! (JWT)")
            }
        }

        get("/json/gson") {
            call.respond(mapOf("hello" to "world"))

            val gson = Gson()
            val cat = Cat("Pepples27", 88)
            server.message("Server", gson.toJson(cat))
        }

        catRouter(get<CatDAO>())
        basicRouter()
    }
}

data class IndexData(val items: List<Int>)

private fun <T : Any> ApplicationCall.redirectUrl(t: T, secure: Boolean = true): String {
    val hostPort = request.host() + request.port().let { port -> if (port == 80) "" else ":$port" }
    val protocol = when {
        secure -> "https"
        else -> "http"
    }
    
    return "$protocol://$hostPort${application.locations.href(t)}"
}

private fun Application.generateJWT(username: String): String {
    val privateKey = javaClass.getResource("/rsakeys/jwt.pkcs8.pem").readText().stripPEMMarker()
    val now = DateTime.now()

    val jwt = createJWT(mutableMapOf(
            Claims.EXPIRATION to now.plusMinutes(5).toDate(),
            Claims.ISSUED_AT to now.toDate(),
            Claims.ISSUER to "mmit",
            Claims.AUDIENCE to "account",
            Claims.SUBJECT to "Subject",
            "typ" to "Bearer",
            "realm_access" to
                    mapOf("roles" to listOf<String>(
                            "offline_access",
                            "uma_authorization",
                            "vip"
                    ))
            ,
            "resource_access" to mapOf(
                    "vue-test-app" to
                            mapOf("roles" to listOf<String>(
                                    "device"
                            ))
                    ,
                    "account" to
                            mapOf("roles" to listOf<String>(
                                    "manage-account",
                                    "manage-account-links",
                                    "view-profile"
                            ))
            ),
            "scope" to "profile email",
            "email_verified" to true,
            "preferred_username" to username,
            "given_name" to username.capitalize(),
            "family_name" to "Mitterer",
            "email" to "${username}@mikemitterer.at"
    ), privateKey)

    return jwt
}

