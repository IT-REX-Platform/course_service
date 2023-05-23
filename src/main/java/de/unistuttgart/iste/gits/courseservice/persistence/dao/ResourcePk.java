package de.unistuttgart.iste.gits.courseservice.persistence.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ResourcePk implements Serializable {

    @Column(nullable = false)
    private UUID courseId;

    @Column(nullable = false)
    private UUID resourceId;

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getResourceId() {
        return resourceId;
    }

    public void setResourceId(UUID resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePk that = (ResourcePk) o;
        return Objects.equals(getCourseId(), that.getCourseId()) && Objects.equals(getResourceId(), that.getResourceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCourseId(), getResourceId());
    }
}
