package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.module
import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.catshostel.utils.asJson
import at.mikemitterer.tdd.TestUtils.predictName
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.junit.*
import org.koin.test.KoinTest
import org.koin.test.get
import org.slf4j.LoggerFactory
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.ArrayList


/**
 * @since 08.04.20, 10:45
 */
@ExperimentalCoroutinesApi
class CatsTest : KoinTest {
    private val logger = LoggerFactory.getLogger(CatsTest::class.java.name)

    private lateinit var baseCat: Cat

    companion object {
        // lateinit var sqlSessionFactory: SqlSessionFactory

        // Wird automatisch aufgerufen
        // Es kann auch mehrere Init-Funktionen geben!
        init {
        }

        @BeforeClass
        @JvmStatic
        fun setup() {
            // once per run
            // startKoin {
            //     modules(appModule)
            // }
        }

        @AfterClass
        @JvmStatic
        fun teardown() {
            // clean up after this class, leave nothing dirty behind
            // stopKoin()
        }
    }

    @Before
    fun setupTest() = runBlockingTest {
        // val dao = inject<CatDAO>()
        // dao.value.deleteAll()
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Delete, "/cats")

            val call = createCat("Streuner", 33)

            call.response.status().`should be`(HttpStatusCode.Created)
            call.response.content.shouldNotBeNull()

            baseCat = Cat.fromJson(call.response.content!!)
        }
    }

    @After
    fun cleanUp() {

    }

    @Test
    @Ignore
    fun testInsert() = runBlockingTest {
        val dao = get<CatDAO>()
        val number = dao.numberOfCats

        dao.numberOfCats.shouldBe(1)
        val name = predictName("Pepples")
        val cat = Cat(name, 99)
        dao.insert(cat)
        dao.numberOfCats.shouldBe(0)
    }

    @Test
    fun testCreateCat() = runBlockingTest {
        withTestApplication({ module(testing = true) }) {
            val call = createCat(predictName("Pepples"), 22)

            call.response.status().`should be`(HttpStatusCode.Created)
            call.response.content.shouldNotBeNull()

            val cat = Cat.fromJson(call.response.content!!)
            logger.info("ID: ${cat.ID}, Name: ${cat.name}")
        }
    }

    @Test
    fun testGetAllCats() = runBlockingTest {
        withTestApplication({ module(testing = true) }) {
            val call = handleRequest(HttpMethod.Get, "/cats")
            val gson = Gson()

            val cats = gson.fromJson(call.response.content, Array<Cat>::class.java)
            cats.size.shouldBeEqualTo(1)
        }
    }

    @Test
    fun testGetCatByID() = runBlockingTest {
        withTestApplication({ module(testing = true) }) {
            val uri = "/cats/${this@CatsTest.baseCat.ID}"
            val call = handleRequest(HttpMethod.Get, uri)

            val cat = Cat.fromJson(call.response.content!!)
            cat.name.shouldBeEqualTo("Streuner")
        }
    }

    @Test
    fun testUpdateCat() {
        withTestApplication({ module(testing = true) }) {
            val call = handleRequest(HttpMethod.Put, "/cats") {
                val gson = Gson()

                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())

                baseCat.age = 33
                baseCat.name = "Streußöner" // .encodeURLParameter(spaceToPlus = true)
                val json = gson.toJson(baseCat)
                // setBody(json.encodeURLParameter(spaceToPlus = true))
                setBody(json)
            }

            call.response.status().`should be`(HttpStatusCode.NoContent)
        }
    }

    @Test
    fun testDeleteCat() {
        withTestApplication({ module(testing = true) }) {
            val callCreate = createCat("Streuner.to.delete", 99)

            callCreate.response.status().`should be`(HttpStatusCode.Created)
            val catToDelete = Cat.fromJson(callCreate.response.content!!)

            val uri = "/cats/${catToDelete.ID}"
            val call = handleRequest(HttpMethod.Delete, uri)

            call.response.status().`should be`(HttpStatusCode.NoContent)
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