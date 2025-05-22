package com.ruerpc;

/**
 * @author Rue
 * @date 2025/5/20 13:03
 */
public class RegistryConfig {

    private String connectionString;

    public RegistryConfig(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }
}
