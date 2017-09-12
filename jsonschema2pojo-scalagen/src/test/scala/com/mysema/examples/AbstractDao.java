package com.mysema.examples;

import javax.annotation.Nullable;

import com.mysema.query.types.EntityPath;
import com.mysema.query.types.OrderSpecifier;
import com.mysema.query.types.Predicate;

public abstract class AbstractDao<T> implements Dao<T, Long> {
    
    private HibernateSessionManager sessionManager;

    protected JPQLQuery query() {
        return new HibernateQuery(getSession());
    }

    protected Session getSession() {
        return sessionManager.getSession();
    }

    protected HibernateSessionManager getSessionManager() {
        return sessionManager;
    }

    protected <K> GridDataSource createGridDataSource(final EntityPath<K> path,
            final OrderSpecifier<?> order, final boolean caseSensitive, final Predicate filters) {
        return new JPQLGridDataSource<K>(getSessionManager(), path, order, caseSensitive, filters);
    }

}

interface Dao<Entity, Id> {

    @Nullable
    Entity getById( Id id );

}
class HibernateQuery implements JPQLQuery {
    public HibernateQuery(Session session) {
        
    }
}

interface HibernateSessionManager {
    Session getSession(); 
}

interface Session { }

interface JPQLQuery { }

interface GridDataSource {}

class JPQLGridDataSource<T> implements GridDataSource {

    public JPQLGridDataSource(HibernateSessionManager sessionManager,
            EntityPath<?> path, OrderSpecifier<?> order, boolean caseSensitive,
            Predicate filters) {
    }
    
}