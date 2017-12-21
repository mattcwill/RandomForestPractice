package com.mattwilliams.decisiontree.io;

import com.mattwilliams.decisiontree.base.Matrix;
import com.mattwilliams.decisiontree.base.Row;

import java.util.Collections;
import java.util.List;

/**
 * AbstractDataSetBuilder is an abstract base class for objects
 * that can create test and training data sets from a labeled feature matrix
 */
public abstract class AbstractDataSetBuilder {

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
     * Constructs a new CassandraDataSetBuilder
     * @param trainingRatio - ratio of samples to use as training data (0.0 - 1.0)
     */
    public AbstractDataSetBuilder(double trainingRatio) {
        this.trainingRatio = trainingRatio;
        trainingSet = new Matrix();
        testSet = new Matrix();
    }

    /**
     * Reads all input data in the given data source and builds a training matrix
     * and a test matrix
     * @param dataSource - A reference to a source of data (could be database table name or directory)
     * @return - total number of samples found in the data source
     */
    public abstract int buildDataSets(String dataSource);

    public double getTrainingRatio() {
        return trainingRatio;
    }

    public void setTrainingRatio(double trainingRatio) {
        this.trainingRatio = trainingRatio;
    }

    /**
     * @return - the training set
     */
    public Matrix getTrainingSet() {
        return trainingSet;
    }

    /**
     * @return the test set
     */
    public Matrix getTestSet() {
        return testSet;
    }

    /**
     * Given a list of rows, this method splits the list into a training set
     * and a test set according to the training ratio. These sets can be retrieved
     * using the getTrainingSet and getTestSet methods, respectively.
     *
     * @param allRows - rows to separate into training and test data sets
     */
    protected void separateIntoTestAndTrainingData(List<Row> allRows) {
        // Randomize the rows so that our training samples and test samples aren't
        // biased
        Collections.shuffle(allRows);

        // Add rows to either the training set or test set
        int trainingRows = (int) (allRows.size() * trainingRatio);

        for (int i=0; i<allRows.size(); ++i) {
            Row row = allRows.get(i);

            if (i < trainingRows) {
                trainingSet.addRow(row);
            } else {
                testSet.addRow(row);
            }
        }
    }
}
