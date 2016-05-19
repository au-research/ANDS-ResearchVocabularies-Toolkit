/** See the file "LICENSE" for the full license governing this code. */

package au.org.ands.vocabs.toolkit.test.arquillian;

import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2Connection;
import org.dbunit.operation.DatabaseOperation;
import org.hibernate.HibernateException;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import au.org.ands.vocabs.toolkit.db.DBContext;
import au.org.ands.vocabs.toolkit.test.utils.NetClientUtils;

/** Support methods for testing with Arquillian. */
public final class ArquillianTestUtils {

    /** Logger. */
    private static Logger logger;

    /** The {@link ClassLoader} of this class. Used for invoking
     * {@link ClassLoader#getResourceAsStream(String)}. */
    private static ClassLoader classLoader;

    static {
        logger = LoggerFactory.getLogger(
                MethodHandles.lookup().lookupClass());
        classLoader = MethodHandles.lookup().lookupClass().
                getClassLoader();
    }

    /** Private constructor for a utility class. */
    private ArquillianTestUtils() {
    }

    /** Get an {@link InputStream} for a file, given its filename.
     * @param filename The filename of the resource.
     * @return An {@link InputStream} for the resource.
     */
    private static InputStream getResourceAsInputStream(
            final String filename) {
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IllegalArgumentException("Can't load resource: "
                    + filename);
        }
        return inputStream;
    }

    /** Clear the database.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void clearDatabase() throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {

            IDatabaseConnection connection = new H2Connection(conn, null);

            logger.info("doing clean_insert");
            FlatXmlDataSet dataset = new FlatXmlDataSetBuilder()
                    .setMetaDataSetFromDtd(getResourceAsInputStream(
                            "test/dbunit-toolkit-export-choice.dtd"))
                    .build(getResourceAsInputStream("test/blank-dbunit.xml"));

            DatabaseOperation.DELETE_ALL.execute(connection, dataset);
            // Force commit at the JDBC level, as closing the EntityManager
            // does a rollback!
            conn.commit();
        }
        em.close();
    }

    /** Load a DBUnit test file into the database.
     * @param testName The name of the test method. Used to generate
     *      the filename of the file to load.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws HibernateException If a problem getting the underlying
     *          JDBC connection.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void loadDbunitTestFile(final String testName) throws
        DatabaseUnitException, HibernateException, IOException, SQLException {
        EntityManager em = DBContext.getEntityManager();
        try (Connection conn = em.unwrap(SessionImpl.class).connection()) {

            IDatabaseConnection connection = new H2Connection(conn, null);

            FlatXmlDataSet dataset = new FlatXmlDataSetBuilder()
                    .setMetaDataSetFromDtd(getResourceAsInputStream(
                            "test/dbunit-toolkit-export-choice.dtd"))
                    .build(getResourceAsInputStream(
                            "test/tests/au.org.ands.vocabs.toolkit."
                            + "test.arquillian.AllArquillianTests."
                            + testName
                            + "/input-dbunit.xml"));
            logger.info("doing clean_insert");
            DatabaseOperation.CLEAN_INSERT.execute(connection, dataset);
            // Force commit at the JDBC level, as closing the EntityManager
            // does a rollback!
            conn.commit();
        }
        em.close();
    }

    /** Client-side clearing of the database.
     * @param baseURL The base URL to use to connect to the Toolkit.
     * @throws DatabaseUnitException If a problem with DBUnit.
     * @throws IOException If a problem getting test data for DBUnit.
     * @throws SQLException If DBUnit has a problem performing
     *           performing JDBC operations.
     */
    public static void clientClearDatabase(final URL baseURL) throws
        DatabaseUnitException, IOException, SQLException {
        logger.info("In clientClearDatabase()");
        Response response = NetClientUtils.doGet(baseURL,
                "testing/clearDB");

        Assert.assertEquals(response.getStatusInfo().getFamily(),
                Family.SUCCESSFUL,
                "clientClearDatabase response status");
        response.close();
    }


}
