package de.unistuttgart.iste.gits.course_service.persistence.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
