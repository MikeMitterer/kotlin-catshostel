package at.mikemitterer.catshostel.di

import at.mikemitterer.catshostel.persistence.CatDAO
import at.mikemitterer.catshostel.persistence.CatDAOImpl
import at.mikemitterer.catshostel.persistence.SessionFactory
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
