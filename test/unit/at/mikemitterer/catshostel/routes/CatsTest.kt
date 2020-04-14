package at.mikemitterer.catshostel.routes

import at.mikemitterer.catshostel.di.appModule
import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.module
import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.catshostel.utils.JsonTest
import at.mikemitterer.catshostel.utils.asJson
import at.mikemitterer.tdd.TestUtils
import at.mikemitterer.tdd.TestUtils.predictName
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.server.testing.TestApplicationCall
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import io.netty.handler.codec.http.HttpHeaders.addHeader
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.*
import org.joda.time.LocalDateTime
import org.junit.*

import org.junit.Assert.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import org.slf4j.LoggerFactory
import kotlin.math.roundToInt

/**
 * @since 08.04.20, 10:45
 */
@ExperimentalCoroutinesApi
class CatsTest: KoinTest {
    private val logger = LoggerFactory.getLogger(CatsTest::class.java.name)

    private lateinit var baseCat: Cat

    companion object {
        // lateinit var sqlSessionFactory: SqlSessionFactory

        // Wird automatisch aufgerufen
        // Es kann auch mehrere Init-Funktionen geben!
        init {}

        @BeforeClass
        @JvmStatic fun setup() {
            // once per run
            // startKoin {
            //     modules(appModule)
            // }
        }

        @AfterClass
        @JvmStatic fun teardown() {
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

        dao.numberOfCats.shouldBe(0)
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
   fun testGetAllCats()  {
       withTestApplication({module(testing = true)}) {
           val call = handleRequest(HttpMethod.Get, "/cats")
           call.response.content?.asJson().shouldBeEqualTo("[]".asJson())
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