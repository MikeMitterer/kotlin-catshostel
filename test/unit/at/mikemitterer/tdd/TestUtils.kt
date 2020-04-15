package at.mikemitterer.tdd

import org.joda.time.LocalDateTime
import kotlin.math.roundToInt

/**
 *
 *
 * @since   09.04.20, 12:24
 */
object TestUtils {
    fun predictName(baseName: String): String {
        val now = LocalDateTime.now().toString("yyyy.MM.dd, HH:mm:ss")
        val rnd = (Math.random() * 1000).roundToInt()

        return "${baseName}-${now}-${rnd}"
    }
}