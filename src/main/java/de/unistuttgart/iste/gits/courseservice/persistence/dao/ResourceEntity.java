package de.unistuttgart.iste.gits.courseservice.persistence.dao;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity(name = "Resource")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourceEntity {
    @EmbeddedId
    ResourcePk resourceKey;

}
