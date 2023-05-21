package de.unistuttgart.iste.gits.template.service;

import de.unistuttgart.iste.gits.generated.dto.TemplateDto;
import de.unistuttgart.iste.gits.template.persistence.dao.TemplateEntity;
import de.unistuttgart.iste.gits.template.persistence.mapper.TemplateMapper;
import de.unistuttgart.iste.gits.template.persistence.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final TemplateMapper templateMapper;

    public List<TemplateDto> getAllTemplates() {
        List<TemplateEntity> templates = templateRepository.findAll();
        return templates.stream().map(templateMapper::entityToDto).toList();
    }

}
