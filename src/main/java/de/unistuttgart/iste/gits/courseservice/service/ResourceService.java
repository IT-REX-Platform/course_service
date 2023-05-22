package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ResourceEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ResourceRepository;
import de.unistuttgart.iste.gits.generated.dto.CourseIdAvailabilityMapDto;
import de.unistuttgart.iste.gits.generated.dto.ResourceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final CourseRepository courseRepository;

    public List<ResourceDto> getCoursesByResourceId(List<UUID> resourceIds){
        ArrayList<ResourceDto> resultList = new ArrayList<>();
        List<ResourceEntity> resourceEntities;
        List<CourseEntity> courseEntities;

        for (UUID resourceId: resourceIds){
            resourceEntities = resourceRepository.findResourceEntitiesByResourceKeyResourceIdContainingOrderByResourceKeyResourceKeyAsc(resourceId);

            courseEntities = courseRepository.findAllById(
                    resourceEntities.stream().map(
                    resource -> resource.getResourceKey().getCourseId()
            ).collect(Collectors.toList())).stream().toList();

            resultList.add(createResourceDTO(resourceId, courseEntities));
        }
        return resultList;
    }

    private ResourceDto createResourceDTO(UUID resourceId, List<CourseEntity> courses){
        ResourceDto resourceDto = new ResourceDto();
        resourceDto.setId(resourceId);
        OffsetDateTime currentTime = OffsetDateTime.now();

        resourceDto.setCourses(
                courses.stream().map(
                        course -> new CourseIdAvailabilityMapDto(course.getId(), isAvailable(course, currentTime))
                ).collect(Collectors.toList())
        );

        return resourceDto;

    }

    private boolean isAvailable(CourseEntity courseEntity, OffsetDateTime currentTime){
        if (courseEntity.isPublished())
            return courseEntity.getStartDate().isBefore(currentTime) && courseEntity.getEndDate().isAfter(currentTime);


        return false;
    }
}
