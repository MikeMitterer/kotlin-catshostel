package at.mikemitterer.catshostel.persitance.mapper

import at.mikemitterer.catshostel.model.Cat
import org.apache.ibatis.annotations.*

interface CatsMapper {
    @get:Select("SELECT count(*) FROM cats")
    @get:Options(useCache = true)
    val numberOfCats: Long

    @get:Select("SELECT * from cats")
    @get:Options(useCache = true)
    val cats: List<Cat>

    // Set keyProperty as the Java variable name and keyColumn as the column name in the database.
    @Options(flushCache = Options.FlushCachePolicy.TRUE, useGeneratedKeys = true, keyProperty = "cat.ID", keyColumn = "id")
    @Insert("INSERT INTO cats ( name, age ) VALUES (#{cat.name}, #{cat.age})")
    fun insert(@Param("cat") cat: Cat?)

    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    @Update("UPDATE cats SET name=#{cat.name},age=#{cat.age} WHERE id = #{cat.ID}")
    fun update(@Param("cat") cat: Cat?)

    @Options(flushCache = Options.FlushCachePolicy.TRUE)
    @Delete("DELETE FROM cats WHERE id = #{cat.ID}")
    fun delete(@Param("cat") cat: Cat?)
}