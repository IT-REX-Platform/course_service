package de.unistuttgart.iste.gits.courseservice.controller;

import de.unistuttgart.iste.gits.courseservice.service.ChapterService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(ChapterService chapterService) {
        this.chapterService = chapterService;
    }


    @MutationMapping
    public ChapterDto createChapter(@Argument("input") CreateChapterInputDto input) {
        return chapterService.createChapter(input);
    }

    @MutationMapping
    public ChapterDto updateChapter(@Argument("input") UpdateChapterInputDto input) {
        return chapterService.updateChapter(input);
    }

    @MutationMapping
    public UUID deleteChapter(@Argument("id") UUID id) {
        return chapterService.deleteChapter(id);
    }

    @QueryMapping
    public ChapterPayloadDto chapters(@Argument("courseId") UUID courseId,
                                      @Argument("filter") @Nullable ChapterFilterDto filter,
                                      @Argument("sortBy") List<String> sortBy,
                                      @Argument("sortDirection") List<SortDirectionDto> sortDirection,
                                      @Argument("pagination") @Nullable PaginationDto pagination) {
        return chapterService.getChapters(courseId, filter, sortBy, sortDirection, pagination);
    }

    @SchemaMapping(typeName = "Course", field = "chapters")
    public ChapterPayloadDto chapters(CourseDto courseDto,
                                      @Argument("filter") @Nullable ChapterFilterDto filter,
                                      @Argument("sortBy") List<String> sortBy,
                                      @Argument("sortDirection") List<SortDirectionDto> sortDirection,
                                      @Argument("pagination") @Nullable PaginationDto pagination) {
        return chapterService.getChapters(courseDto.getId(), filter, sortBy, sortDirection, pagination);
    }
}
