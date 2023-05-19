package de.unistuttgart.iste.gits.util;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test extension that clears the database after each test.
 * <p>
 * Usage:
 * <pre>
 *     &#64;ExtendWith(ClearDatabase.class)
 *     public class MyTest {
 *       // ...
 * </pre>
 * This extension is automatically used by {@link GraphQlApiTest}.
 */
public class ClearDatabase implements AfterEachCallback, BeforeAllCallback {

    private DataSource dataSource;
    private List<String> tables;

    @Override
    public void beforeAll(ExtensionContext context) {
        dataSource = SpringExtension.getApplicationContext(context).getBean("dataSource", DataSource.class);
        tables = getTableNames(dataSource);
    }

    @Override
    public void afterEach(ExtensionContext context) {
        var template = new JdbcTemplate(dataSource);
        JdbcTestUtils.deleteFromTables(template, tables.toArray(new String[0]));
    }

    /**
     * Returns all table names of the database
     *
     * @param dataSource the datasource
     * @return a list of table names
     */
    private List<String> getTableNames(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            List<String> result = new ArrayList<>();
            ResultSet tables = connection.getMetaData().getTables(null, null, "%", new String[]{"TABLE"});
            while (tables.next()) {
                result.add(tables.getString("TABLE_NAME"));
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
