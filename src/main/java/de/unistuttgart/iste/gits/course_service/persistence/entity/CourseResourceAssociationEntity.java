package de.unistuttgart.iste.gits.course_service.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;


@Entity(name = "CourseResourceAssociation")
@IdClass(ResourcePk.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseResourceAssociationEntity {

    @Id
    @Column(nullable = false)
    private UUID resourceId;

    @Id
    @Column(nullable = false)
    private UUID chapterId;

    private UUID courseId;

}
