package de.unistuttgart.iste.gits.course_service.config;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * This is a dev-config for the TopicPublisher. It is intended to be used for development only.
 * It will log all messages instead of sending them to the dapr topic.
 * <p>
 * The purpose of this is to allow developers to work on the media-service without having to run the dapr runtime.
 * <p>
 * To use this config, set the spring profile to "dev" (or any other profile that is not "prod").
 */
@Configuration
@Profile("!prod")
@Slf4j
public class DevTopicPublisherConfiguration {

    @Bean
    public TopicPublisher getTopicPublisher() {
        log.warn("TopicPublisher is mocked. This is intended for development use only.");
        return new MockTopicPublisher();
    }

    private static class MockTopicPublisher extends TopicPublisher {

        public MockTopicPublisher() {
            super(null);
        }

        @Override
        public void notifyCourseChanges(UUID courseID, CrudOperation operation) {
            log.info("notifyChange called with {} and {}", courseID, operation);
        }

        @Override
        public void notifyChapterChanges(List<UUID> chapterIds, CrudOperation operation) {
            log.info("notifyChange called with {} and {}", chapterIds, operation);
        }
    }
}
