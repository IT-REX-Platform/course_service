package de.unistuttgart.iste.gits.course_service.test_config;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockTopicPublisherConfiguration {


    @Primary
    @Bean
    public TopicPublisher getTestTopicPublisher() {
        TopicPublisher mockPublisher = mock(TopicPublisher.class);
        doNothing().when(mockPublisher).notifyCourseChanges(any(UUID.class), any(CrudOperation.class));
        doNothing().when(mockPublisher).notifyChapterChanges(any(), any(CrudOperation.class));
        return mockPublisher;
    }
}
