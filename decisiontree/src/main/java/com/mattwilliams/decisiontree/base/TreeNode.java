package com.mattwilliams.decisiontree.base;

/**
 * The TreeNode class represents a node in a binary decision tree.
 *
 * @author Matt Williams
 */
public class TreeNode implements Predictor {

    /**
     * The left child node
     */
    private TreeNode leftNode;

    /**
     * The right child node
     */
    private TreeNode rightNode;

    /**
     * The feature that this node is testing
     */
    private int featureIndex;

    /**
     * The value of the feature that this node is testing
     */
    private double value;

    /**
     * If this node is a leaf, the classification represented by this node
     */
    private String label;

    /**
     * Returns whether or not this node is a leaf (has no children)
     * @return <code>true</code> if no children, otherwise <code>false</code>
     */
    public boolean isLeaf() {
        return (leftNode == null) && (rightNode == null);
    }

    /**
     * Returns the index of the feature this node represents
     * @return the index of the feature this node represents
     */
    public int getFeatureIndex() {
        return featureIndex;
    }

    /**
     * Set the feature index of this node
     * @param featureIndex - the feature index of this node
     */
    public void setFeatureIndex(int featureIndex) {
        this.featureIndex = featureIndex;
    }


    /**
     * Returns the left child node
     * @return - the left child TreeNode, or null if leaf
     */
    public TreeNode getLeftNode() {
        return leftNode;
    }

    /**
     * Sets the left child node
     * @param leftNode - the left child TreeNode
     */
    public void setLeftNode(TreeNode leftNode) {
        this.leftNode = leftNode;
    }

    /**
     * Gets the right child node
     * @return - the right child TreeNode, or null if leaf
     */
    public TreeNode getRightNode() {
        return rightNode;
    }

    /**
     * Sets the right child node
     * @param rightNode - the right child TreeNode
     */
    public void setRightNode(TreeNode rightNode) {
        this.rightNode = rightNode;
    }


    /**
     * Set the label for this node, meaning that the sample is classified
     * as the label if this node is reached
     *
     * @param label the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Return the label (classification) for this node, or empty String if this node is not a leaf
     * @return - the label for this node
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Return the value for this node's feature
     * @return - the value for this node's feature
     */
    public double getValue() {
        return value;
    }

    /**
     * Set the feature value for this node
     * @param value - the feature value for this node
     */
    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String predict(Row row) {

        if (row.featureAt(featureIndex) < value) {

            if (!leftNode.isLeaf()) {
                return leftNode.predict(row);
            } else {
                return leftNode.getLabel();
            }

        } else {

            if (!rightNode.isLeaf()) {
                return rightNode.predict(row);
            } else {
                return rightNode.getLabel();
            }
        }
    }

}
