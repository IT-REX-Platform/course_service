package de.unistuttgart.iste.gits.template.controller;

import de.unistuttgart.iste.gits.template.dto.TemplateDTO;
import de.unistuttgart.iste.gits.template.service.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
public class TemplateController {

    private final TemplateService templateService;

    public TemplateController(TemplateService templateService) {
        this.templateService = templateService;
    }

    @QueryMapping
    public List<TemplateDTO> templates() {
        log.info("Request for all templates");

        return templateService.getAllTemplates();
    }
}
