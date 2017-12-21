package com.mattwilliams.decisiontree.io;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * The AbstractFeatureExtractor class is responsible for reading the currency
 * input file and writing the long and short feature matrices.
 *
 * @author Matt Williams
 * @version 11/3/2017
 */
public abstract class AbstractFeatureExtractor {

    /**
     * The input file path
     */
    protected String inputFileName;

    /**
     * Input file reader
     */
    protected BufferedReader reader = null;

    /**
     * Format for reading dates from input data file
     */
    protected static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss.SSS");

    /**
     * Format for writing and comparing dates - only goes up to the hour
     */
    protected static DateTimeFormatter hourOnly = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00");

    public AbstractFeatureExtractor(String inputFileName) {
        this.inputFileName = inputFileName;
    }

    /**
     * Open the currency input data file for reading
     * @throws FileNotFoundException if could not open input file
     */
    public void open() throws FileNotFoundException {
        File file = new File(inputFileName);
        reader = new BufferedReader(new FileReader(file));
    }

    /**
     * Close the input data file
     * @throws IOException
     */
    public void close() throws IOException {
        reader.close();
        reader = null;
    }

    /**
     * Reads the currency input data file and extracts features into
     * a repository (like a file or database)
     */
    public abstract void processInputFile();

    /**
     * Compares two dates and returns <code>true</code> if they are in the same hour
     * @param date1
     * @param date2
     * @return <code>true</code> if date1 and date2 have the same hour
     */
    public static boolean sameHour(LocalDateTime date1, LocalDateTime date2) {
        return date1.format(hourOnly).equals(date2.format(hourOnly));
    }

    /**
     * Return a label characterizing the difference between two values
     * @param current - the current value
     * @param previous - the previous value
     * @return "UP" or "DOWN" depending on direction of change
     */
    public static String getLabel(double current, double previous) {
        return current > previous ? "UP" : "DOWN";
    }

    public static double getSlope(List<Double> values) {
        SimpleRegression regression = new SimpleRegression();

        for (int i=0; i<values.size(); i++) {
            regression.addData(i, values.get(i));
        }
        return regression.getSlope();
    }
}
