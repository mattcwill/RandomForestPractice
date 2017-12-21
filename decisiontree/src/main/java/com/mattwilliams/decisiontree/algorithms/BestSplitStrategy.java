package com.mattwilliams.decisiontree.algorithms;

import com.mattwilliams.decisiontree.base.Matrix;

import java.util.List;

/**
 * The BestSplitStrategy interface represents an algorithm for finding the optimal
 * split for a given set of data for use in building a decision tree.
 */
public interface BestSplitStrategy {

    /**
     * Given a list of features and data, evaluate all possible splits to determine the optimal
     * split. The data can have one of two possible labels.
     *
     * @// TODO: 9/30/17 Should eventually not require label1 and label2, should figure it out
     *
     * @param featureIndices - a list of features
     * @param rows - data to split
     * @param label1 - the first possible label
     * @param label2 - the second possible label
     */
    void evaluateSplits(List<Integer> featureIndices, Matrix rows, String label1, String label2);

    /**
     * Return the index of the best feature to split on based on last evaluation
     * @return the index of the best feature to split on based on last evaluation
     */
    int bestFeature();

    /**
     * Return the optimal feature value to split on based on the last evaluation
     * @return the optimal feature value to split on based on the last evaluation
     */
    double featureValue();
}
