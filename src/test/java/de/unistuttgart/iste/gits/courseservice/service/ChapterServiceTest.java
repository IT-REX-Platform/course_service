package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.validation.ChapterValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


/**
 * Unit tests for {@link ChapterService}.
 */
public class ChapterServiceTest {

    private final ChapterRepository chapterRepository = mock(ChapterRepository.class);
    private final CourseService courseService = mock(CourseService.class);
    private final ChapterMapper chapterMapper = new ChapterMapper(new ModelMapper());
    private final ChapterValidator chapterValidator = spy(new ChapterValidator());
    private final ChapterService chapterService = new ChapterService(
            chapterMapper,
            chapterRepository,
            courseService,
            chapterValidator);

    /**
     * Given a valid CreateChapterInputDto
     * When createChapter is called
     * Then a ChapterDto is returned with the correct values
     */
    @Test
    public void testCreateChapterSuccessful() {
        // arrange test data
        CreateChapterInputDto testCreateChapterInput = dummyCreateChapterInputDtoBuilder().build();
        ChapterEntity expectedChapter = dummyChapterEntityBuilder().build();

        // mock repository and service
        doNothing().when(courseService).requireCourseExisting(any());
        when(chapterRepository.save(any()))
                .thenReturn(expectedChapter);

        // act
        ChapterDto createdChapter = chapterService.createChapter(testCreateChapterInput);

        // assert
        assertThat(createdChapter.getId(), is(expectedChapter.getId()));
        assertThat(createdChapter.getTitle(), is(expectedChapter.getTitle()));
        assertThat(createdChapter.getDescription(), is(expectedChapter.getDescription()));
        assertThat(createdChapter.getStartDate(), is(expectedChapter.getStartDate()));
        assertThat(createdChapter.getEndDate(), is(expectedChapter.getEndDate()));
        assertThat(createdChapter.getNumber(), is(expectedChapter.getNumber()));

        // verify that the repository and validator were called
        verify(chapterValidator)
                .validateCreateChapterInputDto(testCreateChapterInput);
        verify(chapterRepository, atMostOnce())
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a CreateChapterInputDto with startDate > endDate
     * When createChapter is called
     * Then a ValidationException is thrown
     */
    @Test
    public void testCreateChapterStartDateAfterEndDate() {
        // arrange test data
        CreateChapterInputDto testCreateChapterInput = dummyCreateChapterInputDtoBuilder()
                .setStartDate(OffsetDateTime.now().plus(1, DAYS))
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
     * Given a CreateChapterInputDto with a non-existing courseId
     * When createChapter is called
     * Then a EntityNotFoundException is thrown
     */
    @Test
    public void testCreateChapterCourseNotFound() {
        // arrange test data
        CreateChapterInputDto testCreateChapterInput = dummyCreateChapterInputDtoBuilder()
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
     * Given a valid UpdateChapterInputDto
     * When updateChapter is called
     * Then a ChapterDto is returned with the correct values
     */
    @Test
    public void testUpdateChapterSuccessful() {
        // arrange test data
        ChapterEntity expectedChapter = dummyChapterEntityBuilder()
                .course(dummyCourseEntityBuilder().build())
                .build();
        UpdateChapterInputDto testUpdateChapterInput = dummyUpdateChapterInputDtoBuilder(expectedChapter.getId())
                .build();

        // mock repository
        when(chapterRepository.save(any()))
                .thenReturn(expectedChapter);
        when(chapterRepository.existsById(testUpdateChapterInput.getId()))
                .thenReturn(true);
        when(chapterRepository.findById(testUpdateChapterInput.getId()))
                .thenReturn(Optional.of(expectedChapter));

        // act
        ChapterDto updatedChapter = chapterService.updateChapter(testUpdateChapterInput);

        // assert
        assertThat(updatedChapter.getId(), is(expectedChapter.getId()));
        assertThat(updatedChapter.getTitle(), is(expectedChapter.getTitle()));
        assertThat(updatedChapter.getDescription(), is(expectedChapter.getDescription()));
        assertThat(updatedChapter.getStartDate(), is(expectedChapter.getStartDate()));
        assertThat(updatedChapter.getEndDate(), is(expectedChapter.getEndDate()));
        assertThat(updatedChapter.getNumber(), is(expectedChapter.getNumber()));
        assertThat(updatedChapter.getCourse().getId(), is(expectedChapter.getCourse().getId()));

        // verify that the repository and validator were called
        verify(chapterValidator)
                .validateUpdateChapterInputDto(testUpdateChapterInput);
        verify(chapterRepository, atMostOnce())
                .save(any(ChapterEntity.class));
    }

    /**
     * Given a valid UpdateChapterInputDto with startDate > endDate
     * When updateChapter is called
     * Then a ValidationException is thrown
     */
    @Test
    public void testUpdateChapterStartDateAfterEndDate() {
        // arrange test data
        ChapterEntity expectedChapter = dummyChapterEntityBuilder().build();
        UpdateChapterInputDto testUpdateChapterInput = dummyUpdateChapterInputDtoBuilder(expectedChapter.getId())
                .setStartDate(OffsetDateTime.now().plus(1, DAYS))
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
    public void testDeleteChapterSuccessful() {
        // arrange test data
        UUID testChapterId = UUID.randomUUID();

        // mock repository
        doNothing()
                .when(chapterRepository).deleteById(any());
        doReturn(true)
                .when(chapterRepository).existsById(any());

        // act
        UUID deletedChapterId = chapterService.deleteChapter(testChapterId);

        // assert
        assertThat(deletedChapterId, is(testChapterId));

        // verify that the repository was called
        verify(chapterRepository).deleteById(testChapterId);
    }

    /**
     * Given a non-existing ChapterId
     * When deleteChapter is called
     * Then a EntityNotFoundException is thrown
     */
    @Test
    public void testDeleteChapterNotExisting() {
        // arrange test data
        UUID testChapterId = UUID.randomUUID();

        // mock repository
        doReturn(false)
                .when(chapterRepository).existsById(any());

        // act
        assertThrows(EntityNotFoundException.class, () -> chapterService.deleteChapter(testChapterId));
    }

    private static CourseEntity.CourseEntityBuilder dummyCourseEntityBuilder() {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("testTitle")
                .description("testDescription")
                .startDate(LocalDate.of(2021, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .endDate(LocalDate.of(2021, 2, 1).atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    private static UpdateChapterInputDto.Builder dummyUpdateChapterInputDtoBuilder(UUID uuid) {
        return UpdateChapterInputDto.builder()
                .setId(uuid)
                .setTitle("testTitle")
                .setDescription("testDescription")
                .setStartDate(LocalDate.of(2021, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setEndDate(LocalDate.of(2021, 2, 1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .setNumber(1);
    }

    private static CreateChapterInputDto.Builder dummyCreateChapterInputDtoBuilder() {
        return CreateChapterInputDto.builder()
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
