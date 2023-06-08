package de.unistuttgart.iste.gits.course_service.service;

import de.unistuttgart.iste.gits.course_service.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.mapper.CourseMapper;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.course_service.persistence.validation.CourseValidator;
import de.unistuttgart.iste.gits.generated.dto.CourseDto;
import de.unistuttgart.iste.gits.generated.dto.CreateCourseInputDto;
import de.unistuttgart.iste.gits.generated.dto.UpdateCourseInputDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;

import java.time.OffsetDateTime;
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
    private final CourseService courseService = new CourseService(courseRepository, courseMapper, courseValidator);

    /**
     * Given a valid CreateCourseInputDto
     * When createCourse is called
     * Then a CourseDto is returned and the CourseRepository is called
     */
    @Test
    void testCreateCourseSuccessful() {
        // arrange
        CreateCourseInputDto input = dummyCreateCourseInputDtoBuilder().build();
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder().build();

        // mock repository
        when(courseRepository.save(any(CourseEntity.class)))
                .thenReturn(expectedCourseEntity);

        // act
        CourseDto actualCourseDto = courseService.createCourse(input);

        // assert
        assertThat(actualCourseDto.getId(), is(expectedCourseEntity.getId()));
        assertThat(actualCourseDto.getTitle(), is(expectedCourseEntity.getTitle()));
        assertThat(actualCourseDto.getDescription(), is(expectedCourseEntity.getDescription()));
        assertThat(actualCourseDto.getStartDate(), is(expectedCourseEntity.getStartDate()));
        assertThat(actualCourseDto.getEndDate(), is(expectedCourseEntity.getEndDate()));

        // verify
        verify(courseValidator).validateCreateCourseInputDto(input);
        verify(courseRepository, times(1)).save(any(CourseEntity.class));
    }

    /**
     * Given a CreateCourseInputDto with a startDate after the endDate
     * When createCourse is called
     * Then a ValidationException is thrown and the course is not saved
     */
    @Test
    void testCreateCourseStartDateAfterEndDate() {
        // arrange
        CreateCourseInputDto input = dummyCreateCourseInputDtoBuilder()
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
     * Given a valid UpdateCourseInputDto
     * When updateCourse is called
     * Then a CourseDto is returned and the CourseRepository is called
     */
    @Test
    void testUpdateCourseSuccessful() {
        // arrange
        CourseEntity expectedCourseEntity = dummyCourseEntityBuilder()
                .description("New description")
                .build();
        UpdateCourseInputDto input = dummyUpdateCourseInputDtoBuilder()
                .setId(expectedCourseEntity.getId())
                .setDescription("New description")
                .build();

        // mock repository
        doReturn(expectedCourseEntity)
                .when(courseRepository).save(any(CourseEntity.class));
        doReturn(true)
                .when(courseRepository).existsById(any(UUID.class));

        // act
        CourseDto actualCourseDto = courseService.updateCourse(input);

        // assert
        assertThat(actualCourseDto.getId(), is(expectedCourseEntity.getId()));
        assertThat(actualCourseDto.getTitle(), is(expectedCourseEntity.getTitle()));
        assertThat(actualCourseDto.getDescription(), is(expectedCourseEntity.getDescription()));
        assertThat(actualCourseDto.getStartDate(), is(expectedCourseEntity.getStartDate()));
        assertThat(actualCourseDto.getEndDate(), is(expectedCourseEntity.getEndDate()));

        // verify
        verify(courseValidator).validateUpdateCourseInputDto(input);
        verify(courseRepository, times(1)).save(any(CourseEntity.class));
    }

    /**
     * Given an UpdateCourseInputDto with a startDate after the endDate
     * When updateCourse is called
     * Then a ValidationException is thrown and the course is not saved
     */
    @Test
    void testUpdateCourseStartDateAfterEndDate() {
        // arrange
        UpdateCourseInputDto input = dummyUpdateCourseInputDtoBuilder()
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
        UUID id = UUID.randomUUID();

        // mock repository
        doReturn(true).when(courseRepository).existsById(id);

        // act
        UUID actualId = courseService.deleteCourse(id);

        // assert
        assertThat(actualId, is(id));

        // verify
        verify(courseRepository).deleteById(id);
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

    private CreateCourseInputDto.Builder dummyCreateCourseInputDtoBuilder() {
        return CreateCourseInputDto.builder()
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

    private UpdateCourseInputDto.Builder dummyUpdateCourseInputDtoBuilder() {
        return UpdateCourseInputDto.builder()
                .setTitle("title")
                .setDescription("description")
                .setStartDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"))
                .setEndDate(OffsetDateTime.parse("2021-01-01T00:00:00Z"));
    }
}
