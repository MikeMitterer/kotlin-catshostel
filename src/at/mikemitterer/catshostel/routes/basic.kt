package at.mikemitterer.catshostel.routes

import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Basic-tests für den Rest-Server.
 *
 * Das ist das equivalent zu "addBasicBinding" in SparkMobiAdApplication
 *
 * @since   16.04.20, 09:45
 */

fun Route.basicRouter() {
    val logger = LoggerFactory.getLogger("Route.basicRouter")

    route("/exception") {

        get {
            throw IOException("REST-Test")
        }
    }
}
