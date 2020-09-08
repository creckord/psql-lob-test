package org.example.hibernate.pgsql.clob;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("test")
public class TestConfig {

    @Value("${spring.jpa.database-platform:}")
    private String dialect;

    private int iterations;

    private int length;

    private int entries;

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getEntries() {
        return entries;
    }

    public void setEntries(int entries) {
        this.entries = entries;
    }

    public boolean isUseTextDialect() {
        return "org.example.hibernate.pgsql.clob.TestApplication$PGSQLClobToTextDialect".equals(dialect);
    }
}
