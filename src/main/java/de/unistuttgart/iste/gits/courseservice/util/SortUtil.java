package de.unistuttgart.iste.gits.courseservice.util;

import de.unistuttgart.iste.gits.courseservice.dto.SortDirectionDto;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;

import java.util.List;

public class SortUtil {

    @NonNull
    public static Sort createSort(List<String> sortFields, List<SortDirectionDto> sortDirections) {
        Sort sort = Sort.unsorted();
        if (sortFields == null) {
            return sort;
        }

        for (int index = 0; index < sortFields.size(); index++) {
            Sort.Direction sortDirection = getSortDirection(sortDirections, index);

            sort = sort.and(Sort.by(sortDirection, sortFields.get(index)));
        }

        return sort;
    }

    @NonNull
    private static Sort.Direction getSortDirection(List<SortDirectionDto> sortDirections, int index) {
        if (sortDirections == null || index >= sortDirections.size()) {
            return Sort.Direction.ASC;
        }
        return sortDirections.get(index) == SortDirectionDto.ASC
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;
    }
}
