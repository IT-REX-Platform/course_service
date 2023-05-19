package de.unistuttgart.iste.gits.courseservice.util;

import de.unistuttgart.iste.gits.courseservice.dto.PaginationDto;
import de.unistuttgart.iste.gits.courseservice.dto.PaginationInfoDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;

public class PaginationUtil {

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

    public static PaginationInfoDto createPaginationInfoDto(Page<?> result) {
        return PaginationInfoDto.builder()
                .setPage(result.getNumber())
                .setSize(result.getSize())
                .setTotalElements((int) result.getTotalElements())
                .setTotalPages(result.getTotalPages())
                .setHasNext(result.hasNext())
                .build();
    }

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
