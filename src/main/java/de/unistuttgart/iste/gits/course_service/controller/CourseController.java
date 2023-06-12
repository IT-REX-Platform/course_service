package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.course_service.service.CourseService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @QueryMapping
    public CoursePayload courses(
            @Argument(name = "filter") @Nullable CourseFilter filter,
            @Argument(name = "sortBy") List<String> sortBy,
            @Argument(name = "sortDirection") List<SortDirection> sortDirection,
            @Argument(name = "pagination") @Nullable Pagination pagination
    ) {
        return courseService.getCourses(filter, sortBy, sortDirection, pagination);
    }

    @QueryMapping
    public List<Course> coursesById(@Argument(name = "ids") List<UUID> ids) {
        return courseService.getCoursesByIds(ids);
    }

    @MutationMapping
    public Course createCourse(@Argument(name = "input") CreateCourseInput input) {
        return courseService.createCourse(input);
    }

    @MutationMapping
    public Course updateCourse(@Argument(name = "input") UpdateCourseInput input) {
        return courseService.updateCourse(input);
    }

    @MutationMapping
    public UUID deleteCourse(@Argument(name = "id") UUID id) {
        return courseService.deleteCourse(id);
    }

}
