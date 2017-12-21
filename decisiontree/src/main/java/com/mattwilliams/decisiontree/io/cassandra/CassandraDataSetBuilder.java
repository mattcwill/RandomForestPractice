package com.mattwilliams.decisiontree.io.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.mattwilliams.decisiontree.base.Row;
import com.mattwilliams.decisiontree.io.AbstractDataSetBuilder;

import java.util.ArrayList;

/**
 * The CassandraDataSetBuilder class is used to build training and test data from
 * a matrix stored in a Cassandra database.
 *
 * @author Matt Williams
 */
public class CassandraDataSetBuilder extends AbstractDataSetBuilder {

    /**
     * Cassandra connection info
     */
    private CassandraConnectionInfo connection;

    /**
     * Constructs a new CassandraDataSetBuilder
     * @param trainingRatio - ratio of samples to use as training data (0.0 - 1.0)
     * @oaran connection - information required to connect to Cassandra
     */
    public CassandraDataSetBuilder(double trainingRatio, CassandraConnectionInfo connection) {
        super(trainingRatio);
        this.connection = connection;
    }

    @Override
    public int buildDataSets(String dataSource) {

        ArrayList<Row> rows = new ArrayList<>();

        Cluster cluster = null;

        try {
            cluster = Cluster.builder()
                    .addContactPoint(connection.getContactPoint())
                    .build();
            Session session = cluster.connect(connection.getKeyspace());

            String table = connection.getKeyspace() + "." + dataSource;
            ResultSet results = session.execute("SELECT * FROM " + table);

            for (com.datastax.driver.core.Row resultRow : results) {

                Row row = new Row();
                row.addFeature(resultRow.getDouble("high"));
                row.addFeature(resultRow.getDouble("low"));
                row.addFeature(resultRow.getDouble("close"));
                row.addFeature(resultRow.getDouble("slope"));
                row.setLabel(resultRow.getString("label"));
                rows.add(row);
            }
            separateIntoTestAndTrainingData(rows);

        } finally {
            if (cluster != null) {
                cluster.close();
            }
        }
        return rows.size();
    }
}
