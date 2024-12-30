package com.lsi.oracle.Controller.DTO.Request;

/**
 * DTO for configuring audit policies.
 */
public class AuditPolicyRequest {
    private String policyName;  // Name of the audit policy
    private String objectName;  // Table or schema to audit
    private String actionName;  // Action to audit (e.g., SELECT, INSERT)

    // Getters and Setters
    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
}
