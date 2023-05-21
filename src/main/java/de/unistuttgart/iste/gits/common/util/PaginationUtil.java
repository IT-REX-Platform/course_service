package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.PaginationDto;
import de.unistuttgart.iste.gits.generated.dto.PaginationInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

/**
 * Utility class for mapping {@link PaginationDto}s to {@link Pageable}s
 * and {@link Page}s to {@link PaginationInfoDto}s.
 */
public class PaginationUtil {

    /**
     * Creates a {@link Pageable} matching the given {@link PaginationDto} and {@link Sort}.
     *
     * @param paginationDto the {@link PaginationDto} to create the {@link Pageable} from.
     * @param sort          the {@link Sort} to use for the {@link Pageable}.
     * @return the created {@link Pageable}.
     * If the given {@link PaginationDto} is null, {@link Pageable#unpaged()} is returned.
     */
    public static Pageable createPageable(@Nullable PaginationDto paginationDto, Sort sort) {
        if (paginationDto == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(
                paginationDto.getPage(),
                paginationDto.getSize(),
                sort
        );
    }

    /**
     * Creates a {@link PaginationInfoDto} from the given {@link Page}.
     *
     * @param result the {@link Page} to create the {@link PaginationInfoDto} from.
     * @return the created {@link PaginationInfoDto}.
     */
    public static PaginationInfoDto createPaginationInfoDto(Page<?> result) {
        return PaginationInfoDto.builder()
                .setPage(result.getNumber())
                .setSize(result.getSize())
                .setTotalElements((int) result.getTotalElements())
                .setTotalPages(result.getTotalPages())
                .setHasNext(result.hasNext())
                .build();
    }

    /**
     * Creates a {@link PaginationInfoDto} for an unpaged query.
     *
     * @param totalElements the total number of elements in the query.
     * @return the created {@link PaginationInfoDto},
     * which has page 0, size totalElements, totalPages 1 and hasNext false.
     */
    public static PaginationInfoDto unpagedPaginationInfoDto(int totalElements) {
        return PaginationInfoDto.builder()
                .setPage(0)
                .setSize(totalElements)
                .setTotalElements(totalElements)
                .setTotalPages(1)
                .setHasNext(false)
                .build();
    }
}
