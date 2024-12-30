package com.lsi.oracle.Controller.DTO.Request;

/**
 * DTO for configuring encryption policies.
 */
public class EncryptionPolicyRequest {
    private String tableName;          // The table to encrypt
    private String columnName;         // The column to encrypt
    private String encryptionAlgorithm; // The encryption algorithm to use (e.g., AES256)

    // Getters and Setters
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }
}
