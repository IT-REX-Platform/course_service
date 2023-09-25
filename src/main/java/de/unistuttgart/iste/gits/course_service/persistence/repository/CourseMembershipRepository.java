package de.unistuttgart.iste.gits.course_service.persistence.repository;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipPk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link CourseMembershipEntity}
 */
@Repository
public interface CourseMembershipRepository extends JpaRepository<CourseMembershipEntity, CourseMembershipPk> {

    /**
     * Find Courses by CourseID
     *
     * @param userIds of the courses to find
     * @return List of CourseMembershipEntities
     */
    List<CourseMembershipEntity> findByUserIdIn(List<UUID> userIds);

    /**
     * Hibernate Query. Find Entities by Course ID. ORDERED BY User ID
     *
     * @param courseId Course ID
     * @return List of Entities
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByCourseId(UUID courseId);
}
