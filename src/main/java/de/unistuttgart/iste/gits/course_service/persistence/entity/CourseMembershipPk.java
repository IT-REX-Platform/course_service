package de.unistuttgart.iste.gits.course_service.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseMembershipPk implements Serializable {

    private UUID userId;

    private UUID courseId;
}
