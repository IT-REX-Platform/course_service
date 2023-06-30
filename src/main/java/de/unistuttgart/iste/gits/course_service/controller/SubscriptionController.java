package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.common.event.CourseAssociationEvent;
import de.unistuttgart.iste.gits.course_service.service.ResourceService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * REST Controller Class listening to a dapr Topic.
 */
@Slf4j
@RestController
public class SubscriptionController {

    private final ResourceService resourceService;

    public SubscriptionController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @Topic(name = "resource-association", pubsubName = "gits")
    @PostMapping(path = "/course-service/resource-association-pubsub")
    public Mono<Void> updateAssociation(@RequestBody(required = false) CloudEvent<CourseAssociationEvent> cloudEvent, @RequestHeader Map<String, String> headers){

            return Mono.fromRunnable( () -> resourceService.updateResourceAssociations(cloudEvent.getData()));
    }
}
