package de.unistuttgart.iste.gits.courseservice.persistence.repository;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {


}
