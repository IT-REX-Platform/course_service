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
     * Finds all course memberships for the specified users. Returns a list of CourseMembershipEntities for the given
     * user ids in no particular order.
     *
     * @param userIds IDs of the users to find their courses for.
     * @return List of CourseMembershipEntities for the users with the given ids.
     */
    List<CourseMembershipEntity> findByUserIdIn(List<UUID> userIds);

    /**
     * Finds all course memberships of the course with the specified id.
     *
     * @param courseId ID of the course to find the memberships for.
     * @return List of CourseMembershipEntities for the course with the given id.
     */
    List<CourseMembershipEntity> findCourseMembershipEntitiesByCourseId(UUID courseId);
}
