package de.unistuttgart.iste.gits.courseservice.persistence.repository;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.ResourceEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ResourcePk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for {@link ResourceEntity}.
 */
@Repository
public interface ResourceRepository extends JpaRepository<ResourceEntity, ResourcePk>, JpaSpecificationExecutor<ResourceEntity> {

    /** Hibernate Query. ORDERED BY ResourceKey ASC
     * @param resourceId resource ID
     * @return a List of Resources for a resource ID
     */
    List<ResourceEntity> findResourceEntitiesByResourceKeyResourceIdContainingOrderByResourceKeyResourceKeyAsc(UUID resourceId);

}
