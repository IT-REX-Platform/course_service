package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.course_service.service.CourseService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.*;

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
    public Course createCourse(@Argument(name = "input") final CreateCourseInput input) {
        return courseService.createCourse(input);
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
