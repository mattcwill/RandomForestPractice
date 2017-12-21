package com.mattwilliams.decisiontree.base;

import java.util.ArrayList;
import java.util.List;

/**
 * A Row represents a particular sample of data
 *
 * @author Matt Williams
 */
public class Row {

    /**
     * Features for this row
     */
    private List<Double> features = new ArrayList<>();

    /**
     * Label for this row
     */
    private String label;

    /**
     * Return the value for the feature at the given index
     * @param index - the feature index
     * @return the value of the feature at that index
     */
    public double featureAt(int index) {
        return this.features.get(index);
    }

    /**
     * Add a feature to this row
     * @param feature - the value of the feature
     */
    public void addFeature(double feature) {
        this.features.add(feature);
    }

    /**
     * Get the label (classification) for this row
     * @return - the label for this row
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * Set the label for this row
     * @param label - the label for this row
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Return the number of features
     * @return the number of features
     */
    public int numFeatures() {
        return this.features.size();
    }

    /**
     * Returns this row as a comma-delimited String
     * @return a comma-delimited String
     */
    public String toCsvString() {

        ArrayList<String> string = new ArrayList<>();

        for (double feature : features) {
            string.add(String.valueOf(feature));
        }
        string.add(label);
        return String.join(",", string);
    }
}
