package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.service.CourseService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(final CourseService courseService) {
        this.courseService = courseService;
    }

    @SchemaMapping
    public Course course(final CourseMembership courseMembership) {
        return courseService.getCourseById(courseMembership.getCourseId());
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
    public Course createCourse(@Argument(name = "input") final CreateCourseInput input, @ContextValue final LoggedInUser currentUser) {
        return courseService.createCourse(input, currentUser.getId());
    }

    @MutationMapping
    public Course updateCourse(@Argument(name = "input") final UpdateCourseInput input) {
        return courseService.updateCourse(input);
    }

    @MutationMapping
    public UUID deleteCourse(@Argument(name = "id") final UUID id) {
        return courseService.deleteCourse(id);
    }

}
