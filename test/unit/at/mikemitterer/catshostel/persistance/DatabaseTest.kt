package at.mikemitterer.catshostel.persistance

import at.mikemitterer.catshostel.di.appModule
import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.catshostel.persitance.SessionFactory
import at.mikemitterer.tdd.TestUtils.predictName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import org.joda.time.LocalDateTime

import org.joda.time.format.DateTimeFormat
import org.joda.time.format.ISODateTimeFormat
import org.junit.*
import org.koin.core.context.KoinContextHandler.get
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.get
import java.io.InputStream
import java.nio.charset.Charset
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.assertTrue
import java.util.Locale;
import kotlin.math.roundToInt

@ExperimentalCoroutinesApi
class DatabaseTest: KoinTest {
    companion object {
        // lateinit var sqlSessionFactory: SqlSessionFactory

        // Wird automatisch aufgerufen
        // Es kann auch mehrere Init-Funktionen geben!
        init {}
        
        @BeforeClass
        @JvmStatic fun setup() {
            // once per run
            startKoin {
                modules(appModule)
            }
        }

        @AfterClass
        @JvmStatic fun teardown() {
            // clean up after this class, leave nothing dirty behind
        }
    }

    @Before
    fun setupTest() {
    }

    @After
    fun cleanUp() {
    }

    @Test
    fun testInsert() = runBlockingTest {
        val dao = get<CatDAO>()
        val number = dao.numberOfCats

        val cat = addNewCat(dao)

        val newNumber = dao.numberOfCats
        newNumber.shouldBeGreaterThan(number)
    }

    @Test
    fun testSelectAll() = runBlockingTest {
        val dao = get<CatDAO>()

        val cat = Cat(predictName("Pepples"), 99)
        dao.insert(cat)

        dao.cats.count().shouldBeGreaterThan(0)
    }

    @Test
    fun testDelete() = runBlockingTest {
        val dao = get<CatDAO>()

        val cat = addNewCat(dao)
        val numberBeforeDelete = dao.numberOfCats

        dao.delete(cat)
        assertTrue(dao.numberOfCats < numberBeforeDelete)
    }

    @Test
    fun testUpdate() = runBlockingTest {
        val dao = get<CatDAO>()
        val cat = addNewCat(dao)

        val oldAge = cat.age

        cat.age = cat.age + 1
        dao.update(cat)

        val updatedCat = dao.cat(cat.ID)
        updatedCat.age.shouldBeEqualTo(oldAge + 1)
    }
}

private suspend fun addNewCat(dao: CatDAO): Cat {
    val name = predictName("Pepples")
    val cat = Cat(name, 99)
    dao.insert(cat)
    return cat
}