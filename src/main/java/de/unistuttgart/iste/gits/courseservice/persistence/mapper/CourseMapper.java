package de.unistuttgart.iste.gits.courseservice.persistence.mapper;

import de.unistuttgart.iste.gits.courseservice.dto.*;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
public class CourseMapper {

    private final ModelMapper modelMapper;

    public CourseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CourseDto entityToDto(CourseEntity courseEntity) {
        return modelMapper.map(courseEntity, CourseDto.class);
    }

    public CourseEntity dtoToEntity(CreateCourseInputDto courseInputDTO) {
        return modelMapper.map(courseInputDTO, CourseEntity.class);
    }

    public CourseEntity dtoToEntity(UpdateCourseInputDto input) {
        return modelMapper.map(input, CourseEntity.class);
    }

    public CoursePayloadDto createPayloadDto(Stream<CourseEntity> courseEntities,
                                             PaginationInfoDto paginationInfoDto) {
        return CoursePayloadDto.builder()
                .setElements(courseEntities.map(this::entityToDto).toList())
                .setPagination(paginationInfoDto)
                .build();
    }
}
