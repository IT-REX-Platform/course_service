package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.common.event.CourseAssociationEvent;
import de.unistuttgart.iste.gits.common.event.CourseChangeEvent;
import de.unistuttgart.iste.gits.course_service.service.CourseResourceAssociationService;
import de.unistuttgart.iste.gits.course_service.service.MembershipService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST Controller Class listening to a dapr Topic.
 */
@Slf4j
@RestController
public class SubscriptionController {

    private final CourseResourceAssociationService resourceService;

    private final MembershipService membershipService;

    public SubscriptionController(final CourseResourceAssociationService resourceService, final MembershipService membershipService) {
        this.resourceService = resourceService;
        this.membershipService = membershipService;
    }

    @Topic(name = "resource-association", pubsubName = "gits")
    @PostMapping(path = "/course-service/resource-association-pubsub")
    public Mono<Void> updateAssociation(@RequestBody final CloudEvent<CourseAssociationEvent> cloudEvent) {

        return Mono.fromRunnable(() -> {
            try {
                resourceService.updateResourceAssociations(cloudEvent.getData());
            } catch (final Exception e) {
                log.error("Error while processing resource-association event. {}", e.getMessage());
            }
        });
    }

    @Topic(name = "course-changes", pubsubName = "gits")
    @PostMapping(path = "/user-service/course-changes-pubsub")
    public Mono<Void> updateCourseAssociation(@RequestBody final CloudEvent<CourseChangeEvent> cloudEvent) {
        log.info("Received course change event: {}", cloudEvent.getData());
        return Mono.fromRunnable(
                () -> {
                    try {
                        membershipService.removeCourse(cloudEvent.getData());
                    } catch (final Exception e) {
                        log.error("Error while processing course-changes event. {}", e.getMessage());
                    }
                });
    }
}
