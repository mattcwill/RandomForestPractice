package com.mattwilliams.decisiontree.io.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.InvalidQueryException;
import com.mattwilliams.decisiontree.io.AbstractMetricsWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The CassandraMetricsWriter class prints accuracy information
 * about predictions and writes the results to a Cassandra database.
 *
 * @author Matt Williams
 */
public class CassandraMetricsWriter extends AbstractMetricsWriter {

    private CassandraConnectionInfo connection;

    /**
     * Constructs a CassandraMetricsWriter
     * @param connection - Cassandra connection info
     */
    public CassandraMetricsWriter(CassandraConnectionInfo connection) {
        super();
        this.connection = connection;
    }

    @Override
    public void writeMetrics() {

        Cluster cluster = Cluster.builder()
                .addContactPoint(connection.getContactPoint())
                .build();
        Session session = cluster.connect(connection.getKeyspace());

        String table = connection.getKeyspace() + "." + connection.getResultsTableName();

        session.execute("CREATE TABLE IF NOT EXISTS " + table + " (" +
                "time timestamp, " +
                "numSamples int, " +
                "accuracy double, " +
                "PRIMARY KEY(time))");

        int total = actuals.size();
        int correct = 0;

        for (int i = 0; i < total; i++) {

            if (actuals.get(i).equals(predictions.get(i))) {
                correct = correct + 1;
            }
        }

        long seconds = System.currentTimeMillis();
        double accuracy = ((double) correct) / total;

        session.execute("INSERT INTO " + table + " (time, numSamples, accuracy) VALUES (" +
                seconds + "," +
                total + "," +
                accuracy + ")");


        // Confusion matrix is a map of actual label to various predictions and their
        // frequency
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();

        // Build an empty confusion matrix by iterating over the actual labels
        // twice. Need to account for fact that some actuals may never be predicted
        // and thus won't appear in predictions list.
        for (String actual : actuals) {

            if (!confusionMatrix.containsKey(actual)) {
                Map<String, Integer> predictions = new HashMap<>();

                for (String prediction : actuals) {
                    predictions.put(prediction, 0);
                }
                confusionMatrix.put(actual, predictions);
            }
        }

        // Now tally up the predictions for each actual value
        for (int i = 0; i < actuals.size(); i++) {
            String actual = actuals.get(i);
            Map<String, Integer> predictionCounts = confusionMatrix.get(actual);
            String prediction = predictions.get(i);
            int count = predictionCounts.get(prediction);
            predictionCounts.put(prediction, count + 1);
            confusionMatrix.put(actual, predictionCounts);
        }

        Set<String> labels = confusionMatrix.keySet();

        for (String label : labels) {

            for (String predictedLabel : labels) {

                // Column names for confusion matrix results in table
                // follow convention "actual_predicted", so column name of
                // "down_up" means number of times an actual DOWN sample was
                // predicted as UP

                String column = label + "_" + predictedLabel;

                try {
                    session.execute("ALTER TABLE " + table + " ADD " + column + " int");
                } catch (InvalidQueryException e) {
                    System.out.println("Column " + column + " already exists!");
                }

                session.execute("UPDATE " + table + " SET " + column + " = " +
                        confusionMatrix.get(label).get(predictedLabel) + " WHERE time = " + seconds);

            }
        }
        session.close();
        cluster.close();
    }
}