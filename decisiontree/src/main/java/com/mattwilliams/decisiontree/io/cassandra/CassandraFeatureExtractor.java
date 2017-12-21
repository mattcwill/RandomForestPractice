package com.mattwilliams.decisiontree.io.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.mattwilliams.decisiontree.io.AbstractFeatureExtractor;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * The CassandraFeatureExtractor class is responsible for reading the currency
 * input file and writing the long and short matrix files.
 *
 * @author Matt Williams
 * @version 9/20/2017
 */
public class CassandraFeatureExtractor extends AbstractFeatureExtractor {

    private CassandraConnectionInfo connection;

    /**
     * Create a new CassandraFeatureExtractor that will read the given input file. The resulting
     * features will be written to a Cassandra database according to the information in
     * the CassandraConnectionInfo object
     *
     * @param inputFileName - a CSV formatted currency raw data file
     * @param connection - information about the Cassandra connection
     */
    public CassandraFeatureExtractor(String inputFileName, CassandraConnectionInfo connection) {
        super(inputFileName);
        this.connection = connection;
    }


    @Override
    public void processInputFile() {

        if (reader == null) {
            System.err.println("Call open() first");
            return;
        }

        Cluster cluster = null;

        try {
            cluster = Cluster.builder()
                    .addContactPoint(connection.getContactPoint())
                    .build();

            // Create the keyspace if it doesn't exist
            Session session = cluster.connect();
            session.execute("CREATE KEYSPACE IF NOT EXISTS " + connection.getKeyspace() +
                    " WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1}");
            session.close();

            // Create the long and short data tables
            session = cluster.connect(connection.getKeyspace());
            String longTable = connection.getKeyspace() + "." + connection.getLongTableName();
            String shortTable = connection.getKeyspace() + "." + connection.getShortTableName();

            session.execute("CREATE TABLE IF NOT EXISTS " + longTable + " (" +
                    "currency text, " +
                    "time timestamp," +
                    "high double, " +
                    "low double, " +
                    "close double, " +
                    "slope double, " +
                    "change double, " +
                    "label text, " +
                    "PRIMARY KEY(currency, time))");

            session.execute("CREATE TABLE IF NOT EXISTS " + shortTable + " (" +
                    "currency text, " +
                    "time timestamp," +
                    "high double, " +
                    "low double, " +
                    "close double, " +
                    "slope double, " +
                    "change double, " +
                    "label text, " +
                    "PRIMARY KEY(currency, time))");

            // Keep track of hourly data
            LocalDateTime lastTimestamp = null;
            ArrayList<Double> bidValues = new ArrayList<>();
            ArrayList<Double> askValues = new ArrayList<>();
            double lowBidOfHour = Double.MAX_VALUE;
            double highBidOfHour = 0;
            double lowAskOfHour = Double.MAX_VALUE;
            double highAskOfHour = 0;
            double closeAsk = 0; // this hour's closing ask
            double closeBid = 0; // this hour's closing bid
            double lastHourCloseAsk = -1; // last hour's closing ask
            double lastHourCloseBid = -1; // last hour's closing bid

            // Start reading the file line-by-line
            String line;

            while ((line = reader.readLine()) != null) {
                String[] array = line.split(",");
                String currency = array[0];
                LocalDateTime thisTimestamp = LocalDateTime.parse(array[1], dateFormat);
                double bid = Double.parseDouble(array[2]);
                double ask = Double.parseDouble(array[3]);
                bidValues.add(bid);
                askValues.add(ask);

                // If this is the first line, or if this line is in the same hour as
                // the last line, compare and update the hourly data
                if (lastTimestamp == null || sameHour(thisTimestamp, lastTimestamp)) {

                    if (bid > highBidOfHour) {
                        highBidOfHour = bid;
                    }

                    if (bid < lowBidOfHour) {
                        lowBidOfHour = bid;
                    }

                    if (ask > highAskOfHour) {
                        highAskOfHour = ask;
                    }

                    if (ask < lowAskOfHour) {
                        lowAskOfHour = ask;
                    }
                    closeAsk = ask; // in case this is last of hour
                    closeBid = bid;

                } else {

                    // Otherwise, this is the start of a new hour. Write a line for the previous hour
                    // into the long and short output files
                    boolean firstLine = (lastHourCloseBid < 0);

                    if (!firstLine) {
                        insertRow(session, longTable, currency, lastHourCloseBid, lastTimestamp,
                                highBidOfHour, lowBidOfHour, closeBid, getSlope(bidValues));

                        insertRow(session, shortTable, currency, lastHourCloseAsk, lastTimestamp,
                                highAskOfHour, lowAskOfHour, closeAsk, getSlope(askValues));
                    }

                    // Update the closing info for the hour
                    lastHourCloseBid = closeBid;
                    lastHourCloseAsk = closeAsk;
                    lowBidOfHour = bid;
                    highBidOfHour = bid;
                    closeBid = bid;
                    lowAskOfHour = ask;
                    highAskOfHour = ask;
                    closeAsk = ask;
                    bidValues.clear();
                    askValues.clear();
                }
                lastTimestamp = thisTimestamp;
            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    /**
     * Build a table row to be inserted into the long or short data table
     * @param session - a Cassandra session
     * @param tableName - the table name to insert into
     * @param currency - currency pair string
     * @param previousClose - previous hour closing bid/ask
     * @param timeStamp - this hour's timestamp
     * @param high - high bid/ask for this hour
     * @param low - low bid/ask for this hour
     * @param close - this hour's closing bid/ask
     * @param slope - slope value for this hour
     */
    private static void insertRow(Session session, String tableName, String currency,
                                    double previousClose, LocalDateTime timeStamp, double high,
                                  double low, double close, double slope) {

        double change = close - previousClose;
        String label = getLabel(close, previousClose);

        session.execute("INSERT INTO " + tableName + " (currency, time, high, low, close, slope, change, label) " +
                "VALUES (" +
                "'" + currency + "'," +
                "'" + timeStamp.format(hourOnly) + "'," +
                high + "," +
                low + "," +
                close + "," +
                slope + "," +
                change + "," +
                "'" + label + "')");
    }
}
