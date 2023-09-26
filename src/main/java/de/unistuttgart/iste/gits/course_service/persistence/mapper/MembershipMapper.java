package de.unistuttgart.iste.gits.course_service.persistence.mapper;

import de.unistuttgart.iste.gits.course_service.persistence.entity.CourseMembershipEntity;
import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.CourseMembershipInput;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MembershipMapper {

    private final ModelMapper modelMapper;


    public CourseMembership entityToDto(final CourseMembershipEntity courseMembershipEntity){

        return modelMapper.map(courseMembershipEntity, CourseMembership.class);

    }

    public CourseMembershipEntity dtoToEntity(final CourseMembershipInput membershipInput){

        return modelMapper.map(membershipInput, CourseMembershipEntity.class);
    }
}
