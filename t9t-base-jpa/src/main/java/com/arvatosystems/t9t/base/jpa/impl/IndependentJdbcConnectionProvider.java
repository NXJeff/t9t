package com.arvatosystems.t9t.base.jpa.impl;

import java.io.IOException;
import java.sql.Connection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaJdbcConnectionProvider;
import com.arvatosystems.t9t.base.services.IIndependentJdbcConnectionProvider;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;


// did not work for the scheduler
@Fallback
@Dependent
@Deprecated
public class IndependentJdbcConnectionProvider implements IIndependentJdbcConnectionProvider {

    private final IJpaJdbcConnectionProvider connProvider = Jdp.getRequired(IJpaJdbcConnectionProvider.class);
    private final EntityManagerFactory emf = Jdp.getRequired(EntityManagerFactory.class);
    private EntityManager em = null;   // sponsors the connection

    @Override
    public Connection getJDBCConnection() {
        if (em != null)
            throw new RuntimeException("Sequence error: EntityManager already present");
        em = emf.createEntityManager();
        return connProvider.get(em);
    }

    @Override
    public void close() throws IOException {
        if (em != null) {
            em.clear();
            em.close();
            em = null;
        }
    }

}