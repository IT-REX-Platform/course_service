package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.course_service.persistence.entity.*;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.MembershipMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class MembershipService {

    private final CourseMembershipRepository courseMembershipRepository;

    private final CourseRepository courseRepository;

    private final MembershipMapper membershipMapper;

    private final CourseMapper courseMapper;

    /**
     * Returns all memberships of a user
     *
     * @param userId             ID of the user
     * @param availabilityFilter Filter field to filter for available or unavailable courses.
     *                           If this field is true, only available courses are returned.
     *                           If this field is false, only unavailable courses are returned.
     *                           If this field is null, all courses are returned.
     * @return List of memberships
     */
    public List<CourseMembership> getAllMembershipByUserId(final UUID userId, final Boolean availabilityFilter) {
        return courseMembershipRepository.findByUserId(userId)
                .stream()
                .map(membershipMapper::entityToDto)
                .filter(byAvailability(availabilityFilter))
                .toList();
    }

    /**
     * creates a new course membership
     *
     * @return created entity
     */
    public CourseMembership createMembership(final CourseMembershipInput inputDto) {
        if (!courseRepository.existsById(inputDto.getCourseId())) {
            throw new EntityNotFoundException("Course with id " + inputDto.getCourseId() + " not found");
        }

        final CourseMembershipEntity entity = courseMembershipRepository.save(membershipMapper.dtoToEntity(inputDto));

        return membershipMapper.entityToDto(entity);
    }

    /**
     * Updates the role of a user in a course
     *
     * @param inputDto contains user ID, course ID, and course role
     * @return updated entity
     */
    public CourseMembership updateMembershipRole(final CourseMembershipInput inputDto) {

        //make sure entity exists in database
        requireMembershipExisting(new CourseMembershipPk(inputDto.getUserId(), inputDto.getCourseId()));

        final CourseMembershipEntity entity = courseMembershipRepository.save(membershipMapper.dtoToEntity(inputDto));
        return membershipMapper.entityToDto(entity);
    }

    /**
     * deletes a course membership of a user
     *
     * @return deleted entity
     */
    public CourseMembership deleteMembership(final UUID userId, final UUID courseId) {
        final CourseMembershipPk membershipPk = new CourseMembershipPk(userId, courseId);

        final CourseMembershipEntity entity = requireMembershipExisting(membershipPk);
        courseMembershipRepository.deleteById(membershipPk);

        return membershipMapper.entityToDto(entity);
    }

    /**
     * removes all memberships for a given course ID from the database
     *
     * @param courseId valid course ID
     */
    public void deleteMembershipByCourseId(final UUID courseId) {

        final List<CourseMembershipEntity> memberships = courseMembershipRepository.findCourseMembershipEntitiesByCourseId(courseId);

        if (memberships != null && !memberships.isEmpty()) {
            courseMembershipRepository.deleteAll(memberships);
        }
    }

    /**
     * Returns all memberships of a course
     *
     * @param courseId ID of the course
     * @return List of memberships
     */
    public List<CourseMembership> getMembershipsOfCourse(final UUID courseId) {
        return courseMembershipRepository.findCourseMembershipEntitiesByCourseId(courseId)
                .stream()
                .map(membershipMapper::entityToDto)
                .toList();
    }

    private Predicate<? super CourseMembership> byAvailability(final Boolean availableCoursesFilter) {
        return membership -> {
            if (availableCoursesFilter == null) {
                // no filter
                return true;
            }
            return availableCoursesFilter == isAvailable(membership);
        };
    }

    private boolean isAvailable(final CourseMembership membership) {
        final Course course = getCourseById(membership.getCourseId());
        if (!course.getPublished()) {
            // course is not published
            return false;
        }

        final OffsetDateTime now = OffsetDateTime.now();
        final OffsetDateTime startDate = course.getStartDate();
        final OffsetDateTime endDate = course.getEndDate();
        return startDate.isBefore(now) && endDate.isAfter(now);
    }

    private Course getCourseById(final UUID courseId) {
        final CourseEntity entity = courseRepository.getReferenceById(courseId);
        return courseMapper.entityToDto(entity);
    }

    /**
     * Helper function to validate existence of an entity in the database
     *
     * @param membershipPk Primary key of entity to be checked
     */
    private CourseMembershipEntity requireMembershipExisting(final CourseMembershipPk membershipPk) {
        return courseMembershipRepository.findById(membershipPk)
                .orElseThrow(() -> new EntityNotFoundException("Membership with user id "
                                                               + membershipPk.getUserId()
                                                               + " and course id "
                                                               + membershipPk.getCourseId()
                                                               + " not found"));
    }
}
