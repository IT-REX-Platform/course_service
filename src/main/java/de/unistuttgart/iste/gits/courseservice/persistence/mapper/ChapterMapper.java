package de.unistuttgart.iste.gits.courseservice.persistence.mapper;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ChapterMapper {

    private final ModelMapper modelMapper;

    public ChapterMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public ChapterDto mapEntityToDto(ChapterEntity chapterEntity) {
        return modelMapper.map(chapterEntity, ChapterDto.class);
    }

    public ChapterEntity mapInputDtoToEntity(CreateChapterInputDto chapterInputDTO) {
        return modelMapper.map(chapterInputDTO, ChapterEntity.class);
    }

    public ChapterEntity mapInputDtoToEntity(UpdateChapterInputDto input) {
        return modelMapper.map(input, ChapterEntity.class);
    }
}
