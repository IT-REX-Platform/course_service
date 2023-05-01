package de.unistuttgart.iste.gits.template.service;

import de.unistuttgart.iste.gits.template.dto.TemplateDTO;
import de.unistuttgart.iste.gits.template.persistence.dao.TemplateEntity;
import de.unistuttgart.iste.gits.template.persistence.repository.TemplateRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateService {

    private final TemplateRepository templateRepository;
    private final ModelMapper modelMapper;

    public TemplateService(TemplateRepository templateRepository, ModelMapper modelMapper) {
        this.templateRepository = templateRepository;
        this.modelMapper = modelMapper;
    }

    public List<TemplateDTO> getAllTemplates() {
        List<TemplateEntity> templates = templateRepository.findAll();
        return templates.stream().map(this::convertEntityToDto).toList();
    }

    private TemplateDTO convertEntityToDto(TemplateEntity templateEntity) {
        // add specific mapping here if needed
        return modelMapper.map(templateEntity, TemplateDTO.class);
    }
}
