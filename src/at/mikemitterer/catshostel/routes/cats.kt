package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.koin.core.Koin
import org.slf4j.LoggerFactory

/**
 * Stellt alle Routes für die Katzen zur Verfügung
 *
 * @since   08.04.20, 09:45
 */

fun Route.catRouter(dao: CatDAO) {
    val logger = LoggerFactory.getLogger("Route.catRouter")

    route("/cats") {
        post {
            with(call) {
                val params = receiveParameters()
                val name = requireNotNull(params["name"])
                val age = requireNotNull(params["age"]).toInt()

                val cat = Cat(name, age)
                dao.insert(cat)

                call.respond(HttpStatusCode.Created,cat)
            }
        }

        get {
            call.respond(dao.all)
        }

        delete {
            dao.deleteAll()
            call.response.status(HttpStatusCode.NoContent)

            logger.info("All cats deleted!")
        }
    }
}
