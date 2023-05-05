package de.unistuttgart.iste.gits.courseservice.persistence.repository;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, UUID> {

    /**
     * Like {@link #findById(Object)} but throws an exception if the course does not exist.
     */
    @NonNull
    CourseEntity getById(@NonNull UUID id);

    List<CourseEntity> findByIdIn(List<UUID> ids);

}
