package de.unistuttgart.iste.gits.course_service.persistence.repository;

import de.unistuttgart.iste.gits.common.persistence.GitsRepository;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

/**
 * Repository for {@link ChapterEntity}.
 */
public interface ChapterRepository extends GitsRepository<ChapterEntity, UUID>, JpaSpecificationExecutor<ChapterEntity> {


}
