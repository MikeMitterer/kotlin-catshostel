package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.module
import at.mikemitterer.tdd.TestUtils
import at.mikemitterer.tdd.TestUtils.predictName
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.netty.handler.codec.http.HttpHeaders.addHeader
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeTrue
import org.joda.time.LocalDateTime
import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import kotlin.math.roundToInt

/**
 * @since 08.04.20, 10:45
 */
class CatsTest {

    @Before
    fun setUp() {
    }

    @Test
    fun testAssertion() {
        true.shouldBeTrue()
    }

    @Test
    fun testCreateCat() {
        withTestApplication({ module(testing = true) }) {
            val call = createCat(predictName("Pepples"), 22)

            call.response.status().`should be`(HttpStatusCode.Created)
        }
    }
}

fun TestApplicationEngine.createCat(name: String, age: Int): TestApplicationCall {
    return handleRequest(HttpMethod.Post, "/cats") {
        addHeader(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
        setBody(listOf(
                "name" to name,
                "age" to age.toString()
        ).formUrlEncode())
    }
}