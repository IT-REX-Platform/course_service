package de.unistuttgart.iste.gits.courseservice.persistence.repository;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for {@link CourseEntity}.
 */
@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID>, JpaSpecificationExecutor<CourseEntity> {

}
