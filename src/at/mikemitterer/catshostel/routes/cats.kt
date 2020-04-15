package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import com.google.gson.Gson
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
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
                // FormUrlEncoded verwendet receiveParameters für die Params
                // Anders als GET-Request mit Parametern
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

        get("/{id}") {
            with(call) {
                // Get-Request greift auf die Parameter direkt zu
                val id = requireNotNull(parameters["id"])

                try {
                    val cat = dao.catByID(id.toInt())
                    respond(cat)
                } catch (e: IllegalArgumentException) {
                    respond(HttpStatusCode.NotFound, e.message.toString())
                }
            }

        }

        put {
            with(call) {
                val cat = receive<Cat>()
                val gson = Gson()

                // val params1 = receiveParameters()
                logger.info("Params: ${gson.toJson(cat)}")
                dao.update(cat)
                call.respond(HttpStatusCode.NoContent)
            }
        }

        delete ("/{id}") {
            with(call) {
                // Get-Request greift auf die Parameter direkt zu
                val id = requireNotNull(parameters["id"])

                try {
                    val cat = dao.delete(id.toInt())
                    call.respond(HttpStatusCode.NoContent)
                } catch (e: IllegalArgumentException) {
                    respond(HttpStatusCode.NotFound, e.message.toString())
                }
            }

        }

        delete {
            dao.deleteAll()
            call.response.status(HttpStatusCode.NoContent)

            logger.info("All cats deleted!")
        }
    }
}
