package de.unistuttgart.iste.gits.template.persistence.mapper;

import de.unistuttgart.iste.gits.generated.dto.TemplateDto;
import de.unistuttgart.iste.gits.template.persistence.dao.TemplateEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateMapper {

    private final ModelMapper modelMapper;

    public TemplateDto entityToDto(TemplateEntity templateEntity) {
        // add specific mapping here if needed
        return modelMapper.map(templateEntity, TemplateDto.class);
    }

    public TemplateEntity dtoToEntity(TemplateDto templateDto) {
        // add specific mapping here if needed
        return modelMapper.map(templateDto, TemplateEntity.class);
    }
}
