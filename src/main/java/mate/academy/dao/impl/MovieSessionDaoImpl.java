package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.persist(movieSession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't insert movie session to DB "
                    + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return movieSession;
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return Optional.ofNullable(session.get(MovieSession.class, id));
        } catch (Exception e) {
            throw new DataProcessingException("Can't get movie session by id: " + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        LocalDateTime timeFrom = date.atStartOfDay();
        LocalDateTime timeTo = date.plusDays(1).atStartOfDay();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from MovieSession ms where ms.id = :id "
                            + "and ms.showTime between :timeFrom  and :timeTo", MovieSession.class)
                    .setParameter("id", movieId)
                    .setParameter("timeFrom", timeFrom)
                    .setParameter("timeTo", timeTo)
                    .getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't find available sessions by movie id: "
                    + movieId + " and date: " + date, e);
        }
    }
}
