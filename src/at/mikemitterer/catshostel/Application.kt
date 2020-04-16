package at.mikemitterer.catshostel

import at.mikemitterer.catshostel.di.appModule
import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.catshostel.routes.basicRouter
import at.mikemitterer.catshostel.routes.catRouter
import at.mikemitterer.webapp.events.RestStatus
import com.google.common.base.Optional
import com.google.gson.Gson
import freemarker.cache.ClassTemplateLoader
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
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
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.generateNonce
import io.ktor.websocket.webSocket
import kotlinx.coroutines.channels.consumeEach
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.logger.slf4jLogger
import org.slf4j.event.Level
import java.time.Duration

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {
    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }

    install(AutoHeadResponse)

    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
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

    install(Authentication) {
        basic("myBasicAuth") {
            realm = "Ktor Server"
            validate { if (it.name == "test" && it.password == "password") UserIdPrincipal(it.name) else null }
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
            val status = RestStatus(RestStatus.Data(
                    HttpStatusCode.InternalServerError.value,
                    cause.message, cause.message,
                    Optional.of(cause),
                    Optional.absent()
            ))
            call.respond(HttpStatusCode.InternalServerError, status.toJson())
        }
    }

    val client = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.HEADERS
        }
    }

    val server = ChatServer()

    routing {
        get("/") {
            application.log.debug("Servus vom Server")
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)

            // val dao = this@routing.get<CatDAO>()
        }

        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3, 4, 5, 6))), ""))
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

        authenticate("myBasicAuth") {
            get("/protected/route/basic") {
                val principal = call.principal<UserIdPrincipal>()!!
                call.respondText("Hello ${principal.name}")
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

