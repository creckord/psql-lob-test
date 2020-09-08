package org.example.hibernate.pgsql.clob;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class TestDataRepositoryImpl implements TestDataRepositoryAdapter {

    private static final String NATIVE_LO_QUERY = "select max(length(lo_get(cast(payload as bigint)))) from test_entity";
    private static final String NATIVE_TEXT_QUERY = "select max(length(payload)) from test_entity";

    @Autowired
    private TestDataRepository repository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private TestConfig config;

    final Random random = new Random(42L);
    byte[] randomData;

    @PostConstruct
    private void init() {
        randomData = generateUtf8Bytes(random);
    }

    @Transactional
    public void generateData() {
        final int sampleSize = config.getEntries();
        final int payloadLength = config.getLength();

        for (int i = 0; i < sampleSize; i++) {
            String payload = new String(randomData, random.nextInt(randomData.length - payloadLength), 320, StandardCharsets.UTF_8);
            TestEntity entity = new TestEntity();
            entity.setPayload(payload);
            repository.save(entity);
        }
    }

    private byte[] generateUtf8Bytes(Random random) {
        byte[] randomData = new byte[config.getLength() * 1000];
        random.nextBytes(randomData);
        final int printableOffset = 32;
        final int printableModulo = 125 - printableOffset + 1;
        for (int i = 0; i < randomData.length; i++) {
            randomData[i] = (byte) (((randomData[i] & 0xff) + printableModulo - printableOffset) % printableModulo + printableOffset);
        }
        return randomData;
    }

    @Transactional
    public void clearSession() {
        entityManager.clear();
    }

    @Transactional
    public void truncate() {
        repository.deleteAll();
        entityManager.flush();
        clearSession();
    }

    @Transactional
    public int queryData() {
        int maxLength = 0;
        for (TestEntity e : repository.findAll()) {
            maxLength = Math.max(maxLength, e.getPayload().length());
        }
        return maxLength;
    }

    @Transactional
    public int nativeQueryData() {
        return (Integer)entityManager
                .createNativeQuery(config.isUseTextDialect() ? NATIVE_TEXT_QUERY : NATIVE_LO_QUERY)
                .getSingleResult();
    }

}
