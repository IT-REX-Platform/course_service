package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.GlobalPermissionAccessValidator;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.common.user_handling.UserCourseAccessValidator;
import de.unistuttgart.iste.gits.course_service.service.CourseService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(final CourseService courseService) {
        this.courseService = courseService;
    }

    @BatchMapping(typeName = "CourseMembership")
    public Map<CourseMembership, Course> course(final List<CourseMembership> courseMemberships) {
        return courseService.getCoursesByCourseMemberships(courseMemberships);
    }

    @QueryMapping
    public CoursePayload courses(
            @Argument(name = "filter") @Nullable final CourseFilter filter,
            @Argument(name = "sortBy") final List<String> sortBy,
            @Argument(name = "sortDirection") final List<SortDirection> sortDirection,
            @Argument(name = "pagination") @Nullable final Pagination pagination
    ) {
        return courseService.getCourses(filter, sortBy, sortDirection, pagination);
    }

    @QueryMapping
    public List<Course> coursesByIds(@Argument(name = "ids") final List<UUID> ids) {
        return courseService.getCoursesByIds(ids);
    }

    @MutationMapping
    public Course createCourse(@Argument(name = "input") final CreateCourseInput input,
                               @ContextValue final LoggedInUser currentUser){
        GlobalPermissionAccessValidator.validateUserHasGlobalPermission(currentUser, Set.of(LoggedInUser.RealmRole.COURSE_CREATOR));

        final Course course = courseService.createCourse(input, currentUser.getId());

        // update user course memberships in context with the newly created course (the creator of the course
        // always gets admin permissions)
        currentUser.getCourseMemberships().add(new LoggedInUser.CourseMembership(
                course.getId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                course.getPublished(),
                course.getStartDate(),
                course.getEndDate()));

        return course;
    }

    @MutationMapping
    public Course updateCourse(@Argument(name = "input") final UpdateCourseInput input,
                               @ContextValue final LoggedInUser currentUser) {
        UserCourseAccessValidator.validateUserHasAccessToCourse(currentUser,
                                                                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                                                                input.getId());

        return courseService.updateCourse(input);
    }

    @MutationMapping
    public UUID deleteCourse(@Argument(name = "id") final UUID id,
                             @ContextValue final LoggedInUser currentUser) {
        UserCourseAccessValidator.validateUserHasAccessToCourse(currentUser,
                                                                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                                                                id);

        return courseService.deleteCourse(id);
    }

}
