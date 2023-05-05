package de.unistuttgart.iste.gits.courseservice.service;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.ChapterEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.dao.CourseEntity;
import de.unistuttgart.iste.gits.courseservice.persistence.mapper.ChapterMapper;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.ChapterRepository;
import de.unistuttgart.iste.gits.courseservice.persistence.repository.CourseRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final ChapterRepository chapterRepository;
    private final CourseRepository courseRepository;

    public ChapterService(ChapterMapper chapterMapper, ChapterRepository chapterRepository, CourseRepository courseRepository) {
        this.chapterMapper = chapterMapper;
        this.chapterRepository = chapterRepository;
        this.courseRepository = courseRepository;
    }

    public ChapterDto createChapter(CreateChapterInputDto chapterData) {
        ChapterEntity chapterEntity = chapterMapper.mapInputDtoToEntity(chapterData);
        CourseEntity course = courseRepository.getById(UUID.fromString(chapterData.getCourseId()));
        chapterEntity.setCourse(course);
        chapterEntity = chapterRepository.save(chapterEntity);
        return chapterMapper.mapEntityToDto(chapterEntity);
    }

    public ChapterDto updateChapter(UpdateChapterInputDto chapterData) {
        ChapterEntity chapterEntity = chapterRepository.getById(UUID.fromString(chapterData.getId()));
        ChapterEntity updatedChapterEntity = chapterMapper.mapInputDtoToEntity(chapterData);
        updatedChapterEntity.setCourse(chapterEntity.getCourse());
        updatedChapterEntity = chapterRepository.save(updatedChapterEntity);
        return chapterMapper.mapEntityToDto(updatedChapterEntity);
    }

    public Optional<UUID> deleteChapter(String id) {
        if (!chapterRepository.existsById(UUID.fromString(id))) {
            return Optional.empty();
        }

        chapterRepository.deleteById(UUID.fromString(id));
        return Optional.of(UUID.fromString(id));
    }
}
