package com.mattwilliams.decisiontree.fileio;

import com.mattwilliams.decisiontree.base.Matrix;
import com.mattwilliams.decisiontree.base.Row;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The DataSetBuilder class is used to build training and test data from
 * input files in a particular directory.
 *
 * @author Matt Williams
 */
public class DataSetBuilder {

    /**
     * The directory to look in
     */
    private String directory;

    /**
     * Ratio of samples to be used in the training set
     */
    private double trainingRatio;

    /**
     * Data representing the training set
     */
    private Matrix trainingSet;

    /**
     * Data representing the test set
     */
    private Matrix testSet;

    /**
     * Constructs a new DataSetBuilder
     * @param trainingRatio - ratio of samples to use as training data (0.0 - 1.0)
     */
    public DataSetBuilder(double trainingRatio) {
        this.trainingRatio = trainingRatio;
        trainingSet = new Matrix();
        testSet = new Matrix();
    }

    /**
     * Reads all input .csv files in the given directory and builds a training matrix
     * and a test matrix
     * @param directory - the directory to look in
     * @return - total number of samples found in the directory
     * @throws IOException
     */
    public int buildDataSets(String directory) throws IOException {

        ArrayList<Row> rows = new ArrayList<>();

        // Open all files in the directory
        // TODO better error handling
        List<File> files = Files.walk(Paths.get(directory))
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .collect(Collectors.toList());

        for (File file : files) {
            rows.addAll(processFile(file));
        }

        // Randomize the rows so that our training samples and test samples aren't
        // biased
        Collections.shuffle(rows);

        // Add rows to either the training set or test set
        int trainingRows = (int) (rows.size() * trainingRatio);

        for (int i=0; i<rows.size(); ++i) {
            Row row = rows.get(i);

            if (i < trainingRows) {
                trainingSet.addRow(row);
            } else {
                testSet.addRow(row);
            }
        }
        return rows.size();
    }

    /**
     * Returns the training set
     * @return the training set
     */
    public Matrix getTrainingSet() {
        return this.trainingSet;
    }

    /**
     * Returns the test set
     * @return the test set
     */
    public Matrix getTestSet() {
        return this.testSet;
    }

    /**
     * Process the given file and return data rows
     * @param file - a CSV file
     * @return a list of data rows from the file
     */
    private List<Row> processFile(File file)  {

        List<Row> rows = new ArrayList<>();

        if (file.getName().endsWith(".csv")) {

            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = br.readLine()) != null) {
                    String[] data = line.split(",");
                    Row row = new Row();
                    row.addFeature(Double.parseDouble(data[2]));
                    row.addFeature(Double.parseDouble(data[3]));
                    row.addFeature(Double.parseDouble(data[4]));
                    row.addFeature(Double.parseDouble(data[5]));
                    row.setLabel(data[7]);
                    rows.add(row);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return rows;
    }

}
