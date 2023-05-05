package de.unistuttgart.iste.gits.courseservice.persistence.mapper;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChapterMapper {

    private final ModelMapper modelMapper;

    public ChapterDto entityToDto(ChapterEntity chapterEntity) {
        return modelMapper.map(chapterEntity, ChapterDto.class);
    }

    public ChapterEntity dtoToEntity(CreateChapterInputDto chapterInputDto) {
        ChapterEntity entity = modelMapper.map(chapterInputDto, ChapterEntity.class);

        // set reference to course
        CourseEntity course = CourseEntity.builder().id(chapterInputDto.getCourseId()).build();
        entity.setCourse(course);

        return entity;
    }

    public ChapterEntity dtoToEntity(UpdateChapterInputDto input) {
        return modelMapper.map(input, ChapterEntity.class);
    }
}
