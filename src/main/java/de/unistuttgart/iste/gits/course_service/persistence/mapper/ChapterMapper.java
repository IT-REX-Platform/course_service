package de.unistuttgart.iste.gits.course_service.persistence.mapper;

import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class ChapterMapper {

    private final ModelMapper modelMapper;

    public Chapter entityToDto(ChapterEntity chapterEntity) {
        return modelMapper.map(chapterEntity, Chapter.class);
    }

    public ChapterEntity dtoToEntity(CreateChapterInput chapterInput) {
        ChapterEntity entity = modelMapper.map(chapterInput, ChapterEntity.class);
        entity.setCourseId(chapterInput.getCourseId());
        return entity;
    }

    public ChapterEntity dtoToEntity(UpdateChapterInput input) {
        return modelMapper.map(input, ChapterEntity.class);
    }

    public ChapterPayload createChapterPayload(Stream<ChapterEntity> chapterEntities, PaginationInfo paginationInfo) {
        return ChapterPayload.builder()
                .setElements(chapterEntities.map(this::entityToDto).toList())
                .setPagination(paginationInfo)
                .build();
    }
}
