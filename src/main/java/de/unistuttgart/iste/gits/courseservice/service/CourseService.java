package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    public List<CourseDto> getAllCourses() {
        return courseRepository.findAll().stream().map(courseMapper::mapEntityToDto).toList();
    }

    public CourseDto createCourse(CreateCourseInputDto courseInputDTO) {
        CourseEntity courseEntity = courseMapper.mapInputDtoToEntity(courseInputDTO);
        courseEntity = courseRepository.save(courseEntity);
        return courseMapper.mapEntityToDto(courseEntity);
    }

    public CourseDto updateCourse(UpdateCourseInputDto input) {
        UUID id = UUID.fromString(input.getDescription());
        if (!courseRepository.existsById(id)) {
            throw new EntityNotFoundException("Course with id " + id + " not found");
        }
        CourseEntity updatedCourseEntity = courseMapper.mapInputDtoToEntity(input);
        updatedCourseEntity.setId(id);
        updatedCourseEntity = courseRepository.save(updatedCourseEntity);
        return courseMapper.mapEntityToDto(updatedCourseEntity);
    }

    public Optional<UUID> deleteCourse(String id) {
        UUID uuid = UUID.fromString(id);
        if (!courseRepository.existsById(uuid)) {
            return Optional.empty();
        }
        courseRepository.deleteById(uuid);
        return Optional.of(uuid);
    }

    public List<CourseDto> getCoursesByIds(List<String> ids) {
        List<UUID> uuids = ids.stream().map(UUID::fromString).toList();
        return courseRepository
                .findByIdIn(uuids)
                .stream()
                .map(courseMapper::mapEntityToDto)
                .toList();
    }
}
