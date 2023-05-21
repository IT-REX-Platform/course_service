package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.PaginationDto;
import de.unistuttgart.iste.gits.generated.dto.PaginationInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.*;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PaginationUtilTest {

    /**
     * Given no pagination dto
     * When createPageable is called
     * Then an unpaged pageable is returned
     */
    @Test
    public void testCreatePageableNoPaginationDto() {
        Pageable pageable = PaginationUtil.createPageable(null, null);

        assertThat(pageable.isPaged(), equalTo(false));

        pageable = PaginationUtil.createPageable(null, Sort.by("field"));

        assertThat(pageable.isPaged(), equalTo(false));
    }

    /**
     * Given a pagination dto
     * When createPageable is called
     * Then a pageable matching the pagination dto is returned
     */
    @Test
    public void testCreatePageable() {
        PaginationDto paginationDto = PaginationDto.builder()
                .setPage(1)
                .setSize(10)
                .build();

        Pageable pageable = PaginationUtil.createPageable(paginationDto, Sort.unsorted());
        assertThat(pageable.isPaged(), equalTo(true));
        assertThat(pageable.getPageNumber(), equalTo(1));
        assertThat(pageable.getPageSize(), equalTo(10));

        assertThat(pageable.getSort(), equalTo(Sort.unsorted()));
    }

    /**
     * Given a number of total elements
     * When unpagedPaginationInfoDto is called
     * Then the correct pagination info dto is returned
     */
    @Test
    public void testCreateUnpagedPaginationInfoDto() {
        PaginationInfoDto paginationDto = PaginationUtil.unpagedPaginationInfoDto(10);

        assertThat(paginationDto.getPage(), equalTo(0));
        assertThat(paginationDto.getSize(), equalTo(10));
        assertThat(paginationDto.getTotalElements(), equalTo(10));
        assertThat(paginationDto.getTotalPages(), equalTo(1));
        assertThat(paginationDto.getHasNext(), equalTo(false));
    }

    /**
     * Given a page
     * When createPaginationInfoDto is called
     * Then the correct pagination info dto is returned
     */
    @Test
    public void testCreatePaginationInfoDto() {
        Page<?> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 20);
        PaginationInfoDto paginationDto = PaginationUtil.createPaginationInfoDto(page);

        assertThat(paginationDto.getPage(), equalTo(0));
        assertThat(paginationDto.getSize(), equalTo(10));
        assertThat(paginationDto.getTotalElements(), equalTo(20));
        assertThat(paginationDto.getTotalPages(), equalTo(2));
        assertThat(paginationDto.getHasNext(), equalTo(true));
    }
}
