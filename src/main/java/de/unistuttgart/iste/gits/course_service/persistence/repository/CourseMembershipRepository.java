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
     * Finds all course memberships for the specified user.
     *
     * @param userId ID of the user to find their courses for.
     * @return List of CourseMembershipEntities for the users with the given id.
     */
    List<CourseMembershipEntity> findByUserId(UUID userId);

    /**
     * Finds all course memberships of the course with the specified id.
     *
     * @param courseId ID of the course to find the memberships for.
     * @return List of CourseMembershipEntities for the course with the given id.
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByCourseId(UUID courseId);
}
