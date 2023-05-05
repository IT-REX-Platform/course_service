package de.unistuttgart.iste.gits.courseservice.persistence.repository;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.UUID;

public interface ChapterRepository extends JpaRepository<ChapterEntity, UUID> {

    @NonNull ChapterEntity getById(@NonNull UUID id);

}
