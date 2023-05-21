package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.SortDirectionDto;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.List;

/**
 * Utility class for sorting.
 */
public class SortUtil {

    /**
     * Creates a {@link Sort} object from the given sort fields and sort directions.
     *
     * @param sortFields     the sort fields, may be empty or null.
     * @param sortDirections the sort directions, may be empty or null. If it has fewer elements than sortFields,
     *                       the remaining sort fields will be sorted ascending.
     * @return the created {@link Sort} object. If sortField is empty or null, {@link Sort#unsorted()} is returned.
     */
    @NonNull
    public static Sort createSort(@Nullable List<String> sortFields, @Nullable List<SortDirectionDto> sortDirections) {
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
