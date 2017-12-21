package com.mattwilliams.decisiontree.algorithms;

import com.mattwilliams.decisiontree.base.Matrix;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * RandomInfoGainStrategy uses the same information gain metric as InformationGainStrategy
 * to determine best split, however it only evaluates a subset of the available features
 * for use in random forest implementation
 */
public class RandomInfoGainStrategy extends InformationGainStrategy {

    @Override
    public void evaluateSplits(List<Integer> featureIndices, Matrix matrix, String label1, String label2) {
        List<Integer> randomFeatures = getRandomFeatureSubset(featureIndices);
        super.evaluateSplits(randomFeatures, matrix, label1, label2);
    }

    /**
     * Returns a random subset of the original feature list (subset length is the square
     * root of the original list size).
     * @param originalFeatures - a set of features (indicies)
     * @return a random subset of features with size == sqrt(originalFeatures.size())
     */
    private List<Integer> getRandomFeatureSubset(List<Integer> originalFeatures) {
        List<Integer> features = new ArrayList<>(originalFeatures);
        int numFeatures = (int) Math.sqrt(originalFeatures.size());
        Collections.shuffle(features);
        return features.subList(0, numFeatures);
    }
}
