package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.validation.CourseValidator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service that handles course related operations.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;
    private final CourseValidator courseValidator;

    /**
     * Returns all courses.
     *
     * @return All courses.
     */
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::entityToDto).toList();
    }

    /**
     * Creates a course.
     *
     * @param courseInputDto The data of the course to create.
     * @return The created course.
     */
    public CourseDto createCourse(CreateCourseInputDto courseInputDto) {
        courseValidator.validateCreateCourseInputDto(courseInputDto);

        CourseEntity courseEntity = courseRepository.save(courseMapper.dtoToEntity(courseInputDto));

        return courseMapper.entityToDto(courseEntity);
    }

    /**
     * Updates a course.
     *
     * @param input The data of the course to update.
     * @return The updated course.
     */
    public CourseDto updateCourse(UpdateCourseInputDto input) {
        courseValidator.validateUpdateCourseInputDto(input);
        requireCourseExisting(input.getId());

        CourseEntity updatedCourseEntity = courseRepository.save(courseMapper.dtoToEntity(input));

        return courseMapper.entityToDto(updatedCourseEntity);
    }

    /**
     * Deletes a course.
     *
     * @param uuid The id of the course to delete.
     * @return The id of the deleted course.
     * @throws EntityNotFoundException If a course with the given id does not exist.
     */
    public UUID deleteCourse(UUID uuid) {
        requireCourseExisting(uuid);
        courseRepository.deleteById(uuid);
        return uuid;
    }

    /**
     * Returns a list of courses by their ids.
     *
     * @param ids The ids of the courses to return.
     * @return A list of courses with the given ids, preserving the order of the ids.
     * @throws EntityNotFoundException If a course with at least one of the given ids does not exist.
     */
    public List<CourseDto> getCoursesByIds(List<UUID> ids) {
        var result = new ArrayList<CourseDto>(ids.size());
        var missingIds = new ArrayList<UUID>();

        for (var id : ids) {
            courseRepository.findById(id)
                    .ifPresentOrElse(
                            courseEntity -> result.add(courseMapper.entityToDto(courseEntity)),
                            () -> missingIds.add(id));
        }

        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException("Course(s) with id(s) "
                    + missingIds.stream().map(UUID::toString).collect(Collectors.joining(", "))
                    + " not found");
        }

        return result;
    }

    /**
     * Checks if a course with the given id exists. If not, an EntityNotFoundException is thrown.
     * @param id The id of the course to check.
     * @throws EntityNotFoundException If a course with the given id does not exist.
     */
    public void requireCourseExisting(UUID id) {
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course with id " + id + " not found");
        }
    }
}
