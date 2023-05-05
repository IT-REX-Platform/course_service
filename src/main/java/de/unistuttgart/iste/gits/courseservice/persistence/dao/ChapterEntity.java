package de.unistuttgart.iste.gits.courseservice.persistence.dao;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull
    private String title;

    @Column(nullable = false, length = 3000)
    @NotNull
    private String description;

    @Column(nullable = false)
    @Min(1)
    private int number;

    @Column(nullable = false)
    @NotNull
    private OffsetDateTime startDate;

    private OffsetDateTime endDate;

    @ManyToOne
    private CourseEntity course;
}
