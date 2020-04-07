package at.mikemitterer.catshostel.persitance

import at.mikemitterer.catshostel.model.Cat
import at.mikemitterer.catshostel.persitance.mapper.CatsMapper
import org.apache.ibatis.session.SqlSessionFactory

class CatDAO(
        private val sessionFactory: SqlSessionFactory) : CatsMapper {
    
    override fun insert(cat: Cat?) {
        var session = sessionFactory.openSession()
        try {
            session.getMapper(CatsMapper::class.java).insert(cat)
            session.commit()
        } finally {
            session.close()
        }
    }

    override val cats: List<Cat>
        get() {
            sessionFactory.openSession().use { session ->
                return session.getMapper(CatsMapper::class.java).cats
            }
    }

    override val numberOfCats: Long
        get() {
        var session = sessionFactory.openSession()
        try {
            return session.getMapper(CatsMapper::class.java).numberOfCats
        } finally {
            session.close()
        }
    }



    override fun update(cat: Cat?) {
        var session = sessionFactory.openSession()
        try {
            session.getMapper(CatsMapper::class.java).update(cat)
            session.commit()
        } finally {
            session.close()
        }
    }

    override fun delete(cat: Cat?) {
        var session = sessionFactory.openSession()
        try {
            session.getMapper(CatsMapper::class.java).delete(cat)
            session.commit()
        } finally {
            session.close()
        }
    }
}