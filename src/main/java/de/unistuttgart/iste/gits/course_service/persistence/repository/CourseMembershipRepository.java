package de.unistuttgart.iste.gits.course_service.persistence.repository;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link CourseMembershipEntity}
 */
@Repository
public interface CourseMembershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    /**
     * Hibernate Query. Find Entities by User ID. ORDERED BY Course ID
     * @param userId User ID
     * @return List of Entities
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByUserIdOrderByCourseId(UUID userId);

    /**
     * Hibernate Query. Find Entities by Course ID. ORDERED BY User ID
     *
     * @param courseId Course ID
     * @return List of Entities
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByCourseId(UUID courseId);
}
