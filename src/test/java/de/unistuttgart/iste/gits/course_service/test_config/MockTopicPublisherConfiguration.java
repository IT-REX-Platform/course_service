package de.unistuttgart.iste.gits.course_service.test_config;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;
import java.util.UUID;

@TestConfiguration
public class MockTopicPublisherConfiguration {


    @Primary
    @Bean
    public TopicPublisher getTestTopicPublisher() {
        TopicPublisher mockPublisher = Mockito.mock(TopicPublisher.class);
        Mockito.doNothing().when(mockPublisher).notifyCourseChanges(Mockito.any(UUID.class), Mockito.any(CrudOperation.class));
        Mockito.doNothing().when(mockPublisher).notifyChapterChanges(Mockito.any(List.class), Mockito.any(CrudOperation.class));
        return mockPublisher;
    }
}
