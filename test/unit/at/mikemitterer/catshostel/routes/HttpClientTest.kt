package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.WsClientApp.main
import at.mikemitterer.catshostel.main
import at.mikemitterer.tdd.RestServer
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.junit.*
import org.koin.test.KoinTest
import org.slf4j.LoggerFactory

/**
 *
 *
 * @since   16.04.20, 10:17
 */
@ExperimentalCoroutinesApi
class HttpClientTest : KoinTest {
    private val logger = LoggerFactory.getLogger(HttpClientTest::class.java.name)

    companion object {
        lateinit var server: NettyApplicationEngine

        // Wird automatisch aufgerufen
        // Es kann auch mehrere Init-Funktionen geben!
        init {
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            server = RestServer.start()
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            // clean up after this class, leave nothing dirty behind
            RestServer.stop(server)
        }
    }

    @Before
    fun setupTest() = runBlockingTest {
    }

    @After
    fun cleanUp() {

    }

    @Test
    fun testMitDefaultHttpClient() {
        val httpget = HttpGet("http://0.0.0.0:8080/json/gson")
        httpget.setHeader("Accept", "application/json")
        httpget.setHeader("Content-Type", "application/json")

        val httpclient: HttpClient = HttpClients.createDefault()
        val response = httpclient.execute(httpget)

        Assert.assertEquals(200, response.statusLine.statusCode.toLong())

        val responseString = EntityUtils.toString(response.entity)
        logger.info(responseString)
    }

    @Test
    fun testMitDefaultHttpClientWrongURL() {
        val httpget = HttpGet("http://0.0.0.0:8080/json/gson1")

        val httpclient: HttpClient = HttpClients.createDefault()
        val response = httpclient.execute(httpget)

        Assert.assertEquals(404, response.statusLine.statusCode.toLong())
    }
}
