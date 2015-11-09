package eu.profinit.opendata.test;

import eu.profinit.opendata.model.Entity;
import junit.framework.TestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Created by DM on 9. 11. 2015.
 */
public class PersistenceTest extends TestCase {

    private SessionFactory sessionFactory;
    private Session session;

    public PersistenceTest() {
        // A SessionFactory is set up once for an application!
        final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .configure() // configures settings from hibernate.cfg.xml
                .build();
        try {
            sessionFactory = new MetadataSources( registry ).buildMetadata().buildSessionFactory();
        }
        catch (Exception e) {
            // The registry would be destroyed by the SessionFactory, but we had trouble building the SessionFactory
            // so destroy it manually.
            StandardServiceRegistryBuilder.destroy( registry );
        }
    }

    @Override
    public void setUp() throws Exception {
        session = sessionFactory.openSession();
        assertTrue(session.isConnected());
        assertTrue(session.isOpen());
    }

    public void testDatabaseConnection() throws Exception {
        session.byId(Entity.class);
    }

    @Override
    public void tearDown() throws Exception {
        session.close();
        session = null;
    }
}
