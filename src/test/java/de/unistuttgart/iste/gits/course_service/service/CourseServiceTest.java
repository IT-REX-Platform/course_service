package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.common.event.CrudOperation;
import de.unistuttgart.iste.gits.course_service.dapr.TopicPublisher;
import de.unistuttgart.iste.gits.course_service.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.persistence.validation.CourseValidator;
import de.unistuttgart.iste.gits.generated.dto.Course;
import de.unistuttgart.iste.gits.generated.dto.CreateCourseInput;
import de.unistuttgart.iste.gits.generated.dto.UpdateCourseInput;
import de.unistuttgart.iste.gits.generated.dto.YearDivision;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link CourseService}.
 */
class CourseServiceTest {

    private final CourseRepository courseRepository = Mockito.mock(CourseRepository.class);
    private final CourseMapper courseMapper = new CourseMapper(new ModelMapper());
    private final CourseValidator courseValidator = Mockito.spy(CourseValidator.class);
    private final TopicPublisher topicPublisher = Mockito.mock(TopicPublisher.class);
    private final CourseService courseService = new CourseService(courseRepository, courseMapper, courseValidator, topicPublisher);

    /**
     * Given a valid CreateCourseInput
     * When createCourse is called
     * Then a Course is returned and the CourseRepository is called
     */
    @Test
    void testCreateCourseSuccessful() {
        // arrange
        CreateCourseInput input = dummyCreateCourseInputBuilder().build();
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder().build();

        // mock repository
        when(courseRepository.save(any(CourseEntity.class)))
                .thenReturn(expectedCourseEntity);

        // act
        Course actualCourse = courseService.createCourse(input);

        // assert
        assertThat(actualCourse.getId(), is(expectedCourseEntity.getId()));
        assertThat(actualCourse.getTitle(), is(expectedCourseEntity.getTitle()));
        assertThat(actualCourse.getDescription(), is(expectedCourseEntity.getDescription()));
        assertThat(actualCourse.getStartDate(), is(expectedCourseEntity.getStartDate()));
        assertThat(actualCourse.getEndDate(), is(expectedCourseEntity.getEndDate()));

        // verify
        verify(courseValidator).validateCreateCourseInput(input);
        verify(courseRepository, times(1)).save(any(CourseEntity.class));
    }

    /**
     * Given a valid CreateCourseInput
     * When createCourse is called
     * Then a Course is returned and the CourseRepository is called
     */
    @Test
    void testCreateCourseWithTermSuccessful() {
        // arrange
        CreateCourseInput input = dummyCreateCourseInputBuilder().setStartYear(2023).setYearDivision(YearDivision.FIRST_SEMESTER).build();
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder().startYear(2023).yearDivision(YearDivision.FIRST_SEMESTER).build();

        // mock repository
        when(courseRepository.save(any(CourseEntity.class)))
                .thenReturn(expectedCourseEntity);

        // act
        Course actualCourse = courseService.createCourse(input);

        // assert
        assertThat(actualCourse.getId(), is(expectedCourseEntity.getId()));
        assertThat(actualCourse.getTitle(), is(expectedCourseEntity.getTitle()));
        assertThat(actualCourse.getDescription(), is(expectedCourseEntity.getDescription()));
        assertThat(actualCourse.getStartDate(), is(expectedCourseEntity.getStartDate()));
        assertThat(actualCourse.getEndDate(), is(expectedCourseEntity.getEndDate()));
        assertThat(actualCourse.getStartYear(), is(expectedCourseEntity.getStartYear()));
        assertThat(actualCourse.getYearDivision(), is(expectedCourseEntity.getYearDivision()));

        // verify
        verify(courseValidator).validateCreateCourseInput(input);
        verify(courseRepository, times(1)).save(any(CourseEntity.class));
    }

    /**
     * Given a CreateCourseInput with a startDate after the endDate
     * When createCourse is called
     * Then a ValidationException is thrown and the course is not saved
     */
    @Test
    void testCreateCourseStartDateAfterEndDate() {
        // arrange
        CreateCourseInput input = dummyCreateCourseInputBuilder()
                .setStartDate(OffsetDateTime.parse("2021-01-02T00:00:00Z"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .build();
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder().build();

        // mock repository
        when(courseRepository.save(any(CourseEntity.class)))
                .thenReturn(expectedCourseEntity);

        // act and assert
        assertThrows(ValidationException.class, () -> courseService.createCourse(input));

        // verify
        verify(courseRepository, never()).save(any(CourseEntity.class));
    }

    /**
     * Given a valid UpdateCourseInput
     * When updateCourse is called
     * Then a Course is returned and the CourseRepository is called
     */
    @Test
    void testUpdateCourseSuccessful() {
        // arrange
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder()
                .description("New description")
                .build();
        UpdateCourseInput input = dummyUpdateCourseInputBuilder()
                .setId(expectedCourseEntity.getId())
                .setDescription("New description")
                .build();

        // mock repository
        doReturn(expectedCourseEntity)
                .when(courseRepository).save(any(CourseEntity.class));
        doReturn(true)
                .when(courseRepository).existsById(any(UUID.class));

        // act
        Course actualCourse = courseService.updateCourse(input);

        // assert
        assertThat(actualCourse.getId(), is(expectedCourseEntity.getId()));
        assertThat(actualCourse.getTitle(), is(expectedCourseEntity.getTitle()));
        assertThat(actualCourse.getDescription(), is(expectedCourseEntity.getDescription()));
        assertThat(actualCourse.getStartDate(), is(expectedCourseEntity.getStartDate()));
        assertThat(actualCourse.getEndDate(), is(expectedCourseEntity.getEndDate()));

        // verify
        verify(courseValidator).validateUpdateCourseInput(input);
        verify(courseRepository, times(1)).save(any(CourseEntity.class));
    }

    /**
     * Given an UpdateCourseInput with a startDate after the endDate
     * When updateCourse is called
     * Then a ValidationException is thrown and the course is not saved
     */
    @Test
    void testUpdateCourseStartDateAfterEndDate() {
        // arrange
        UpdateCourseInput input = dummyUpdateCourseInputBuilder()
                .setStartDate(OffsetDateTime.parse("2021-01-02T00:00:00Z"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .build();
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder().build();

        // mock repository
        doReturn(expectedCourseEntity)
                .when(courseRepository).save(any(CourseEntity.class));

        // act and assert
        assertThrows(ValidationException.class, () -> courseService.updateCourse(input));

        // verify
        verify(courseRepository, never()).save(any(CourseEntity.class));
    }

    /**
     * Given a valid CourseId
     * When deleteCourse is called
     * Then the CourseRepository is called and the id is returned
     */
    @Test
    void testDeleteCourseSuccessful() {
        // arrange
        CourseEntity entity = dummyCourseEntityBuilder().build();



        // mock repository
        doReturn(true).when(courseRepository).existsById(entity.getId());
        when(courseRepository.getReferenceById(entity.getId())).thenReturn(entity);
        entity.setChapters(List.of(dummyChapterBuilder(entity.getId()), dummyChapterBuilder(entity.getId())));

        // act
        UUID actualId = courseService.deleteCourse(entity.getId());

        // assert
        assertThat(actualId, is(entity.getId()));

        // verify
        verify(courseRepository).delete(entity);
        verify(topicPublisher).notifyCourseChanges(entity.getId(), CrudOperation.DELETE);
        verify(topicPublisher).notifyChapterChanges(entity.getChapters()
                .stream()
                .map(ChapterEntity::getId)
                .toList(), CrudOperation.DELETE);

    }

    /**
     * Given a non-existing CourseId
     * When deleteCourse is called
     * Then an EntityNotFoundException is thrown
     */
    @Test
    void testDeleteCourseNonExisting() {
        // arrange
        UUID id = UUID.randomUUID();

        // mock repository
        doReturn(false).when(courseRepository).existsById(id);

        // act and assert
        assertThrows(EntityNotFoundException.class, () -> courseService.deleteCourse(id));

        // we do not care if the repository was called
    }

    /**
     * Given a valid CourseId
     * When requireCourseExisting is called
     * Then no exception is thrown
     */
    @Test
    void testRequireCourseExisting() {
        // arrange
        UUID id = UUID.randomUUID();

        // mock repository
        doReturn(true).when(courseRepository).existsById(id);

        // act
        assertDoesNotThrow(() -> courseService.requireCourseExisting(id));

        // verify
        verify(courseRepository).existsById(id);
    }

    /**
     * Given a non-existing CourseId
     * When requireCourseExisting is called
     * Then an EntityNotFoundException is thrown
     */
    @Test
    void testRequireCourseExistingNonExisting() {
        // arrange
        UUID id = UUID.randomUUID();

        // mock repository
        doReturn(false).when(courseRepository).existsById(id);

        // act and assert
        assertThrows(EntityNotFoundException.class, () -> courseService.requireCourseExisting(id));

        // verify
        verify(courseRepository).existsById(id);
    }

    private CreateCourseInput.Builder dummyCreateCourseInputBuilder() {
        return CreateCourseInput.builder()
                .setTitle("title")
                .setDescription("description")
                .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"));
    }

    private CourseEntity.CourseEntityBuilder dummyCourseEntityBuilder() {
        return CourseEntity.builder()
                .id(UUID.randomUUID())
                .title("title")
                .description("description")
                .startDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"));
    }

    private UpdateCourseInput.Builder dummyUpdateCourseInputBuilder() {
        return UpdateCourseInput.builder()
                .setTitle("title")
                .setDescription("description")
                .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"));
    }

    private ChapterEntity dummyChapterBuilder(UUID courseId){
        return ChapterEntity.builder()
                .id(UUID.randomUUID())
                .courseId(courseId)
                .description("TestChapter")
                .title("Test")
                .number(1)
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now())
                .suggestedStartDate(OffsetDateTime.now())
                .suggestedEndDate(OffsetDateTime.now())
                .build();
    }
}
