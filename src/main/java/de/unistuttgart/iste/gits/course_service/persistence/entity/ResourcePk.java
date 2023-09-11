package de.unistuttgart.iste.gits.course_service.persistence.entity;


import lombok.*;

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
