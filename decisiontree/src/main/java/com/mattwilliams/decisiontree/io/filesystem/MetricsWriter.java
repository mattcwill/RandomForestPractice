package com.mattwilliams.decisiontree.fileio;

import java.io.PrintStream;
import java.util.*;

/**
 * The MetricsWriter class prints accuracy information
 * about predictions.
 *
 * @author Matt Williams
 */
public class MetricsWriter {

    /**
     * Where to write the output to
     */
    private PrintStream writer;

    /**
     * Actual labels
     */
    private List<String> actuals;

    /**
     * Predicted labels
     */
    private List<String> predictions;

    /**
     * Format string for the confusion matrix
     */
    private static final String colFormat = "%8s";

    /**
     * Constructs a new MetricsWriter writing to System.out
     */
    public MetricsWriter() {
        this(System.out);
    }

    /**
     * Constructs a new MetricsWriter
     * @param writer - a PrintStream to write to
     */
    public MetricsWriter(PrintStream writer) {
        this.writer = writer;
        this.actuals = new ArrayList<>();
        this.predictions = new ArrayList<>();
    }

    /**
     * Add a prediction outcome
     * @param actual - the actual (expected) value
     * @param prediction - the predicted value
     */
    public void addSample(String actual, String prediction) {
        actuals.add(actual);
        predictions.add(prediction);
    }

    /**
     * Clears all predictions in this MetricsWriter
     */
    public void clear() {
        actuals.clear();
        predictions.clear();
    }

    /**
     * Prints a description of the accuracy of the predictions
     */
    public void printAccuracyMetrics() {
        int total = actuals.size();
        int correct = 0;


        for (int i=0; i<total; i++) {

            if (actuals.get(i).equals(predictions.get(i))) {
                correct = correct + 1;
            }
        }
        writer.println("Samples classified: " + total);
        double accuracy = ((double)correct) / total;
        writer.println("Accuracy: " + accuracy);

    }

    /**
     * Prints a confusion matrix from the predictions
     */
    public void printConfusionMatrix() {

        // Confusion matrix is a map of actual label to various predictions and their
        // frequency
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();

        // Build an empty confusion matrix by iterating over the actual labels
        // twice. Need to account for fact that some actuals may never be predicted
        // and thus won't appear in predictions list.
        for (String actual : actuals) {

            if (!confusionMatrix.containsKey(actual)) {
                Map<String, Integer> predictions = new HashMap<>();

                for (String prediction : actuals) {
                    predictions.put(prediction, 0);
                }
                confusionMatrix.put(actual, predictions);
            }
        }

        // Now tally up the predictions for each actual value
        for (int i=0; i<actuals.size(); i++) {
            String actual = actuals.get(i);
            Map<String, Integer> predictionCounts = confusionMatrix.get(actual);
            String prediction = predictions.get(i);
            int count = predictionCounts.get(prediction);
            predictionCounts.put(prediction, count + 1);
            confusionMatrix.put(actual, predictionCounts);
        }

        Set<String> labels = confusionMatrix.keySet();

        // Print the matrix
        writer.format(colFormat, " ");

        for (String label : labels) {
            writer.format(colFormat, label);
        }
        writer.println();

        for (String label : labels) {
            writer.format(colFormat, label);

            for (String predictedLabel : labels) {
                writer.format(colFormat, confusionMatrix.get(label).get(predictedLabel));
            }
            writer.println();
        }
    }
}
