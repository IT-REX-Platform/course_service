package de.unistuttgart.iste.gits.courseservice.controller;

import de.unistuttgart.iste.gits.courseservice.service.ResourceService;
import de.unistuttgart.iste.gits.generated.dto.CourseResourceAssociationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
public class ResourceController {

    private final ResourceService resourceService;


    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @QueryMapping
    public List<CourseResourceAssociationDto> resourceById(@Argument(name="ids") List<UUID> ids){
        return resourceService.getCoursesByResourceId(ids);
    }
}
