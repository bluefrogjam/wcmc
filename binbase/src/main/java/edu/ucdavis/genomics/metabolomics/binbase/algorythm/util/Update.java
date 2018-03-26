package edu.ucdavis.genomics.metabolomics.binbase.algorythm.util;

import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.CorrectionMethod;
import edu.ucdavis.genomics.metabolomics.binbase.algorythm.methods.GitterMethode;
import edu.ucdavis.genomics.metabolomics.util.database.ConnectionFactory;
import org.slf4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.*;

/**
 * Created by wohlgemuth on 1/18/17.
 */
public class Update {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public void setCallback(SampleCallback callback) {
        this.callback = callback;
    }

    private SampleCallback callback = new SampleCallback() {
        @Override
        public void processed(int sampleId,boolean success) {

        }
    };

    /**
     * updates the given sample, including the correction
     *
     * @param sampleId
     * @param connection
     */
    public void sample(int sampleId, Connection connection) throws Exception {

        try {

            logger.info("updating sample: " + sampleId);
            CorrectionMethod correction = new CorrectionMethod();
            GitterMethode matching = new GitterMethode();

            correction.setConnection(connection);
            matching.setConnection(connection);

            matching.setNewBinAllowed(false);

            correction.setSampleId(sampleId);
            correction.run();

            matching.setSampleId(sampleId);
            matching.run();

            //update the database time stamps

            PreparedStatement statement = connection.prepareStatement("update samples set date_of_import = ?, finished='TRUE' where sample_id = ?");

            statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            statement.setInt(2, sampleId);
            statement.execute();
            statement.close();

            callback.processed(sampleId,true);

        } catch (Exception e) {
            logger.warn(e.getMessage());
            logger.debug(e.getMessage(), e);

            PreparedStatement statement = connection.prepareStatement("update samples set finished='TRUE' where sample_id = ?");

            statement.setInt(1, sampleId);
            statement.execute();
            statement.close();
            callback.processed(sampleId,false);
        }
    }

    /**
     * reprocesses all samples with the given bin id
     *
     * @param binId
     */
    public void binAnnotations(int binId, ConnectionFactory factory) throws Exception {

        Connection connection = factory.getConnection();


        processQuery(factory, "select a.sample_id from samples a, spectra b where a.sample_id = b.sample_id and b.bin_id = " + binId);

        factory.close(connection);
    }

    /**
     * reprocesses the complete database
     *
     * @throws Exception
     */
    public void database(ConnectionFactory factory) throws Exception {

        Connection connection = factory.getConnection();


        processQuery(factory, "select sample_id from samples where visible = 'TRUE'");

        factory.close(connection);

    }

    /**
     * processes the actual query and provides some progress feedback using logging
     *
     * @param factory
     * @param query
     * @throws Exception
     */
    private void processQuery(final ConnectionFactory factory, final String query) throws Exception {
        if (query.trim().toLowerCase().startsWith("select")) {

            final ResultSet set2 = factory.getConnection().createStatement().executeQuery(query);

            int count = 0;

            while (set2.next()) {
                count = count + 1;
            }

            set2.close();

            final ResultSet set = factory.getConnection().createStatement().executeQuery(query);
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

            int counter = 0;

            ArrayList<Future<Integer>> results = new ArrayList<Future<Integer>>();

            while (set.next()) {

                final int id = set.getInt(1);
                Callable<Integer> task = new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        try {
                            long begin = System.currentTimeMillis();

                            ConnectionFactory f2 = factory.createFactory();
                            f2.setProperties(factory.getProperties());
                            sample(id, f2.getConnection());
                            f2.close();

                            long end = System.currentTimeMillis();

                            return (int) (end - begin);
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                            return 0;
                        }
                    }
                };

                results.add(executor.submit(task));

            }

            double average = 0;

            for (Future<Integer> f : results) {
                counter++;
                if (counter > 1) {
                    average = (f.get(5000, TimeUnit.DAYS) + average) / 2;
                } else {
                    average = f.get(5000, TimeUnit.DAYS);
                }


                logger.info("processed:" + (double) counter / count * 100 + "% (" + counter + ") out of " + count + " samples");
                logger.info("remaining: " + ((count - counter) * average) / 1000 / 60 + " minutes, average processing time is: " + average + " ms for a sample");
            }
            set.close();

            executor.shutdown();
        } else {
            throw new SQLException("only selects are supported");
        }
    }

    /**
     * exexute query hard
     *
     * @param factory
     * @param query
     * @throws Exception
     */
    public void executeQuery(final ConnectionFactory factory, final String query) throws Exception {


        processQuery(factory, query);
    }

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = ConnectionFactory.createFactory();

        Properties p = System.getProperties();
        p.setProperty("Binbase.database", "binbase");
        p.setProperty("Binbase.type", "3");
        p.setProperty("Binbase.user", "rtx5recal");
        p.setProperty("Binbase.password", "c258083");
        p.setProperty("Binbase.host", "venus.fiehnlab.ucdavis.edu");


        factory.setProperties(p);

        Update update = new Update();
        //update.sample(695715,factory.getConnection());

        /*
        //update curve
        update.sample(722318,factory.getConnection());

        //match sample, which should fail correction
        update.sample(82700,factory.getConnection());

*/
        update.binAnnotations(160842, factory);

    }
}
