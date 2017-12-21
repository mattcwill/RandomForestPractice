package com.mattwilliams.decisiontree.algorithms;

import com.mattwilliams.decisiontree.base.Matrix;
import com.mattwilliams.decisiontree.base.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * The InformationGainStrategy uses the entropy/information gain algorithm
 * to evaluate splits
 *
 * @author Matt Williams
 */
public class InformationGainStrategy implements BestSplitStrategy {

    private int bestFeature;
    private double featureValue;

    @Override
    public void evaluateSplits(List<Integer> featureIndices, Matrix matrix, String label1, String label2) {

        if (featureIndices.isEmpty()) {
            this.bestFeature = -1;
            this.featureValue = -1;
            System.err.println("Invalid feature array!");
            return;
        }

        this.bestFeature = featureIndices.get(0);
        this.featureValue = matrix.median(this.bestFeature);

        // Entropy before
        double beforeEntropy = calculateEntropy(matrix, label1, label2);
        int beforeRows = matrix.size();

        double largestGain = 0;

        for (int feature : featureIndices) {

            double value = matrix.median(feature);

            ArrayList<Matrix> splits = matrix.split(feature, value);
            Matrix left = splits.get(0);
            Matrix right = splits.get(1);

            // Calculate information gain
            double weightLeft = ((double)left.size()) / beforeRows;
            double weightRight = ((double)right.size()) / beforeRows;
            double entropyLeft = weightLeft * calculateEntropy(left, label1, label2);
            double entropyRight = weightRight * calculateEntropy(right, label1, label2);
            double afterEntropy = entropyLeft + entropyRight;
            double gain = beforeEntropy - afterEntropy;

            // Update best feature/value
            if (gain > largestGain) {
                largestGain = gain;
                this.bestFeature = feature;
                this.featureValue = value;
            }
        }
    }

    @Override
    public int bestFeature() {
        return this.bestFeature;
    }

    public double featureValue() {
        return this.featureValue;
    }

    private static double calculateEntropy(Matrix matrix, String label1, String label2) {

        int total = matrix.size();
        int numLabel1 = 0;
        int numLabel2 = 0;

        for (Row row : matrix.rows()) {

            if (row.getLabel().equals(label1)) {
                numLabel1++;
            } else {
                numLabel2++;
            }
        }
        double prob1 = ((double) numLabel1) / total;
        double prob2 = ((double) numLabel2) / total;

        return -1 * prob1 * Math.log(prob1) - prob2 * Math.log(prob2);
    }
}
