package com.mattwilliams.decisiontree.base;

/**
 * Classes implementing Predictor can predict a label given a row of data.
 */
public interface Predictor {

    /**
     * Predict the label for the given row
     * @param row - a row with features
     * @return the predicted label
     */
    String predict(Row row);
}
