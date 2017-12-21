package com.mattwilliams.decisiontree.io;

import java.util.ArrayList;
import java.util.List;

/**
 * The AbstractMetricsWriter class keeps track of prediction results
 * and can write them to a repository (filesystem or database)
 */
public abstract class AbstractMetricsWriter {

    /**
     * Actual labels
     */
    protected List<String> actuals = new ArrayList<>();

    /**
     * Predicted labels
     */
    protected List<String> predictions = new ArrayList<>();


    /**
     * Writes a description of the accuracy of the predictions to a data store
     */
    public abstract void writeMetrics();

    /**
     * Add a prediction outcome
     *
     * @param actual     - the actual (expected) value
     * @param prediction - the predicted value
     */
    public void addSample(String actual, String prediction) {
        actuals.add(actual);
        predictions.add(prediction);
    }

    /**
     * Clears all predictions in this CassandraMetricsWriter
     */
    public void clear() {
        actuals.clear();
        predictions.clear();
    }

}
