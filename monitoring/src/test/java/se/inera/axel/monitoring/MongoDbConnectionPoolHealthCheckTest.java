package se.inera.axel.monitoring;

import com.mongodb.DBPort;
import com.mongodb.DBPortPool;
import com.mongodb.MongoClient;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongodForTestsFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class MongoDbConnectionPoolHealthCheckTest extends AbstractHealthCheckTest {

    private MongodForTestsFactory mongodForTestsFactory;
    private MongoClient mongoClient;

    @BeforeClass
    public void beforeClass() throws Exception {
        mongodForTestsFactory = MongodForTestsFactory.with(Version.Main.PRODUCTION);
        mongoClient = mongodForTestsFactory.newMongo();

        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    @AfterClass
    public void tearDown() throws Exception {
        if (mongodForTestsFactory != null) {
            mongodForTestsFactory.shutdown();
        }
    }

    @BeforeMethod
    public void setUp() throws Exception {



    }

    @Test
    public void whenNoConnectionIsInUseAHealthStatusShouldNotBeReported() throws Exception {
        MongoDbConnectionPoolHealthCheck healthCheck =
                new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", "com.mongodb:type=ConnectionPool,*", null);

        List<HealthStatus> healthStatuses = new ArrayList<>();
        healthCheck.check(healthStatuses, mBeanServer);

        assertThat(healthStatuses, is(empty()));
    }

    @Test
    public void allConnectionsInUseShouldTriggerWarning() throws Exception {
        List<DBPort> ports = new ArrayList<>();
        DBPortPool dbPortPool = mongoClient.getConnector().getDBPortPool(mongoClient.getAddress());

        try {
            while (dbPortPool.getTotal() < dbPortPool.getMaxSize()) {
                ports.add(dbPortPool.get());
            }

            MongoDbConnectionPoolHealthCheck healthCheck =
                    new MongoDbConnectionPoolHealthCheck("se.inera.axel.test", "com.mongodb:type=ConnectionPool,*", null);

            List<HealthStatus> healthStatuses = new ArrayList<>();
            healthCheck.check(healthStatuses, mBeanServer);

            assertThat(healthStatuses, contains(hasProperty("level", equalTo(SeverityLevel.WARNING))));

        } finally {
            for (DBPort port : ports) {
                dbPortPool.done(port);
            }
        }
    }
}
