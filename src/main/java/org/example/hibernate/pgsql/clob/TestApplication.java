package org.example.hibernate.pgsql.clob;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Types;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class TestApplication {

    private static final Logger log = LoggerFactory.getLogger(TestApplication.class);

    public static void main(String[] args) {
        int exitCode = 1;
        final ConfigurableApplicationContext appContext = SpringApplication.run(TestApplication.class, args);
        appContext.start();
        try {
            final TestApplication app = appContext.getBean(TestApplication.class);
            app.work();
            exitCode = 0;
        } finally {
            try {
                appContext.stop();
            } finally {
                System.exit(exitCode);
            }
        }
    }

    private final TestDataRepository repository;

    private final TestConfig config;

    @Autowired
    public TestApplication(TestConfig config, TestDataRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    public void work() {
        long total = 0;
        final int iterations = config.getIterations();
        for (int i = 0; i < iterations; i++) {
            log.info("Run {}:", i);
            repository.truncate();
            long now = System.nanoTime();
            workOnce();
            long took = System.nanoTime() - now;
            total += took;
            log.info("... took {} ms", TimeUnit.NANOSECONDS.toMillis(took));
        }
        log.info("Total {} ms", TimeUnit.NANOSECONDS.toMillis(total));
        log.info("Avg {} ms", TimeUnit.NANOSECONDS.toMillis(total / iterations));
    }

    private void workOnce() {
        long now = System.nanoTime();
        repository.generateData();
        long generateDuration = System.nanoTime() - now;
        log.info("    ORM write took {} ms", TimeUnit.NANOSECONDS.toMillis(generateDuration));

        now = System.nanoTime();
        final int lengthOrm = repository.queryData();
        long ormReadDuration = System.nanoTime() - now;
        log.info("    ORM read took {} ms", TimeUnit.NANOSECONDS.toMillis(ormReadDuration));
        log.info("        result: {}", lengthOrm);

        now = System.nanoTime();
        final int lengthNative = repository.queryData();
        long nativeReadDuration = System.nanoTime() - now;
        log.info("    Native read took {} ms", TimeUnit.NANOSECONDS.toMillis(nativeReadDuration));
        log.info("        result: {}", lengthNative);
    }

    public static class PGSQLClobToTextDialect extends PostgreSQL10Dialect {

        @Override
        public SqlTypeDescriptor remapSqlTypeDescriptor(SqlTypeDescriptor sqlTypeDescriptor) {
            if (Types.CLOB == sqlTypeDescriptor.getSqlType()) {
                return LongVarcharTypeDescriptor.INSTANCE;
            }
            return super.remapSqlTypeDescriptor(sqlTypeDescriptor);
        }
    }
}
