package com.lsi.oracle.Controller.DTO.Request;

/**
 * DTO for configuring VPD policies.
 */
public class VPDPolicyRequest {
    private String policyName;       // Name of the VPD policy
    private String tableName;        // Table to apply the VPD policy
    private String policyFunction;   // The function that defines the policy
    private String predicate;        // SQL predicate to filter rows (optional, if applicable)
    private String schemaName;       // Optional schema name for dynamic schema configuration

    // Getters and Setters
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getPolicyFunction() {
        return policyFunction;
    }

    public void setPolicyFunction(String policyFunction) {
        this.policyFunction = policyFunction;
    }

    public String getPredicate() {
        return predicate;
    }

    public void setPredicate(String predicate) {
        this.predicate = predicate;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
}
