package de.unistuttgart.iste.gits.common.util;

import de.unistuttgart.iste.gits.generated.dto.DateTimeFilterDto;
import de.unistuttgart.iste.gits.generated.dto.IntFilterDto;
import de.unistuttgart.iste.gits.generated.dto.StringFilterDto;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.function.Function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * A very rudimentary test for the SpecificationUtil class.
 * Note that spring specifications are hard to test in a unit test, as
 * they provide no useful methods to test the specification itself.
 * Users of the SpecificationUtil class should test their specifications in an integration test.
 * <p>
 * In this test, we only test null safety and the correct invocation of the criteria builder methods.
 */
public class TestSpecificationUtil {

    private final CriteriaBuilder criteriaBuilder = spy(CriteriaBuilder.class);
    @SuppressWarnings("unchecked")
    private final Root<Object> root = mock(Root.class);
    private final CriteriaQuery<?> criteriaQuery = mock(CriteriaQuery.class);

    /**
     * Given a null value in any of the SpecificationUtil methods
     * When the method is called
     * Then the default specification is returned
     */
    @Test
    public void testNullSafety() {
        Function<Object, Specification<Object>> dummyFunction = o -> Specification.where(null);

        assertThat(SpecificationUtil.stringFilter("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.dateTimeFilter("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.intFilter("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.booleanFilter("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.and(null, dummyFunction),
                equalTo(SpecificationUtil.alwaysTrue()));

        // only exception whereby default always false is returned
        assertThat(SpecificationUtil.or(null, dummyFunction),
                equalTo(SpecificationUtil.alwaysFalse()));

        assertThat(SpecificationUtil.not(null, dummyFunction),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.equalTo("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.stringEqualTo("test", null, true),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.greaterThan("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.lessThan("test", null),
                equalTo(SpecificationUtil.alwaysTrue()));
        assertThat(SpecificationUtil.contains("test", null, true),
                equalTo(SpecificationUtil.alwaysTrue()));
    }

    /**
     * Given a string filter dto
     * When stringFilter is called
     * Then the correct methods of the criteria builder are called
     */
    @Test
    public void testStringFilter() {
        Specification<Object> specification = SpecificationUtil.stringFilter("test", StringFilterDto.builder()
                .setContains("testContains")
                .setEquals("testEquals")
                .build());

        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).like(any(), eq("%testContains%"));
        verify(criteriaBuilder, times(1)).equal(any(), eq("testEquals"));
    }

    /**
     * Given a date time filter dto
     * When dateTimeFilter is called
     * Then the correct methods of the criteria builder are called
     */
    @Test
    public void testDateTimeFilter() {
        var after = LocalDate.of(2022, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC);
        var before = LocalDate.of(2023, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC);
        Specification<Object> specification = SpecificationUtil.dateTimeFilter("test", DateTimeFilterDto.builder()
                .setAfter(after)
                .setBefore(before)
                .build());

        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).greaterThan(any(), eq(after));
        verify(criteriaBuilder, times(1)).lessThan(any(), eq(before));
    }

    /**
     * Given a boolean
     * When booleanFilter is called
     * Then the correct methods of the criteria builder are called
     */
    @Test
    public void testBooleanFilter() {
        Specification<Object> specification = SpecificationUtil.booleanFilter("test", true);

        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).equal(any(), eq(true));
    }

    /**
     * Given an int filter dto
     * When intFilter is called
     * Then the correct methods of the criteria builder are called
     */
    @Test
    public void testIntFilter() {
        Specification<Object> specification = SpecificationUtil.intFilter("test",
                IntFilterDto.builder().setEquals(1).setGreaterThan(2).setLessThan(3).build());

        specification.toPredicate(root, criteriaQuery, criteriaBuilder);

        verify(criteriaBuilder, times(1)).equal(any(), eq(1));
        verify(criteriaBuilder, times(1)).greaterThan(any(), eq(2));
        verify(criteriaBuilder, times(1)).lessThan(any(), eq(3));
    }
}
