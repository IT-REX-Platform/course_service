package de.unistuttgart.iste.gits.courseservice.controller;

import de.unistuttgart.iste.gits.courseservice.dto.ChapterDto;
import de.unistuttgart.iste.gits.courseservice.dto.CreateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.dto.UpdateChapterInputDto;
import de.unistuttgart.iste.gits.courseservice.service.ChapterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.UUID;

@Controller
@Slf4j
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }


    @MutationMapping
    public ChapterDto createChapter(@Argument(name = "input") CreateChapterInputDto input) {
        return chapterService.createChapter(input);
    }

    @MutationMapping
    public ChapterDto updateChapter(@Argument(name = "input") UpdateChapterInputDto input) {
        return chapterService.updateChapter(input);
    }

    @MutationMapping
    public Optional<UUID> deleteChapter(@Argument(name = "id") UUID id) {
        return chapterService.deleteChapter(id);
    }
}
