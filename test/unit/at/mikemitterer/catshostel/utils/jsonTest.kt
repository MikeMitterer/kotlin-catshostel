package at.mikemitterer.catshostel.utils


import org.junit.Test
import org.slf4j.LoggerFactory

/**
 *
 *
 * @since   14.04.20, 08:39
 */
class JsonTest {
    private val logger = LoggerFactory.getLogger(JsonTest::class.java.name)

    @Test
    fun testJsonArray() {
        val list = arrayOf("Hallo", "Dies", "ist", "ein", "Test")
        logger.info(list.asJson())
        // print(list.asJson())
    }
}