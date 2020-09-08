package org.example.hibernate.pgsql.clob;

public interface TestDataRepositoryAdapter {
    void generateData();
    int queryData();
    int nativeQueryData();
    void truncate();
}
