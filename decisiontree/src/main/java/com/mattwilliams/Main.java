package com.mattwilliams;

import com.mattwilliams.decisiontree.base.*;
import com.mattwilliams.decisiontree.base.mapreduce.MRForestBuilder;
import com.mattwilliams.decisiontree.io.AbstractDataSetBuilder;
import com.mattwilliams.decisiontree.io.AbstractFeatureExtractor;
import com.mattwilliams.decisiontree.io.AbstractMetricsWriter;
import com.mattwilliams.decisiontree.io.cassandra.CassandraConnectionInfo;
import com.mattwilliams.decisiontree.io.cassandra.CassandraDataSetBuilder;
import com.mattwilliams.decisiontree.io.cassandra.CassandraFeatureExtractor;
import com.mattwilliams.decisiontree.io.cassandra.CassandraMetricsWriter;

import java.io.IOException;
import java.text.NumberFormat;

/**
 * Advanced Big Data Assignment 5
 * Main class for the MapReduce application.
 *
 * @author Matt Williams (11/19/17)
 */
public class Main {

    private static final double trainingRatio = 0.8;

    public static void main(String[] args) {
        org.apache.log4j.BasicConfigurator.configure();

        CassandraConnectionInfo connection = new CassandraConnectionInfo();
        connection.setContactPoint("127.0.0.1");
        connection.setKeyspace("abd5");
        connection.setLongTableName("long");
        connection.setShortTableName("short");
        connection.setResultsTableName("results");

        try {

            if (args.length < 1) {
                printUsage();
            }

            else if (args[0].equals("-build")) {

                if (args.length == 2) {
                    String inputDataFile = args[1];
                    System.out.println("Processing input file " + inputDataFile);
                    AbstractFeatureExtractor extractor = new CassandraFeatureExtractor(inputDataFile, connection);
                    extractor.open();
                    extractor.processInputFile();
                    extractor.close();

                } else {
                    printUsage();
                }

            } else if (args[0].equals("-train")) {

                String label1 = "UP";
                String label2 = "DOWN";
                int numberOfTrees = 7;

                if (args.length == 2) {
                    numberOfTrees = Integer.valueOf(args[1]);
                }
                String dataSource = "long"; // Use the long table for this example

                AbstractDataSetBuilder dataSetBuilder = new CassandraDataSetBuilder(trainingRatio, connection);

                System.out.println("Looking for input files data in " + dataSource);
                System.out.print("Building data sets...");
                int rows = dataSetBuilder.buildDataSets(dataSource);
                System.out.println("Done!");
                System.out.println("Found " + rows + " samples. Using " + NumberFormat.getPercentInstance().format(trainingRatio) + " training data.");

                Matrix trainingData = dataSetBuilder.getTrainingSet();
                Matrix testData = dataSetBuilder.getTestSet();

                System.out.print("Building decision tree for labels: " + label1 + ", " + label2 + "...");
                Trainer trainer = new MRForestBuilder(numberOfTrees, label1, label2);
                Predictor predictor = trainer.train(trainingData);
                System.out.println("Done!");

                System.out.print("Making predictions...");
                AbstractMetricsWriter metrics = new CassandraMetricsWriter(connection);

                for (Row row : testData.rows()) {
                    String predicted = predictor.predict(row);
                    metrics.addSample(row.getLabel(), predicted);
                }
                System.out.println("Done!");
                System.out.println();
                metrics.writeMetrics();
                System.out.println("All Done!");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void printUsage() {
        System.out.println("Usage for building feature matrix: java -jar MapReduceApp.jar -build [inputFile]");
        System.out.println("inputFile - path to a raw data input file (.csv)");
        System.out.println();
        System.out.println("Usage for training algorithm and writing results: java -jar MapredApp.jar -train [numTrees]");
        System.out.println("numTrees - number of trees to grow, default = 7");
        System.out.println();
    }
}
