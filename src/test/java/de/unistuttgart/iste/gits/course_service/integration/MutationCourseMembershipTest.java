package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.UUID;

@GraphQlApiTest
class MutationCourseMembershipTest {

    @Autowired
    CourseMembershipRepository courseMembershipRepository;

    @Test
    void createMembershipTest(final GraphQlTester tester){

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(UUID.randomUUID())
                .setCourseId(UUID.randomUUID())
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        final String query = """
                mutation {
                    createMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("createMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void updateMembershipMembershipNotExistingTest(final GraphQlTester tester){

        //init input data
        final UUID userId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        final String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void updateMembershipTest(final GraphQlTester tester){

        //init input data
        final UUID userId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();

        final CourseMembershipEntity entity = CourseMembershipEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .role(UserRoleInCourse.STUDENT)
                .build();
        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        final String query = """
                mutation {
                    updateMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("updateMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

    @Test
    void deleteNotExistingMembershipTest(final GraphQlTester tester){

        //init input data
        final UUID userId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();

        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();


        final String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .errors()
                .expect(responseError -> responseError.getMessage() != null && responseError.getMessage().contains("not member in course"));
    }

    @Test
    void deleteMembershipTest(final GraphQlTester tester){

        //init input data
        final UUID userId = UUID.randomUUID();
        final UUID courseId = UUID.randomUUID();

        final CourseMembershipEntity entity = CourseMembershipEntity.builder()
                .userId(userId)
                .courseId(courseId)
                .role(UserRoleInCourse.STUDENT).build();
        final CourseMembership expectedDto = CourseMembership.builder()
                .setUserId(userId)
                .setCourseId(courseId)
                .setRole(UserRoleInCourse.STUDENT)
                .build();

        //create entity in DB
        courseMembershipRepository.save(entity);

        final String query = """
                mutation {
                    deleteMembership(
                        input: {
                            userId: "%s"
                            courseId: "%s"
                            role: %s
                        }
                    ) {
                        userId
                        courseId
                        role
                    }
                }
                """.formatted(expectedDto.getUserId(), expectedDto.getCourseId(), expectedDto.getRole());

        tester.document(query)
                .execute()
                .path("deleteMembership")
                .entity(CourseMembership.class)
                .isEqualTo(expectedDto);
    }

}
