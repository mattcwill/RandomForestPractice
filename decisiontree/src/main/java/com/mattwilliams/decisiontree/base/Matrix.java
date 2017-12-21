package com.mattwilliams.decisiontree.base;

import java.util.*;

/**
 * A Matrix represents a collection of Rows with features
 * and labels.
 *
 * @author Matt Williams
 *
 * @see Row
 */
public class Matrix {

    /**
     * Rows in this matrix
     */
    private ArrayList<Row> rows = new ArrayList<>();

    /**
     * Add a row to this matrix
     * @param row - row to add
     */
    public void addRow(Row row) {
        rows.add(row);
    }

    /**
     * Return the size of this matrix (number of rows)
     * @return the number of rows in the matrix
     */
    public int size() {
        return rows.size();
    }

    /**
     * Get the row at the given index
     * @param index - index of a row
     * @return - the row at the given index
     */
    public Row get(int index) {
        return rows.get(index);
    }

    /**
     * Returns <code>true</code> if empty, otherwise <code>false</code>
     * @return <code>true</code> if empty, otherwise <code>false</code>
     */
    public boolean isEmpty() {
        return rows.isEmpty();
    }

    /**
     * Returns <code>true</code> if all rows in this matrix have the same label,
     * otherwise returns <code>false</code>
     *
     * @return <code>true</code> if all rows in this matrix have the same label,
     * otherwise returns <code>false</code>
     */
    public boolean isPure() {
        for (Row row : rows) {

            if (!row.getLabel().equals(rows.get(0))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get all rows in the matrix. The resulting list cannot be modified (use addRow to modify the Matrix)
     * @return an unmodifiable list of the rows in this matrix
     */
    public List<Row> rows() {
        return Collections.unmodifiableList(this.rows);
    }

    /**
     * Return a list of values for the given feature (noted by its index)
     * @param index - the index of a feature
     * @return a list of values for that feature
     */
    public List<Double> featureValues(int index) {
        ArrayList<Double> values = new ArrayList<>();

        for (Row row : rows) {
            values.add(row.featureAt(index));
        }
        return values;
    }

    /**
     * Return the label that appears most often in this Matrix
     * @return - the label that appears most often in this Matrix
     */
    public String mostCommonLabel() {

        Map<String, Integer> labelCount = new HashMap<>();

        for (Row row : rows) {
            String label = row.getLabel();

            if (!labelCount.containsKey(label)) {
                labelCount.put(label, 0);
            }
            labelCount.put(label, labelCount.get(label) + 1);
        }

        int mostCommonCount = 0;
        String mostCommonFeature = null;

        for (Map.Entry<String, Integer> entry : labelCount.entrySet()) {
            if (entry.getValue() > mostCommonCount) {
                mostCommonCount = entry.getValue();
                mostCommonFeature = entry.getKey();
            }
        }
        return mostCommonFeature;
    }

    /**
     * Return the median value for the given feature (index)
     * @param feature - a feature index
     * @return - the median for the feature's values
     */
    public double median(int feature) {
        List<Double> values = featureValues(feature);
        Collections.sort(values);
        int middle = values.size() / 2;

        if (values.size() % 2 == 1) {
            return values.get(middle);
        } else {
            return (values.get(middle-1) + values.get(middle)) / 2.0;
        }
    }

    /**
     * Split this matrix into two matrices based on the given feature and value. If a Row's value
     * for the feature is less than the given threshold value, it will be placed in the left matrix.
     * Otherwise, it will be placed in the right matrix.
     * @param feature - the feature to split on
     * @param value - a value acting as a theshold value
     * @return a list of Matrix objects, where index 0 is the left split and index 1 is the right split
     */
    public ArrayList<Matrix> split(int feature, double value) {
        Matrix left = new Matrix();
        Matrix right = new Matrix();

        for (Row row : rows) {

            if (row.featureAt(feature) < value) {
                left.addRow(row);
            } else {
                right.addRow(row);
            }
        }
        ArrayList<Matrix> split = new ArrayList<>();
        split.add(left);
        split.add(right);
        return split;
    }

    /**
     * Returns a representation of this data matrix as a comma separated value String, with lines
     * ending in the system's line separator
     * @return a comma-separated String representation of this matrix
     */
    public String toCsv() {
        ArrayList<String> csv = new ArrayList<>();
        for (Row row : rows) {
            csv.add(row.toCsvString());
        }
        return String.join(System.lineSeparator(), csv);
    }

}
