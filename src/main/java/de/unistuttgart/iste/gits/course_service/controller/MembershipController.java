package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.common.user_handling.UserCourseAccessValidator;
import de.unistuttgart.iste.gits.course_service.service.MembershipService;
import de.unistuttgart.iste.gits.generated.dto.Course;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.CourseMembershipInput;
import de.unistuttgart.iste.gits.generated.dto.UserRoleInCourse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @QueryMapping(name = "_internal_noauth_courseMembershipsByUserIds")
    public List<List<CourseMembership>> courseMembershipsByUserIds(@Argument final List<UUID> userIds) {
        return membershipService.getAllMembershipsByUserIds(userIds);
    }

    @MutationMapping
    public CourseMembership joinCourse(@Argument final UUID courseId,
                             @ContextValue final LoggedInUser currentUser) {
        return membershipService.createMembership(CourseMembershipInput.builder()
                .setCourseId(courseId)
                .setUserId(currentUser.getId())
                .setRole(UserRoleInCourse.STUDENT)
                .build());
    }

    @MutationMapping
    public CourseMembership createMembership(@Argument(name = "input") final CourseMembershipInput inputDto,
                                             @ContextValue final LoggedInUser currentUser) {
        UserCourseAccessValidator.validateUserHasAccessToCourse(currentUser,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                inputDto.getCourseId());

        return membershipService.createMembership(inputDto);
    }

    @MutationMapping
    public CourseMembership updateMembership(@Argument(name = "input") final CourseMembershipInput inputDto,
                                             @ContextValue final LoggedInUser currentUser) {
        UserCourseAccessValidator.validateUserHasAccessToCourse(currentUser,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                inputDto.getCourseId());

        return membershipService.updateMembershipRole(inputDto);
    }

    @MutationMapping
    public CourseMembership deleteMembership(@Argument(name = "input") final CourseMembershipInput inputDto,
                                             @ContextValue final LoggedInUser currentUser) {
        UserCourseAccessValidator.validateUserHasAccessToCourse(currentUser,
                LoggedInUser.UserRoleInCourse.ADMINISTRATOR,
                inputDto.getCourseId());

        return membershipService.deleteMembership(inputDto);
    }
}
