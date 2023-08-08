package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.course_service.service.ChapterService;
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
    public Chapter createChapter(@Argument("input") CreateChapterInput input) {
        return chapterService.createChapter(input);
    }

    @MutationMapping
    public Chapter updateChapter(@Argument("input") UpdateChapterInput input) {
        return chapterService.updateChapter(input);
    }

    @MutationMapping
    public UUID deleteChapter(@Argument("id") UUID id) {
        return chapterService.deleteChapter(id);
    }

    @QueryMapping
    public ChapterPayload chapters(@Argument("courseId") UUID courseId,
                                   @Argument("filter") @Nullable ChapterFilter filter,
                                   @Argument("sortBy") List<String> sortBy,
                                   @Argument("sortDirection") List<SortDirection> sortDirection,
                                   @Argument("pagination") @Nullable Pagination pagination) {
        return chapterService.getChapters(courseId, filter, sortBy, sortDirection, pagination);
    }

    @SchemaMapping(typeName = "Course", field = "chapters")
    public ChapterPayload chapters(Course course,
                                   @Argument("filter") @Nullable ChapterFilter filter,
                                   @Argument("sortBy") List<String> sortBy,
                                   @Argument("sortDirection") List<SortDirection> sortDirection,
                                   @Argument("pagination") @Nullable Pagination pagination) {
        return chapterService.getChapters(course.getId(), filter, sortBy, sortDirection, pagination);
    }

    @QueryMapping
    public List<Chapter> chaptersByIds(@Argument List<UUID> ids) {
        return chapterService.getChaptersByIds(ids);
    }
}
