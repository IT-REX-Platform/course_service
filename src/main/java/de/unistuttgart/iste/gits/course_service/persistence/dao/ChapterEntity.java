package de.unistuttgart.iste.gits.course_service.persistence.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity(name = "Chapter")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private int number;

    @Column(nullable = false)
    private OffsetDateTime startDate;

    @Column(nullable = false)
    private OffsetDateTime endDate;

    @Column
    private OffsetDateTime suggestedStartDate;

    @Column
    private OffsetDateTime suggestedEndDate;

    @Column(nullable = false, name = "course_id")
    private UUID courseId;

}
