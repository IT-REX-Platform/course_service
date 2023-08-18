package de.unistuttgart.iste.gits.course_service.persistence.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity(name = "Course")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private OffsetDateTime startDate;

    @Column(nullable = false)
    private OffsetDateTime endDate;

    @Column(nullable = false)
    private boolean published;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "courseId")
    @OrderBy("number ASC")
    private List<ChapterEntity> chapters;

    @OneToMany(cascade = CascadeType.REMOVE, mappedBy = "courseId")
    private Set<CourseResourceAssociationEntity> resources;

    @Column()
    private int startYear;

    @Enumerated(EnumType.STRING)
    private YearDivision yearDivision;

    public enum YearDivision{
        FIRST_SEMESTER,
        SECOND_SEMESTER,
        FIRST_TRIMESTER,
        SECOND_TRIMESTER,
        THIRD_TRIMESTER,
        FIRST_QUARTER,
        SECOND_QUARTER,
        THIRD_QUARTER,
        FOURTH_QUARTER

    }

}
