package de.unistuttgart.iste.gits.courseservice.controller;

import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @QueryMapping
    public List<CourseDto> courses() {
        return courseService.getAllCourses();
    }

    @QueryMapping
    public List<CourseDto> coursesById(@Argument(name = "ids") List<String> ids) {
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
    public Optional<UUID> deleteCourse(@Argument(name = "id") String id) {
        return courseService.deleteCourse(id);
    }

}
