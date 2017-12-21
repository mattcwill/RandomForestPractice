package com.mattwilliams.decisiontree.io.cassandra;

/**
 * CassandraConnectionInfo encapsulates details needed to connect to
 * a Cassandra cluster and run the Advanced Big Data assignment.
 *
 * @author Matt Williams
 */
public class CassandraConnectionInfo {

    private String contactPoint;

    private String keyspace;

    private String longTableName;

    private String shortTableName;

    private String resultsTableName;

    /**
     * Get the Cassandra contact point (i.e. "127.0.0.1")
     * @return the contact point
     */
    public String getContactPoint() {
        return contactPoint;
    }

    /**
     * Set the Cassandra contact point
     * @param contactPoint - the contact point to set
     */
    public void setContactPoint(String contactPoint) {
        this.contactPoint = contactPoint;
    }

    /**
     * Get the Cassandra keyspace
     * @return the keyspace
     */
    public String getKeyspace() {
        return keyspace;
    }

    /**
     * Set the Cassandra keyspace
     * @param keyspace the keyspace to set
     */
    public void setKeyspace(String keyspace) {
        this.keyspace = keyspace;
    }

    /**
     * Get the name of the table used to store the long/bid data matrix
     * @return - name of the long/bid table
     */
    public String getLongTableName() {
        return longTableName;
    }

    /**
     * Set the name of the long/bid table (do not include keyspace)
     * @param longTableName - name of the long/bid table
     */
    public void setLongTableName(String longTableName) {
        this.longTableName = longTableName;
    }

    /**
     * Get the name of the table used to store the short/ask data matrix
     * @return the name of the short/ask table
     */
    public String getShortTableName() {
        return shortTableName;
    }

    /**
     * Set the name of the short/ask table (do not include keyspace)
     * @param shortTableName - name of the short/ask table
     */
    public void setShortTableName(String shortTableName) {
        this.shortTableName = shortTableName;
    }

    /**
     * Get the name of the table used to store prediction results
     * @return the name of the results table
     */
    public String getResultsTableName() {
        return resultsTableName;
    }

    /**
     * Set the name of the table used to store prediction results (do not include keyspace)
     * @param resultsTableName - the name of the results table
     */
    public void setResultsTableName(String resultsTableName) {
        this.resultsTableName = resultsTableName;
    }
}
