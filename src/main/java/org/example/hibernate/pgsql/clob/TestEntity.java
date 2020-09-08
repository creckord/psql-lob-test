package org.example.hibernate.pgsql.clob;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity(name = TestEntity.ENTITY_NAME)
@Table(name = TestEntity.TABLE_NAME)
public class TestEntity {

    public static final String GENERATOR_NAME = "default_id_generator";
    public static final String SEQUENCE_NAME = "test_seq";

    public static final String TABLE_NAME = "test_entity";
    public static final String ENTITY_NAME = "TestEntity";

    private Long id;

    private String payload;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = GENERATOR_NAME)
    @SequenceGenerator(name = GENERATOR_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 100)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Lob
    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
