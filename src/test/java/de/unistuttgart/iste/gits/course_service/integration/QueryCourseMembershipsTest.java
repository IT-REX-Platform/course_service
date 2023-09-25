package de.unistuttgart.iste.gits.course_service.integration;

import de.unistuttgart.iste.gits.common.testutil.GraphQlApiTest;
import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.course_service.persistence.repository.CourseMembershipRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.test.tester.GraphQlTester;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GraphQlApiTest
class QueryCourseMembershipsTest {

    @Autowired
    private CourseMembershipRepository membershipRepository;

    UUID userId = UUID.randomUUID();

    @Test
    void testNoMembershipExisting(final GraphQlTester tester){
        final String query = String.format("""
                query {
                        courseMemberships(userId: "%s") {
                            userId
                            courseId
                            role
                    }
                }
                """, userId);
        tester.document(query)
                .variable("userId", userId)
                .execute()
                .path("courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(0);
    }

    @Test
    void testMembership(final GraphQlTester tester){

        final UUID userId = UUID.randomUUID();
        final List<CourseMembership> DTOList = new ArrayList<>();

        for (int i = 0; i < 2; i++) {
            final UUID courseId = UUID.randomUUID();
            final CourseMembershipEntity entity = CourseMembershipEntity.builder().userId(userId).courseId(courseId).role(UserRoleInCourse.STUDENT).build();
            final CourseMembership dto = CourseMembership.builder().setUserId(userId).setCourseId(courseId).setRole(UserRoleInCourse.STUDENT).build();
            membershipRepository.save(entity);
            DTOList.add(dto);
        }

        final String query = String.format("""
                query {
                        courseMemberships(userId: "%s") {
                            userId
                            courseId
                            role
                    }
                }
                """, userId);
        tester.document(query)
                .variable("userId", userId)
                .execute()
                .path("courseMemberships")
                .entityList(CourseMembership.class)
                .hasSize(2)
                .contains(DTOList.get(0), DTOList.get(1));
    }

}
