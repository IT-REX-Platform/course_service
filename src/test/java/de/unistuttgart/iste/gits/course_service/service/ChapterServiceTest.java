package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.course_service.persistence.validation.ChapterValidator;
import de.unistuttgart.iste.gits.generated.dto.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.*;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Unit tests for {@link ChapterService}.
 */
class ChapterServiceTest {

    private final ChapterRepository chapterRepository = mock(ChapterRepository.class);
    private final CourseService courseService = mock(CourseService.class);
    private final ChapterMapper chapterMapper = new ChapterMapper(new ModelMapper());
    private final ChapterValidator chapterValidator = spy(new ChapterValidator());

    private final TopicPublisher topicPublisher = mock(TopicPublisher.class);

    private final ChapterService chapterService = new ChapterService(
            chapterMapper,
            chapterRepository,
            courseService,
            chapterValidator,
            topicPublisher);

    @Test
    void testGetChaptersByIdsMissingChapter() {
        UUID wrongUUID = UUID.randomUUID();
        List<UUID> uuids = List.of(wrongUUID);
        assertThrows(EntityNotFoundException.class, () -> chapterService.getChaptersByIds(uuids));
    }

    /**
     * Given a valid CreateChapterInput
     * When createChapter is called
     * Then a Chapter is returned with the correct values
     */
    @Test
    void testCreateChapterSuccessful() {
        // arrange test data
        CreateChapterInput testCreateChapterInput = dummyCreateChapterInputBuilder().build();
        ChapterEntity expectedChapter = dummyChapterEntityBuilder().build();

        // mock repository and service
        doNothing().when(courseService).requireCourseExisting(any());
        when(chapterRepository.save(any()))
                .thenReturn(expectedChapter);

        // act
        Chapter createdChapter = chapterService.createChapter(testCreateChapterInput);

        // assert
        assertThat(createdChapter.getId(), is(expectedChapter.getId()));
        assertThat(createdChapter.getTitle(), is(expectedChapter.getTitle()));
        assertThat(createdChapter.getDescription(), is(expectedChapter.getDescription()));
        assertThat(createdChapter.getStartDate(), is(expectedChapter.getStartDate()));
        assertThat(createdChapter.getEndDate(), is(expectedChapter.getEndDate()));
        assertThat(createdChapter.getNumber(), is(expectedChapter.getNumber()));

        // verify that the repository and validator were called
        verify(chapterValidator)
                .validateCreateChapterInput(testCreateChapterInput);
        verify(chapterRepository, times(1))
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a CreateChapterInput with startDate > endDate
     * When createChapter is called
     * Then a ValidationException is thrown
     */
    @Test
    void testCreateChapterStartDateAfterEndDate() {
        // arrange test data
        CreateChapterInput testCreateChapterInput = dummyCreateChapterInputBuilder()
                .setStartDate(OffsetDateTime.now().plusDays(1))
                .setEndDate(OffsetDateTime.now())
                .build();

        // mock service
        doNothing().when(courseService).requireCourseExisting(any());

        // act and assert
        assertThrows(ValidationException.class, () -> chapterService.createChapter(testCreateChapterInput));

        // verify that the repository was not called
        verify(chapterRepository, never())
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a CreateChapterInput with a non-existing courseId
     * When createChapter is called
     * Then a EntityNotFoundException is thrown
     */
    @Test
    void testCreateChapterCourseNotFound() {
        // arrange test data
        CreateChapterInput testCreateChapterInput = dummyCreateChapterInputBuilder()
                .setCourseId(UUID.randomUUID())
                .build();

        // mock service
        doThrow(EntityNotFoundException.class).when(courseService).requireCourseExisting(any());

        // act and assert
        assertThrows(EntityNotFoundException.class, () -> chapterService.createChapter(testCreateChapterInput));

        // verify that the repository was not called
        verify(chapterRepository, never())
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a valid UpdateChapterInput
     * When updateChapter is called
     * Then a Chapter is returned with the correct values
     */
    @Test
    void testUpdateChapterSuccessful() {
        // arrange test data
        ChapterEntity expectedChapter = dummyChapterEntityBuilder()
                .description("new description")
                .courseId(UUID.randomUUID())
                .build();
        UpdateChapterInput testUpdateChapterInput = dummyUpdateChapterInputBuilder(expectedChapter.getId())
                .setDescription("new description")
                .build();

        // mock repository
        when(chapterRepository.save(any()))
                .thenReturn(expectedChapter);
        when(chapterRepository.existsById(testUpdateChapterInput.getId()))
                .thenReturn(true);
        when(chapterRepository.findById(testUpdateChapterInput.getId()))
                .thenReturn(Optional.of(expectedChapter));

        // act
        Chapter updatedChapter = chapterService.updateChapter(testUpdateChapterInput);

        // assert
        assertThat(updatedChapter.getId(), is(expectedChapter.getId()));
        assertThat(updatedChapter.getTitle(), is(expectedChapter.getTitle()));
        assertThat(updatedChapter.getDescription(), is(expectedChapter.getDescription()));
        assertThat(updatedChapter.getStartDate(), is(expectedChapter.getStartDate()));
        assertThat(updatedChapter.getEndDate(), is(expectedChapter.getEndDate()));
        assertThat(updatedChapter.getNumber(), is(expectedChapter.getNumber()));
        assertThat(updatedChapter.getCourse().getId(), is(expectedChapter.getCourseId()));

        // verify that the repository and validator were called
        verify(chapterValidator)
                .validateUpdateChapterInput(testUpdateChapterInput);
        verify(chapterRepository, times(1))
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a valid UpdateChapterInput with startDate > endDate
     * When updateChapter is called
     * Then a ValidationException is thrown
     */
    @Test
    void testUpdateChapterStartDateAfterEndDate() {
        // arrange test data
        ChapterEntity expectedChapter = dummyChapterEntityBuilder().build();
        UpdateChapterInput testUpdateChapterInput = dummyUpdateChapterInputBuilder(expectedChapter.getId())
                .setStartDate(OffsetDateTime.now().plusDays(1))
                .setEndDate(OffsetDateTime.now())
                .build();

        // mock repository
        when(chapterRepository.findById(testUpdateChapterInput.getId()))
                .thenReturn(Optional.of(expectedChapter));

        // act and assert
        assertThrows(ValidationException.class, () -> chapterService.updateChapter(testUpdateChapterInput));

        // verify that the repository was not called
        verify(chapterRepository, never())
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a valid ChapterId
     * When deleteChapter is called
     * Then the UUID is returned and the delete method of the repository is called
     */
    @Test
    void testDeleteChapterSuccessful() {
        // arrange test data
        UUID testChapterId = UUID.randomUUID();

        // mock repository
        doNothing()
                .when(chapterRepository).deleteById(any());
        doReturn(ChapterEntity.builder().build())
                .when(chapterRepository).findById(any());

        // act
        UUID deletedChapterId = chapterService.deleteChapter(testChapterId);

        // assert
        assertThat(deletedChapterId, is(testChapterId));

        // verify that the repository was called
        verify(chapterRepository).deleteById(testChapterId);

        //verify notification method was called
        verify(topicPublisher).notifyChapterChanges(List.of(testChapterId), CrudOperation.DELETE);
    }

    /**
     * Given a non-existing ChapterId
     * When deleteChapter is called
     * Then a EntityNotFoundException is thrown
     */
    @Test
    void testDeleteChapterNotExisting() {
        // arrange test data
        UUID testChapterId = UUID.randomUUID();

        // mock repository
        doReturn(false)
                .when(chapterRepository).existsById(any());

        // act
        assertThrows(EntityNotFoundException.class, () -> chapterService.deleteChapter(testChapterId));

        //verify notification method was NOT called
        verify(topicPublisher, never()).notifyChapterChanges(List.of(testChapterId), CrudOperation.DELETE);
    }

    private static UpdateChapterInput.Builder dummyUpdateChapterInputBuilder(UUID uuid) {
        return UpdateChapterInput.builder()
                .setId(uuid)
                .setTitle("testTitle")
                .setDescription("testDescription")
                .setStartDate(LocalDate.of(2021, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setEndDate(LocalDate.of(2021, 2, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setNumber(1);
    }

    private static CreateChapterInput.Builder dummyCreateChapterInputBuilder() {
        return CreateChapterInput.builder()
                .setTitle("testTitle")
                .setDescription("testDescription")
                .setStartDate(LocalDate.of(2021, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setEndDate(LocalDate.of(2021, 2, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setNumber(1);
    }

    private static ChapterEntity.ChapterEntityBuilder dummyChapterEntityBuilder() {
        return ChapterEntity.builder()
                .id(UUID.randomUUID())
                .title("testTitle")
                .description("testDescription")
                .startDate(LocalDate.of(2021, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .endDate(LocalDate.of(2021, 2, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .number(1);
    }

}
