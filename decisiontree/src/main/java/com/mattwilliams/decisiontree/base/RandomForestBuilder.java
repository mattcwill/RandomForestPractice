package com.mattwilliams.decisiontree.base;

import com.mattwilliams.decisiontree.algorithms.RandomInfoGainStrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The RandomForestBuilder class grows a random forest
 */
public class RandomForestBuilder implements Trainer {

    /**
     * First possible classification label
     */
    protected String label1;

    /**
     * Second possible classification label
     */
    protected String label2;

    /**
     * Number of trees to grow
     */
    protected int numTrees;

    /**
     * How much training data should be used for the tree
     */
    public static double SUBSET = 0.66;

    /**
     * Creates a RandomForestBuilder
     * @param numTrees - number of trees to grow
     * @param label1 - first classification label
     * @param label2 - second classification label
     */
    public RandomForestBuilder(int numTrees, String label1, String label2) {
        this.label1 = label1;
        this.label2 = label2;
        this.numTrees = numTrees;
    }

    @Override
    public Predictor train(Matrix matrix) {

        RandomForest randomForest = new RandomForest();
        DecisionTreeBuilder treeBuilder = new DecisionTreeBuilder(label1, label2, new RandomInfoGainStrategy());

        for (int i=0; i<numTrees; ++i) {

            // Copy the rows in the original dataset
            List<Row> rows = new ArrayList<>(matrix.rows());

            // Draw a random subset of the original rows
            Collections.shuffle(rows);
            int numRows = (int) (rows.size() * SUBSET);

            // Create a new matrix from the random subset
            Matrix dataSubset = new Matrix();
            for (int j=0; j<numRows; j++) {
                dataSubset.addRow(rows.get(j));
            }

            // Grow a tree using that subset and add it to the forest
            randomForest.addTree((TreeNode)treeBuilder.train(dataSubset));
        }
        return randomForest;
    }
}
