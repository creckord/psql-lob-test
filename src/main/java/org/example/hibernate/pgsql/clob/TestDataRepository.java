package org.example.hibernate.pgsql.clob;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDataRepository extends CrudRepository<TestEntity, Long>, TestDataRepositoryAdapter {
}
