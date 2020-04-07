package at.mikemitterer

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.CatDAO
import org.apache.ibatis.io.Resources
import org.apache.ibatis.session.SqlSessionFactory
import org.apache.ibatis.session.SqlSessionFactoryBuilder
import java.io.InputStream
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DatabaseTest {
    private lateinit var inputStream: InputStream
    private lateinit var sqlSessionFactory: SqlSessionFactory

    @BeforeTest
    fun setup() {
        var resource = "mybatis-config.xml";
        inputStream = Resources.getResourceAsStream(resource);

        val reader = Resources.getResourceAsReader(resource)
//        Class.forName(JDBC_DRIVER)
//        val script = javaClass.getResourceAsStream("/CreateSimpleDB.sql")
//        DriverManager.getConnection(JDBC_URL, "sa", "").use { connection ->
//            val sr = ScriptRunner(connection)
//            sr.setLogWriter(null)
//            sr.runScript(InputStreamReader(script))
//        }
//
//        val ds = UnpooledDataSource(JDBC_DRIVER, JDBC_URL, "sa", "")
//        val environment = Environment("test", JdbcTransactionFactory(), ds)
//        val config = Configuration(environment)
//        config.addMapper(Example04Mapper::class.java)
          sqlSessionFactory = SqlSessionFactoryBuilder().build(reader,"development")
    }

    @Test
    fun testInsert() {
        val dao = CatDAO(sqlSessionFactory)
        val number = dao.numberOfCats

        val cat = Cat("Pepples-${number + 1}", 99)
        dao.insert(cat)

        val newNumber = dao.numberOfCats
        assertTrue(newNumber > number)
    }

    @Test
    fun testSelectAll() {
        val dao = CatDAO(sqlSessionFactory)
        val cats = dao.cats

        assertTrue(cats.count() > 0)
    }

    @Test
    fun testDelete() {
        val dao = CatDAO(sqlSessionFactory)
        val cats = dao.cats
        val numberBeforeDelete = dao.numberOfCats

        dao.delete(cats.last())
        assertTrue(dao.numberOfCats < numberBeforeDelete)
    }

    @Test
    fun testUpdate() {
        val dao = CatDAO(sqlSessionFactory)
        val cats = dao.cats
        val numberBeforeDelete = dao.numberOfCats

        val cat = cats.last()
        cat.age = cat.age + 1

        dao.update(cat)
        // assertTrue(dao.numberOfCats < numberBeforeDelete)
    }

}
