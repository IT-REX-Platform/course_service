package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.DateTimeFilterDto;
import de.unistuttgart.iste.gits.generated.dto.IntFilterDto;
import de.unistuttgart.iste.gits.generated.dto.StringFilterDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utility class for creating specifications for JPA queries.
 * <p>
 * Contains methods for creating specifications for filtering by string, boolean, integer and date time values
 * using the filter DTOs.
 */
public class SpecificationUtil {

    private static final Specification<?> ALWAYS_TRUE = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

    private static final Specification<?> ALWAYS_FALSE = (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();

    /**
     * Creates a specification for the given string filter.
     *
     * @param field           the field to filter by
     * @param stringFilterDto the string filter DTO, may be null
     * @param <T>             generic type to allow this filter to be used with any entity type
     * @return the specification for the given string filter,
     * or {@link SpecificationUtil#alwaysTrue()} if the filter is null
     */
    public static <T> Specification<T> stringFilter(String field, @Nullable StringFilterDto stringFilterDto) {
        if (stringFilterDto == null) {
            return alwaysTrue();
        }

        return Specification
                .<T>where(stringEqualTo(field, stringFilterDto.getEquals(), stringFilterDto.getIgnoreCase()))
                .and(contains(field, stringFilterDto.getContains(), stringFilterDto.getIgnoreCase()));
    }

    /**
     * Creates a specification for the given date time filter.
     *
     * @param field             the field to filter by
     * @param dateTimeFilterDto the date time filter DTO, may be null
     * @param <T>               generic type to allow this filter to be used with any entity type
     * @return the specification for the given date time filter,
     * or {@link SpecificationUtil#alwaysTrue()} if the filter is null
     */
    public static <T> Specification<T> dateTimeFilter(String field, @Nullable DateTimeFilterDto dateTimeFilterDto) {
        if (dateTimeFilterDto == null) {
            return alwaysTrue();
        }

        return Specification.<T>where(lessThan(field, dateTimeFilterDto.getBefore()))
                .and(greaterThan(field, dateTimeFilterDto.getAfter()));
    }

    /**
     * Creates a specification for the given integer filter.
     *
     * @param field        the field to filter by
     * @param intFilterDto the integer filter DTO, may be null
     * @param <T>          generic type to allow this filter to be used with any entity type
     * @return the specification for the given integer filter,
     * or {@link SpecificationUtil#alwaysTrue()} if the filter is null
     */
    public static <T> Specification<T> intFilter(String field, @Nullable IntFilterDto intFilterDto) {
        if (intFilterDto == null) {
            return alwaysTrue();
        }

        return Specification.<T>where(equalTo(field, intFilterDto.getEquals()))
                .and(lessThan(field, intFilterDto.getLessThan()))
                .and(greaterThan(field, intFilterDto.getGreaterThan()));
    }

    /**
     * Creates a specification for the "not" filter.
     * This filter is used to negate another filter.
     *
     * @param not                the filter to negate, may be null.
     * @param dtoToSpecification the function to convert the filter DTO to a specification.
     * @param <EntityType>       the type of the entity.
     * @param <DtoType>          the type of the filter DTO.
     * @return the specification for the "not" filter, or {@link SpecificationUtil#alwaysTrue()} if the filter is null.
     */
    public static <EntityType, DtoType> Specification<EntityType> not(
            DtoType not,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Optional.ofNullable(not)
                .map(dtoToSpecification)
                .map(Specification::not)
                .orElse(alwaysTrue());
    }

    /**
     * Creates a specification for combining multiple filter DTOs with "and".
     *
     * @param ands               the filter DTOs to combine, may be null.
     * @param dtoToSpecification the function to convert the filter DTO to a specification.
     * @param <EntityType>       the type of the entity.
     * @param <DtoType>          the type of the filter DTO.
     * @return the specification for the "and" filter, or {@link SpecificationUtil#alwaysTrue()} if the filter is null.
     */
    public static <EntityType, DtoType> Specification<EntityType> and(
            @Nullable List<DtoType> ands,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Stream.ofNullable(ands)
                .flatMap(List::stream)
                .map(dtoToSpecification)
                .reduce(alwaysTrue(), Specification::and);
    }

    /**
     * Creates a specification for combining multiple filter DTOs with "or".
     *
     * @param ors                the filter DTOs to combine, may be null.
     * @param dtoToSpecification the function to convert the filter DTO to a specification.
     * @param <EntityType>       the type of the entity.
     * @param <DtoType>          the type of the filter DTO.
     * @return the specification for the "or" filter, or {@link SpecificationUtil#alwaysFalse()} if the filter is null.
     */
    public static <EntityType, DtoType> Specification<EntityType> or(
            @Nullable List<DtoType> ors,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Stream.ofNullable(ors)
                .flatMap(List::stream)
                .map(dtoToSpecification)
                .reduce(Specification::or)
                // otherwise the result must be always false because (something OR true) would be always true
                .orElse(alwaysFalse());
    }

    /**
     * Creates a specification for filtering by a boolean value.
     *
     * @param field the field to filter by
     * @param value the value to filter by, may be null
     * @param <T>   generic type to allow this filter to be used with any entity type
     * @return the specification that checks if the field equals the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T> Specification<T> booleanFilter(String field, @Nullable Boolean value) {
        return equalTo(field, value);
    }

    /**
     * Creates a specification that checks if the given field equals the given value.
     *
     * @param field the field to filter by
     * @param value the value the field should be equal to, may be null
     * @param <T>   generic type to allow this filter to be used with any entity type
     * @return the specification that checks if the field equals the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T> Specification<T> equalTo(String field, @Nullable Object value) {
        if (value == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    /**
     * Creates a specification that checks if the given string field equals the given value.
     *
     * @param field      the field to filter by
     * @param value      the value the field should be equal to, may be null
     * @param ignoreCase whether to ignore case when comparing the field and the value
     * @param <T>        generic type to allow this filter to be used with any entity type
     * @return the specification that checks if the field equals the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T> Specification<T> stringEqualTo(String field, @Nullable String value, boolean ignoreCase) {
        if (value == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> {
            Expression<String> path = towLowerCasePathIfIgnoreCase(field, root, criteriaBuilder, ignoreCase);
            return criteriaBuilder.equal(path, toLowerCaseIfIgnoreCase(value, ignoreCase));
        };
    }

    /**
     * Creates a specification that checks if the given string value is contained in the given field.
     *
     * @param field      the field to filter by
     * @param value      the value the field should contain, may be null
     * @param ignoreCase whether to ignore case when comparing the field and the value
     * @param <T>        generic type to allow this filter to be used with any entity type
     * @return the specification that checks if the field contains the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T> Specification<T> contains(String field, String value, boolean ignoreCase) {
        if (value == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> {
            Expression<String> path = towLowerCasePathIfIgnoreCase(field, root, criteriaBuilder, ignoreCase);

            // use the sql wildcard character % to allow any characters before and after the value
            return criteriaBuilder.like(path, "%" + toLowerCaseIfIgnoreCase(value, ignoreCase) + "%");
        };
    }

    /**
     * Creates a specification that checks if the given value is less than the given field.
     *
     * @param field the field to filter by
     * @param value the value the field should be less than, may be null
     * @param <T>   generic type to allow this filter to be used with any entity type
     * @param <C>   type of the value to compare, must be comparable
     * @return the specification that checks if the field is less than the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T, C extends Comparable<C>> Specification<T> lessThan(String field, @Nullable C value) {
        if (value == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(field), value);
    }

    /**
     * Creates a specification that checks if the given value is greater than the given field.
     *
     * @param field the field to filter by
     * @param value the value the field should be greater than, may be null
     * @param <T>   generic type to allow this filter to be used with any entity type
     * @param <C>   type of the value to compare, must be comparable
     * @return the specification that checks if the field is greater than the value,
     * or {@link SpecificationUtil#alwaysTrue()} if the value is null
     */
    public static <T, C extends Comparable<C>> Specification<T> greaterThan(String field, @Nullable C value) {
        if (value == null) {
            return alwaysTrue();
        }
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(field), value);
    }

    /**
     * Specification for a filter that does not filter out anything, i.e. always true.
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> alwaysTrue() {
        return (Specification<T>) ALWAYS_TRUE;
    }

    /**
     * Specification for a filter that filters out everything, i.e. always false.
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> alwaysFalse() {
        return (Specification<T>) ALWAYS_FALSE;
    }

    private static Expression<String> towLowerCasePathIfIgnoreCase(String field,
                                                                   Root<?> root,
                                                                   CriteriaBuilder criteriaBuilder,
                                                                   boolean ignoreCase) {
        return ignoreCase ? criteriaBuilder.lower(root.get(field)) : root.get(field);
    }

    private static String toLowerCaseIfIgnoreCase(String value, boolean ignoreCase) {
        return ignoreCase ? value.toLowerCase() : value;
    }
}