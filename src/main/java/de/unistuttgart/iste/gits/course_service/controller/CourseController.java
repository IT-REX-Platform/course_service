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
    public CoursePayloadDto courses(
            @Argument(name = "filter") @Nullable CourseFilterDto filter,
            @Argument(name = "sortBy") List<String> sortBy,
            @Argument(name = "sortDirection") List<SortDirectionDto> sortDirection,
            @Argument(name = "pagination") @Nullable PaginationDto pagination
    ) {
        return courseService.getCourses(filter, sortBy, sortDirection, pagination);
    }

    @QueryMapping
    public List<CourseDto> coursesById(@Argument(name = "ids") List<UUID> ids) {
        return courseService.getCoursesByIds(ids);
    }

    @MutationMapping
    public CourseDto createCourse(@Argument(name = "input") CreateCourseInputDto input) {
        return courseService.createCourse(input);
    }

    @MutationMapping
    public CourseDto updateCourse(@Argument(name = "input") UpdateCourseInputDto input) {
        return courseService.updateCourse(input);
    }

    @MutationMapping
    public UUID deleteCourse(@Argument(name = "id") UUID id) {
        return courseService.deleteCourse(id);
    }

}
