package de.unistuttgart.iste.gits.course_service.controller;

import de.unistuttgart.iste.gits.course_service.service.ChapterService;
import de.unistuttgart.iste.gits.generated.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Controller
@Slf4j
public class ChapterController {

    private final ChapterService chapterService;

    public ChapterController(final ChapterService chapterService) {
        this.chapterService = chapterService;
    }


    @MutationMapping
    public Chapter createChapter(@Argument("input") final CreateChapterInput input) {
        return chapterService.createChapter(input);
    }

    @MutationMapping
    public Chapter updateChapter(@Argument("input") final UpdateChapterInput input) {
        return chapterService.updateChapter(input);
    }

    @MutationMapping
    public UUID deleteChapter(@Argument("id") final UUID id) {
        return chapterService.deleteChapter(id);
    }

    @QueryMapping
    public ChapterPayload chapters(@Argument("courseId") final UUID courseId,
                                   @Argument("filter") @Nullable final ChapterFilter filter,
                                   @Argument("sortBy") final List<String> sortBy,
                                   @Argument("sortDirection") final List<SortDirection> sortDirection,
                                   @Argument("pagination") @Nullable final Pagination pagination) {
        return chapterService.getChapters(courseId, filter, sortBy, sortDirection, pagination);
    }

    @SchemaMapping(typeName = "Course", field = "chapters")
    public ChapterPayload chapters(final Course course,
                                   @Argument("filter") @Nullable final ChapterFilter filter,
                                   @Argument("sortBy") final List<String> sortBy,
                                   @Argument("sortDirection") final List<SortDirection> sortDirection,
                                   @Argument("pagination") @Nullable final Pagination pagination) {
        return chapterService.getChapters(course.getId(), filter, sortBy, sortDirection, pagination);
    }

    @QueryMapping
    public List<Chapter> chaptersByIds(@Argument final List<UUID> ids) {
        return chapterService.getChaptersByIds(ids);
    }

    @SchemaMapping(typeName = "Chapter", field = "course")
    public Course course(final Chapter chapter) {
        return chapterService.getCourseForChapterId(chapter.getId());
    }
}
