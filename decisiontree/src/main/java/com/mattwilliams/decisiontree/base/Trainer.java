package com.mattwilliams.decisiontree.base;

/**
 * Interface for classes that can train Predictors
 */
public interface Trainer {

    /**
     * Use the data in the given matrix to train a Predictor
     * @param matrix - a matrix of training data
     * @return - a Predictor object
     */
    Predictor train(Matrix matrix);
}
