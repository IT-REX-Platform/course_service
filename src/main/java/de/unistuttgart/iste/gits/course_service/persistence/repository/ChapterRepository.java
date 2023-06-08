package de.unistuttgart.iste.gits.course_service.persistence.repository;

import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Repository for {@link ChapterEntity}.
 */
public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID>, JpaSpecificationExecutor<ChapterEntity> {


}
