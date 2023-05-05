package de.unistuttgart.iste.gits.courseservice.persistence.mapper;

import de.unistuttgart.iste.gits.courseservice.dto.CourseDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateCourseInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CourseMapper {

    private final ModelMapper modelMapper;

    public CourseMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public CourseDto entityToDto(CourseEntity courseEntity) {
        CourseDto courseDto = modelMapper.map(courseEntity, CourseDto.class);
        if (courseDto.getChapters() == null) {
            courseDto.setChapters(Collections.emptyList());
        }
        return courseDto;
    }

    public CourseEntity dtoToEntity(CreateCourseInputDto courseInputDTO) {
        return modelMapper.map(courseInputDTO, CourseEntity.class);
    }

    public CourseEntity dtoToEntity(UpdateCourseInputDto input) {
        return modelMapper.map(input, CourseEntity.class);
    }
}
