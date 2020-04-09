package at.mikemitterer.catshostel.di

import at.mikemitterer.catshostel.persitance.CatDAO
import at.mikemitterer.catshostel.persitance.CatDAOImpl
import at.mikemitterer.catshostel.persitance.SessionFactory
import org.apache.ibatis.session.SqlSessionFactory
import org.koin.dsl.module

/**
 * DI - BASIS
 *
 * @since   09.04.20, 10:03
 */

val appModule = module(createdAtStart = true) {
    single<CatDAO> {
        CatDAOImpl(SessionFactory.sqlSessionFactory)
    }
}
