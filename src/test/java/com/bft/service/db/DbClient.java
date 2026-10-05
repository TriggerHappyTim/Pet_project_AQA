package com.bft.service.db;

import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin JDBC client for direct database verification (Apache Phoenix / HBase).
 *
 * <p>All queries run through {@link PreparedStatement} (parameterized, no string
 * concatenation of user data). SQL and result summaries are attached to Allure.
 *
 * <p>Usage:
 * <pre>{@code
 * DbClient db = new DbClient();
 *
 * // Scalar lookup
 * Long count = db.queryScalar("SELECT COUNT(*) FROM reports WHERE snils = ?", "351-818-056-74");
 *
 * // Row lookup
 * Map<String, Object> row = db.queryRow(
 *     "SELECT status FROM requests WHERE number = ?", requestNumber);
 * assertions.assertEquals("SIGNED", row.get("STATUS"), "Request must be signed");
 *
 * // Existence check (soft)
 * if (db.rowExists("SELECT 1 FROM reports WHERE id = ?", id)) { ... }
 * }</pre>
 */
public class DbClient {

    private static final Logger log = LoggerFactory.getLogger(DbClient.class);

    private final DbConfig config;

    public DbClient() {
        this(DbConfig.getInstance());
    }

    public DbClient(DbConfig config) {
        this.config = config;
        if (!config.isEnabled()) {
            log.warn("DB: access not configured (-Ddb.url missing). Queries will fail fast.");
        }
    }

    /**
     * Executes a SELECT and returns all rows as ordered maps (column name → value).
     *
     * @throws DbQueryException on connection/query failure or when DB is not configured
     */
    public List<Map<String, Object>> query(String sql, Object... params) {
        requireEnabled();
        long start = System.currentTimeMillis();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, sql, params);
             ResultSet resultSet = statement.executeQuery()) {

            List<Map<String, Object>> rows = new ArrayList<>();
            ResultSetMetaData meta = resultSet.getMetaData();
            int columnCount = meta.getColumnCount();

            while (resultSet.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(meta.getColumnLabel(i), resultSet.getObject(i));
                }
                rows.add(row);
            }

            log.info("DB: '{}' -> {} row(s) in {} ms", summarize(sql), rows.size(),
                    System.currentTimeMillis() - start);
            attachToAllure(sql, params, rows.size() + " row(s)");
            return rows;
        } catch (Exception e) {
            log.error("DB: query failed '{}': {}", summarize(sql), e.getMessage());
            throw new DbQueryException("Query failed: " + summarize(sql), e);
        }
    }

    /**
     * Executes a SELECT and returns the first row, or null when empty.
     */
    public Map<String, Object> queryRow(String sql, Object... params) {
        List<Map<String, Object>> rows = query(sql, params);
        return rows.isEmpty() ? null : rows.get(0);
    }

    /**
     * Executes a SELECT and returns the first column of the first row, or null when empty.
     * Handy for COUNT(*)/MAX()/id lookups.
     */
    @SuppressWarnings("unchecked")
    public <T> T queryScalar(String sql, Object... params) {
        Map<String, Object> row = queryRow(sql, params);
        if (row == null || row.isEmpty()) {
            return null;
        }
        return (T) row.values().iterator().next();
    }

    /**
     * Returns true when the SELECT yields at least one row.
     */
    public boolean rowExists(String sql, Object... params) {
        List<Map<String, Object>> rows = query(limitOne(sql), params);
        return !rows.isEmpty();
    }

    /**
     * Returns the count produced by a {@code SELECT COUNT(*) ...} query as long.
     */
    public long queryCount(String fromClauseWithWhere, Object... params) {
        Long value = queryScalar("SELECT COUNT(*) " + fromClauseWithWhere, params);
        return value == null ? 0 : ((Number) value).longValue();
    }

    /**
     * Executes an UPDATE/DELETE/INSERT and returns the affected row count.
     */
    public int execute(String sql, Object... params) {
        requireEnabled();
        long start = System.currentTimeMillis();
        try (Connection connection = openConnection();
             PreparedStatement statement = prepare(connection, sql, params)) {

            int affected = statement.executeUpdate();
            log.info("DB: '{}' -> {} row(s) affected in {} ms", summarize(sql), affected,
                    System.currentTimeMillis() - start);
            attachToAllure(sql, params, affected + " row(s) affected");
            return affected;
        } catch (Exception e) {
            log.error("DB: update failed '{}': {}", summarize(sql), e.getMessage());
            throw new DbQueryException("Update failed: " + summarize(sql), e);
        }
    }

    // ==================== Internals ====================

    private Connection openConnection() throws Exception {
        if (config.getUser() != null && config.getPassword() != null) {
            return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
        }
        return DriverManager.getConnection(config.getUrl());
    }

    private PreparedStatement prepare(Connection connection, String sql, Object[] params)
            throws Exception {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }

    private void requireEnabled() {
        if (!config.isEnabled()) {
            throw new DbQueryException(
                    "Database is not configured. Set -Ddb.url=<jdbc url> or DB_URL env var.", null);
        }
    }

    /** Wraps the query into a LIMIT 1 form when it does not already limit results. */
    private String limitOne(String sql) {
        String upper = sql.toUpperCase().replaceAll("\\s+", " ").trim();
        if (upper.contains("LIMIT ") || upper.contains("FETCH FIRST")) {
            return sql;
        }
        return sql + " LIMIT 1";
    }

    private String summarize(String sql) {
        String oneLine = sql.replaceAll("\\s+", " ").trim();
        return oneLine.length() <= 120 ? oneLine : oneLine.substring(0, 120) + "...";
    }

    private void attachToAllure(String sql, Object[] params, String resultSummary) {
        try {
            StringBuilder text = new StringBuilder("SQL:\n").append(sql.trim()).append("\n\n");
            if (params != null && params.length > 0) {
                text.append("Params: ").append(java.util.Arrays.toString(params)).append("\n\n");
            }
            text.append("Result: ").append(resultSummary);
            Allure.addAttachment("DB query", "text/plain", text.toString(), ".txt");
        } catch (Exception e) {
            log.debug("DB: failed to attach to Allure: {}", e.getMessage());
        }
    }

    /** Runtime exception for database failures. */
    public static class DbQueryException extends RuntimeException {
        public DbQueryException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
