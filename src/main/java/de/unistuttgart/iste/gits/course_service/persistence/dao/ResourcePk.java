package de.unistuttgart.iste.gits.course_service.persistence.dao;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResourcePk implements Serializable {

    private UUID resourceId;

    private UUID chapterId;

}
