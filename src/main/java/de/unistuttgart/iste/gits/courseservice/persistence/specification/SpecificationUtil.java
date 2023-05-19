package de.unistuttgart.iste.gits.courseservice.persistence.specification;

import de.unistuttgart.iste.gits.courseservice.dto.DateTimeFilterDto;
import de.unistuttgart.iste.gits.courseservice.dto.IntFilterDto;
import de.unistuttgart.iste.gits.courseservice.dto.StringFilterDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class SpecificationUtil {

    public static <EntityType, DtoType> Specification<EntityType> not(
            DtoType not,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Optional.ofNullable(not)
                .map(dtoToSpecification)
                .map(Specification::not)
                .orElse(alwaysTrue());
    }

    public static <EntityType, DtoType> Specification<EntityType> and(
            @Nullable List<DtoType> ands,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Stream.ofNullable(ands)
                .flatMap(List::stream)
                .map(dtoToSpecification)
                .reduce(alwaysTrue(), Specification::and);
    }

    public static <EntityType, DtoType> Specification<EntityType> or(
            @Nullable List<DtoType> ors,
            Function<DtoType, Specification<EntityType>> dtoToSpecification) {

        return Stream.ofNullable(ors)
                .flatMap(List::stream)
                .map(dtoToSpecification)
                .reduce(alwaysFalse(), Specification::or);
    }

    public static <T> Specification<T> booleanFilter(String field, @Nullable Boolean value) {
        if (value == null) {
            return alwaysTrue();
        }

        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    public static <T> Specification<T> stringFilter(String field, @Nullable StringFilterDto stringFilterDto) {
        if (stringFilterDto == null) {
            return alwaysTrue();
        }

        return Specification
                .<T>where(getContainsSpecification(stringFilterDto, field))
                .and(getEqualsSpecification(stringFilterDto, field));
    }

    private static <T> Specification<T> getContainsSpecification(StringFilterDto stringFilterDto, String field) {
        return Optional.ofNullable(stringFilterDto.getContains())
                .map(value -> SpecificationUtil.<T>like(field, "%" + value + "%", stringFilterDto.getIgnoreCase()))
                .orElse(alwaysTrue());
    }

    private static <T> Specification<T> getEqualsSpecification(StringFilterDto stringFilterDto, String field) {
        return Optional.ofNullable(stringFilterDto.getEquals())
                .map(value -> SpecificationUtil.<T>equals(field, value))
                .orElse(alwaysTrue());
    }

    private static String toLowerCaseIfIgnoreCase(String value, boolean ignoreCase) {
        return ignoreCase ? value.toLowerCase() : value;
    }

    public static <T> Specification<T> dateTimeFilter(String field, @Nullable DateTimeFilterDto dateTimeFilterDto) {
        if (dateTimeFilterDto == null) {
            return alwaysTrue();
        }

        return Specification.<T>where(getEqualsSpecification(dateTimeFilterDto, field))
                .and(getBeforeSpecification(dateTimeFilterDto, field))
                .and(getAfterSpecification(dateTimeFilterDto, field));
    }

    private static <T> Specification<T> getEqualsSpecification(DateTimeFilterDto stringFilterDto, String field) {
        return Optional.ofNullable(stringFilterDto.getEquals())
                .map(value -> SpecificationUtil.<T>equals(field, value))
                .orElse(alwaysTrue());
    }

    private static <T> Specification<T> getBeforeSpecification(DateTimeFilterDto stringFilterDto, String field) {
        return Optional.ofNullable(stringFilterDto.getBefore())
                .map(value -> SpecificationUtil.<T, OffsetDateTime>lessThan(field, value))
                .orElse(alwaysTrue());
    }

    private static <T> Specification<T> getAfterSpecification(DateTimeFilterDto stringFilterDto, String field) {
        return Optional.ofNullable(stringFilterDto.getAfter())
                .map(value -> SpecificationUtil.<T, OffsetDateTime>greaterThan(field, value))
                .orElse(alwaysTrue());
    }

    public static <T> Specification<T> intFilter(String field, @Nullable IntFilterDto intFilterDto) {
        if (intFilterDto == null) {
            return alwaysTrue();
        }

        return Specification.<T>where(getEqualsSpecification(intFilterDto, field))
                .and(getGreaterThanSpecification(intFilterDto, field))
                .and(getLessThanSpecification(intFilterDto, field));
    }

    public static <T> Specification<T> getEqualsSpecification(IntFilterDto intFilterDto, String field) {
        return Optional.ofNullable(intFilterDto.getEquals())
                .map(value -> SpecificationUtil.<T>equals(field, value))
                .orElse(alwaysTrue());
    }

    public static <T> Specification<T> getGreaterThanSpecification(IntFilterDto intFilterDto, String field) {
        return Optional.ofNullable(intFilterDto.getGreaterThan())
                .map(value -> SpecificationUtil.<T, Integer>greaterThan(field, value))
                .orElse(alwaysTrue());
    }

    public static <T> Specification<T> getLessThanSpecification(IntFilterDto intFilterDto, String field) {
        return Optional.ofNullable(intFilterDto.getLessThan())
                .map(value -> SpecificationUtil.<T, Integer>lessThan(field, value))
                .orElse(alwaysTrue());
    }

    public static <T> Specification<T> alwaysTrue() {
        return Specification.where(null);
    }

    public static <T> Specification<T> alwaysFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
    }

    private static <T> Specification<T> like(String field, String value, boolean ignoreCase) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> path = ignoreCase ? criteriaBuilder.lower(root.get(field)) : root.get(field);
            return criteriaBuilder.like(path, toLowerCaseIfIgnoreCase(value, ignoreCase));
        };
    }

    public static <T> Specification<T> equals(String field, String value, boolean ignoreCase) {
        return (root, query, criteriaBuilder) -> {
            Expression<String> path = towLowerCasePathIfIgnoreCase(field, root, criteriaBuilder, ignoreCase);
            return criteriaBuilder.equal(path, toLowerCaseIfIgnoreCase(value, ignoreCase));
        };
    }

    public static <T> Specification<T> equals(String field, Object value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(field), value);
    }

    private static <T> Expression<String> towLowerCasePathIfIgnoreCase(String field,
                                                                       Root<T> root,
                                                                       CriteriaBuilder criteriaBuilder,
                                                                       boolean ignoreCase) {
        return ignoreCase ? criteriaBuilder.lower(root.get(field)) : root.get(field);
    }

    private static <T, C extends Comparable<C>> Specification<T> lessThan(String field, C value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get(field), value);
    }

    private static <T, C extends Comparable<C>> Specification<T> greaterThan(String field, C value) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.greaterThan(root.get(field), value);
    }
}