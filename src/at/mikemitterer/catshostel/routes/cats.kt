package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.core.Koin

/**
 * Stellt alle Routes für die Katzen zur Verfügung
 *
 * @since   08.04.20, 09:45
 */

fun Route.catRouter(dao: CatDAO) {
    route("/cats") {
        post {
            with(call) {
                val params = receiveParameters()
                val name = requireNotNull(params["name"])
                val age = requireNotNull(params["age"]).toInt()

                dao.insert(Cat(name, age))
                call.response.status(HttpStatusCode.Created)
            }
        }
    }
}
