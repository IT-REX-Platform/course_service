package de.unistuttgart.iste.gits.template.persistence.mapper;

import de.unistuttgart.iste.gits.generated.dto.Template;
import de.unistuttgart.iste.gits.template.persistence.dao.TemplateEntity;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TemplateMapper {

    private final ModelMapper modelMapper;

    public Template entityToDto(TemplateEntity templateEntity) {
        // add specific mapping here if needed
        return modelMapper.map(templateEntity, Template.class);
    }

    public TemplateEntity dtoToEntity(Template template) {
        // add specific mapping here if needed
        return modelMapper.map(template, TemplateEntity.class);
    }
}
