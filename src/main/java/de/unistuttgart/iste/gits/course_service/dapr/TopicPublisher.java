package de.unistuttgart.iste.gits.course_service.dapr;


import de.unistuttgart.iste.gits.common.event.*;
import io.dapr.client.DaprClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

/**
 * Component that takes care of publishing messages to a dapr Topic
 */
@Slf4j
@RequiredArgsConstructor
public class TopicPublisher {

    private static final String PUBSUB_NAME = "gits";
    private static final String TOPIC_COURSE_CHANGES = "course-changes";

    private static final String TOPIC_CHAPTER_CHANGES = "chapter-changes";

    private final DaprClient client;

    /**
     * method used to publish dapr messages to a topic
     * @param dto message
     */
    private void publishChanges(Object dto, String topic){
        log.info("publishing message");
        client.publishEvent(
                PUBSUB_NAME,
                topic,
                dto).block();
    }

    /**
     * Method that creates Course Change Event Message and publishes it to a topic
     * @param courseID course that received changes
     * @param operation type of change performed as CRUD operation
     */
    public void notifyCourseChanges(UUID courseID, CrudOperation operation){
        CourseChangeEvent dto = CourseChangeEvent.builder()
                .courseId(courseID)
                .operation(operation)
                .build();
        publishChanges(dto, TOPIC_COURSE_CHANGES);
    }

    /**
     * Method that creates Chapter Change Event Message and publishes it to a topic
     * @param chapterIds List of Chapter IDs on which changes were performed
     * @param operation type of change performed as CRUD operation
     */
    public void notifyChapterChanges(List<UUID> chapterIds, CrudOperation operation){
        ChapterChangeEvent dto = ChapterChangeEvent.builder()
                .chapterIds(chapterIds)
                .operation(operation)
                .build();
        publishChanges(dto, TOPIC_CHAPTER_CHANGES);

    }


}
