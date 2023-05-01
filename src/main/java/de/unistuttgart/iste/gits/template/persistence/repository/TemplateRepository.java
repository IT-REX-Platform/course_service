package de.unistuttgart.iste.gits.template.persistence.repository;

import de.unistuttgart.iste.gits.template.persistence.dao.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {

    Optional<TemplateEntity> findByName(String name);

}
