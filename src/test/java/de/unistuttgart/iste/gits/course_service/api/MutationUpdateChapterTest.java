package de.unistuttgart.iste.gits.course_service.api;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.common.testutil.MockTestPublisherConfiguration;
import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.course_service.persistence.entity.ChapterEntity;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.*;
import de.unistuttgart.iste.gits.generated.dto.Chapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.graphql.test.tester.HttpGraphQlTester;
import org.springframework.test.context.ContextConfiguration;

import java.time.OffsetDateTime;
import java.util.*;

import static de.unistuttgart.iste.gits.common.testutil.HeaderUtils.addCurrentUserHeader;
import static de.unistuttgart.iste.gits.common.testutil.TestUsers.userWithMembershipInCourseWithId;
import static de.unistuttgart.iste.gits.course_service.test_utils.TestUtils.saveCourseMembershipsOfUserToRepository;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ContextConfiguration(classes = MockTestPublisherConfiguration.class)
@GraphQlApiTest
class MutationUpdateChapterTest {

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private ChapterRepository chapterRepository;
    @Autowired
    private CourseMembershipRepository courseMembershipRepository;

    /**
     * Given a valid UpdateChapterInput
     * When the updateChapter mutation is executed
     * Then the chapter is updated and returned
     */
    @Test
    void testUpdateChapter(HttpGraphQlTester tester) {
        final CourseEntity course = courseRepository.save(CourseEntity.builder()
                .title("New Course")
                .description("This is a new course")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .published(false)
                .build());

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(course.getId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final ChapterEntity chapterEntity = chapterRepository.save(ChapterEntity.builder()
                .courseId(course.getId())
                .title("Old Chapter")
                .description("This is an old chapter")
                .startDate(OffsetDateTime.parse("2020-01-01T00:00:00.000Z"))
                .endDate(OffsetDateTime.parse("2021-01-01T00:00:00.000Z"))
                .number(1)
                .build());

        final String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "%s"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        number
                    }
                }""".formatted(chapterEntity.getId());

        tester.document(query)
                .execute()
                .path("updateChapter")
                .entity(Chapter.class)
                .satisfies(chapter -> {
                    assertThat(chapter.getTitle(), is("New Chapter"));
                    assertThat(chapter.getDescription(), is("This is a new chapter"));
                    assertThat(chapter.getStartDate(), is(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")));
                    assertThat(chapter.getEndDate(), is(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")));
                    assertThat(chapter.getNumber(), is(1));
                });

        assertThat(chapterRepository.count(), is(1L));
        final ChapterEntity updatedChapter = chapterRepository.findAll().get(0);
        assertThat(updatedChapter.getTitle(), is("New Chapter"));
        assertThat(updatedChapter.getDescription(), is("This is a new chapter"));
        assertThat(updatedChapter.getStartDate().isEqual(OffsetDateTime.parse("2020-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedChapter.getEndDate().isEqual(OffsetDateTime.parse("2021-01-01T00:00:00.000Z")), is(true));
        assertThat(updatedChapter.getNumber(), is(1));
    }

    /**
     * Given a UpdateChapterInput with an id that does not exist
     * When the updateChapter mutation is executed
     * Then an error is returned
     */
    @Test
    void testUpdateChapterNotExisting(HttpGraphQlTester tester) {
        // create admin user object
        final LoggedInUser adminUser = new LoggedInUser(UUID.randomUUID(),
                "admin",
                "admin",
                "admin",
                Collections.emptyList(),
                Collections.emptySet());
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                        description
                        startDate
                        endDate
                        number
                    }
                }""";

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> Objects.requireNonNull(responseError.getMessage())
                        .contains("Chapter with id 00000000-0000-0000-0000-000000000000 not found"));

        assertThat(chapterRepository.count(), is(0L));
    }

    /**
     * Given a UpdateChapterInput with a blank title
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testErrorOnBlankTitle(final GraphQlTester tester) {
        final String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: " "
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                    }
                }""";

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                        && responseError.getMessage().contains("must not be blank"));
    }

    /**
     * Given a UpdateChapterInput with a title that is too long
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongTitle(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: "%s"
                            description: "This is a new chapter"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                    }
                }""", "a".repeat(256));

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                        && responseError.getMessage().contains("size must be between 0 and 255"));
    }

    /**
     * Given a UpdateChapterInput with a too long description
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testTooLongDescription(final GraphQlTester tester) {
        final String query = String.format("""
                mutation {
                    updateChapter(
                        input: {
                            id: "00000000-0000-0000-0000-000000000000"
                            title: "New Chapter"
                            description: "%s"
                            startDate: "2020-01-01T00:00:00.000Z"
                            endDate: "2021-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                    }
                }""", "a".repeat(3001));

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                        && responseError.getMessage().contains("size must be between 0 and 3000"));
    }

    /**
     * Given a UpdateChapterInput where the start date is after the end date
     * When the updateChapter mutation is executed
     * Then a validation error is returned
     */
    @Test
    void testStartDateAfterEndDate(HttpGraphQlTester tester) {
        // create and save chapter
        final ChapterEntity chapter = chapterRepository.save(dummyChapterBuilder().build());

        // create admin user object
        final LoggedInUser adminUser = userWithMembershipInCourseWithId(chapter.getCourseId(),
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR);
        // save course memberships of admin to repository
        saveCourseMembershipsOfUserToRepository(courseMembershipRepository, adminUser);

        // add admin user data to header
        tester = addCurrentUserHeader(tester, adminUser);

        final String query = """
                mutation {
                    updateChapter(
                        input: {
                            id: "%s"
                            title: "New Chapter"
                            description: "This is a new chapter"
                            startDate: "2021-01-01T00:00:00.000Z"
                            endDate: "2020-01-01T00:00:00.000Z"
                            number: 1
                        }
                    ) {
                        id
                        title
                    }
                }""".formatted(chapter.getId());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null
                        && responseError.getMessage()
                        .toLowerCase().contains("start date must be before end date"));
    }

    private static ChapterEntity.ChapterEntityBuilder dummyChapterBuilder() {
        return ChapterEntity.builder()
                .courseId(UUID.randomUUID())
                .description("Test Description")
                .startDate(OffsetDateTime.now())
                .endDate(OffsetDateTime.now().plusDays(1))
                .title("Test Chapter")
                .number(1);
    }
}
