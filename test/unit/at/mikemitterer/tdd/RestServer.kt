package at.mikemitterer.tdd

import at.mikemitterer.catshostel.main
import at.mikemitterer.catshostel.routes.HttpClientTest
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

/**
 *
 *
 * @since   16.04.20, 11:24
 */
object RestServer {
    private val servers = arrayOf<NettyApplicationEngine>()

    fun start(): NettyApplicationEngine {
        val env = applicationEngineEnvironment {
            module {
                main(true)
            }
            // Private API
            // connector {
            //    host = "127.0.0.1"
            //    port = 9090
            // }

            // Public API
            connector {
                host = "0.0.0.0"
                port = 8080
            }
        }
        return embeddedServer(Netty, env).start(false)
    }

    fun stop(server: NettyApplicationEngine) {
        server.stop(1000, 10000)
    }
}