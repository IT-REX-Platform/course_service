package de.unistuttgart.iste.gits.course_service.persistence.mapper;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.generated.dto.*;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class CourseMapper {

    private final ModelMapper modelMapper;

    public CourseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public Course entityToDto(CourseEntity courseEntity) {
        return modelMapper.map(courseEntity, Course.class);
    }

    public CourseEntity dtoToEntity(CreateCourseInput courseInputDTO) {
        return modelMapper.map(courseInputDTO, CourseEntity.class);
    }

    public CourseEntity dtoToEntity(UpdateCourseInput input) {
        return modelMapper.map(input, CourseEntity.class);
    }

    public CoursePayload createPayload(Stream<CourseEntity> courseEntities,
                                       PaginationInfo paginationInfo) {
        return CoursePayload.builder()
                .setElements(courseEntities.map(this::entityToDto).toList())
                .setPagination(paginationInfo)
                .build();
    }
}
