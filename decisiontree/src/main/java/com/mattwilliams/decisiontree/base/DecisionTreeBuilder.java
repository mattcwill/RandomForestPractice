package com.mattwilliams.decisiontree.base;

import com.mattwilliams.decisiontree.algorithms.BestSplitStrategy;
import com.mattwilliams.decisiontree.algorithms.InformationGainStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * The DecisionTreeBuilder class is used to construct a decision tree. You
 * can specify different splitting algorithms to implement different tree
 * algorithms. Currently accomodates data with 2 possible labels.
 *
 * @author Matt Williams
 */
public class DecisionTreeBuilder implements Trainer {

    /**
     * Strategy used to rank possible splits
     */
    private BestSplitStrategy splitStrategy;

    /**
     * The first possible label
     */
    private String label1;

    /**
     * The second possible label
     */
    private String label2;

    /**
     * Constructs a DecisionTreeBuilder that will classify data as one of two
     * labels. Uses the information gain algorithm to choose best split.
     * @param label1 - the first possible label
     * @param label2 - the second possible label
     */
    public DecisionTreeBuilder(String label1, String label2) {
        this(label1, label2, new InformationGainStrategy());
    }

    /**
     * Constructs a DecisionTreeBuilder that will classify data as one of two
     * labels.
     * @param label1 - the first possible label
     * @param label2 - the second possible label
     * @param splitStrategy - the strategy to use to find the best split
     */
    public DecisionTreeBuilder(String label1, String label2, BestSplitStrategy splitStrategy) {
        this.label1 = label1;
        this.label2 = label2;
        this.splitStrategy = splitStrategy;
    }

    /**
     * Get the split strategy
     * @return - the split strategy
     */
    public BestSplitStrategy getSplitStrategy() {
        return splitStrategy;
    }

    /**
     * Sets the split strategy
     * @param splitStrategy - the split strategy
     */
    public void setSplitStrategy(BestSplitStrategy splitStrategy) {
        this.splitStrategy = splitStrategy;
    }

    @Override
    public Predictor train(Matrix matrix) {

        if (matrix.isEmpty()) {
            throw new IllegalArgumentException("Training set can't be empty");
        }

        List<Integer> featureIndicies = new ArrayList<>();

        for (int i = 0; i < matrix.get(0).numFeatures(); ++i) {
            featureIndicies.add(i);
        }
        return recurseBuildTree(featureIndicies, matrix);
    }

    private TreeNode recurseBuildTree(List<Integer> featureIndices, Matrix split) {

        if (split.isEmpty()) {
            return null; // Shouldn't happen?

        } else if (split.isPure() || featureIndices.isEmpty()) {
            // Return a leaf node
            TreeNode node = new TreeNode();
            node.setLabel(split.mostCommonLabel());
            return node;

        } else {
            // Split into two child nodes
            TreeNode node = new TreeNode();
            splitStrategy.evaluateSplits(featureIndices, split, label1, label2);
            ArrayList<Matrix> newSplit = split.split(splitStrategy.bestFeature(), splitStrategy.featureValue());

            // Copy the feature list and remove the best feature
            List<Integer> list = new ArrayList<>(featureIndices.size());
            list.addAll(featureIndices);

            int index = list.indexOf(splitStrategy.bestFeature());

            if (index >= 0) {
                list.remove(index);
            } else {
                System.err.println("Invalid index!");
            }

            node.setFeatureIndex(splitStrategy.bestFeature());
            node.setValue(splitStrategy.featureValue());

            // Recurse to build the child nodes
            node.setLeftNode(recurseBuildTree(list, newSplit.get(0)));
            node.setRightNode(recurseBuildTree(list, newSplit.get(1)));
            return node;
        }
    }
}
