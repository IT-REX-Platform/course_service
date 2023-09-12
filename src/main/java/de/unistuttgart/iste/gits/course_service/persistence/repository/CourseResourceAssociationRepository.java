package de.unistuttgart.iste.gits.course_service.persistence.repository;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseResourceAssociationEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ResourcePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link CourseResourceAssociationEntity}.
 */
@Repository
public interface CourseResourceAssociationRepository
        extends JpaRepository<CourseResourceAssociationEntity, ResourcePk>, JpaSpecificationExecutor<CourseResourceAssociationEntity> {

    /**
     * Hibernate Query. ORDERED BY Course ID ASC
     *
     * @param resourceId resource ID
     * @return a List of Resources for a resource ID
     */
    List<CourseResourceAssociationEntity> findCourseResourceAssociationEntitiesByResourceIdOrderByCourseIdAsc(UUID resourceId);
}
