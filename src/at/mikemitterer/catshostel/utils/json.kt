package at.mikemitterer.catshostel.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

/**
 *
 *
 * @since   14.04.20, 08:36
 */
fun String.asJson(): String? {
    val gson = Gson()
    return gson.toJson(this)
}

fun Array<String>.asJson(): String {
    val gson = GsonBuilder().setPrettyPrinting().create()
    val mapType = object : TypeToken<Array<String>>() {}.type

    return gson.toJson(this)
}