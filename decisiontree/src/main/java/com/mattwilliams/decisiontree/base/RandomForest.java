package com.mattwilliams.decisiontree.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The RandomForest class represents a collection of
 * one or more decision trees that work together to make
 * a prediction (majority vote).
 */
public class RandomForest implements Predictor {

    /**
     * List of trees in the forest
     */
    private List<TreeNode> trees = new ArrayList<>();

    /**
     * Add a tree to the forest
     * @param tree
     */
    public void addTree(TreeNode tree) {
        trees.add(tree);
    }

    /**
     * Clear all trees from the forest
     */
    public void clear() {
        trees.clear();
    }

    @Override
    public String predict(Row row) {

        Map<String, Integer> votes = new HashMap<>();

        for (TreeNode tree : trees) {
            String label =  tree.predict(row);

            if (!votes.containsKey(label)) {
                votes.put(label, 0);
            }
            int count = votes.get(label);
            votes.put(label, count + 1);
        }

        String bestLabel = "";
        int max = -1;

        for (Map.Entry<String, Integer> entry : votes.entrySet()) {

            if (entry.getValue() > max) {
                bestLabel = entry.getKey();
                max = entry.getValue();
            }
        }
        return bestLabel;
    }
}
