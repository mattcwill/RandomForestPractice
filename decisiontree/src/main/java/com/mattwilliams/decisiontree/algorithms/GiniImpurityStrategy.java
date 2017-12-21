package com.mattwilliams.decisiontree.algorithms;

import com.mattwilliams.decisiontree.base.Matrix;

import java.util.List;

/**
 * Example of possible implementation of BestSplitStrategy that uses Gini impurity as
 * a best split measure for CART tree
 *
 * @author Matt Williams
 *
 * @// TODO: 9/30/17 Just a possible example
 */
public class GiniImpurityStrategy implements BestSplitStrategy {
    @Override
    public void evaluateSplits(List<Integer> featureIndices, Matrix rows, String label1, String label2) {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
    @Override
    public int bestFeature() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }

    @Override
    public double featureValue() {
        throw new UnsupportedOperationException("Not Yet Implemented");
    }
}
