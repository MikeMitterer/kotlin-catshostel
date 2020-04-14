package at.mikemitterer.catshostel.persistance

import at.mikemitterer.catshostel.di.appModule
import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.tdd.TestUtils.predictName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeGreaterThan

import org.junit.*
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import kotlin.test.assertTrue

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
            stopKoin()
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

        dao.all.count().shouldBeGreaterThan(0)
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