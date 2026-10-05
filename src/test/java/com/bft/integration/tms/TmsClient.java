package com.bft.integration.tms;

import com.bft.integration.mapper.TestStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.qameta.allure.Step;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * REST client for the Jira TMS (Test Management) API.
 *
 * <p>Endpoints used:
 * <ul>
 *   <li>{@code GET  /rest/tests/1.0/testrun/{id}/testrunitems} — resolve case id by @TmsLink</li>
 *   <li>{@code PUT  /rest/tests/1.0/testresult} — update test result status</li>
 * </ul>
 *
 * <p>All failures are logged as warnings and never propagate — a TMS outage
 * must not break test runs.
 */
public class TmsClient {

    private static final Logger log = LoggerFactory.getLogger(TmsClient.class);

    private static final String TEST_RUN_ITEMS_PATH =
            "/rest/tests/1.0/testrun/%s/testrunitems?fields=id,index,issueCount,$lastTestResult";
    private static final String TEST_RESULT_PATH = "/rest/tests/1.0/testresult";

    private final TmsConfig config;

    public TmsClient() {
        this(TmsConfig.getInstance());
    }

    public TmsClient(TmsConfig config) {
        this.config = config;
    }

    /**
     * Resolves the internal test-result id for a test case key inside the configured test run.
     *
     * @param tmsLink value of {@code @TmsLink} on the test method (e.g., "EVS-T-101")
     * @return internal result id, or -1 if not found
     */
    @Step("TMS: find test case {tmsLink} in run {config.testRunId}")
    public int findCaseId(String tmsLink) {
        try {
            TestRunItems response = given()
                    .contentType(ContentType.JSON)
                    .urlEncodingEnabled(false)
                    .header("Authorization", "Bearer " + config.getToken())
                    .when()
                    .get(config.getBaseUrl() + String.format(TEST_RUN_ITEMS_PATH, config.getTestRunId()))
                    .then()
                    .statusCode(200)
                    .extract().body().as(TestRunItems.class);

            return response.getItems().stream()
                    .filter(item -> tmsLink.equals(item.getCaseKey()))
                    .findFirst()
                    .map(Item::getCaseId)
                    .orElse(-1);
        } catch (Exception e) {
            log.warn("TMS: failed to resolve case id for '{}': {}", tmsLink, e.getMessage());
            return -1;
        }
    }

    /**
     * Updates the status of a test result.
     *
     * @param caseId   internal test result id (from {@link #findCaseId(String)})
     * @param status   new status
     * @param comment  optional execution comment (may be null)
     * @return true on success
     */
    @Step("TMS: set case {caseId} status to {status}")
    public boolean setStatus(int caseId, TestStatus status, String comment) {
        int statusId = TmsStatusMapper.toStatusId(status);
        if (statusId < 0 || caseId <= 0) {
            log.debug("TMS: skipping status update (caseId={}, status={})", caseId, status);
            return false;
        }

        try {
            List<Map<String, Object>> body = new ArrayList<>();
            body.add(Map.ofEntries(
                    Map.entry("id", caseId),
                    Map.entry("testResultStatusId", statusId),
                    Map.entry("userKey", config.getUserKey()),
                    Map.entry("actualStartDate", LocalDate.now().toString()),
                    Map.entry("executionDate", LocalDate.now().toString()),
                    Map.entry("comment", comment == null ? "" : comment)
            ));

            given()
                    .contentType(ContentType.JSON)
                    .header("Authorization", "Bearer " + config.getToken())
                    .header("jira-project-id", config.getProjectId())
                    .body(body)
                    .when()
                    .put(config.getBaseUrl() + TEST_RESULT_PATH)
                    .then()
                    .statusCode(200);

            log.info("TMS: updated case {} -> {}", caseId, status);
            return true;
        } catch (Exception e) {
            log.warn("TMS: failed to update case {} status: {}", caseId, e.getMessage());
            return false;
        }
    }

    /**
     * Convenience method: resolves case id by tmsLink and updates its status.
     *
     * @param tmsLink value of {@code @TmsLink}
     * @param status  new status
     * @param comment optional comment
     * @return true on success
     */
    public boolean reportResult(String tmsLink, TestStatus status, String comment) {
        if (!config.isEnabled()) {
            return false;
        }
        int caseId = findCaseId(tmsLink);
        if (caseId <= 0) {
            log.warn("TMS: case '{}' not found in test run {}", tmsLink, config.getTestRunId());
            return false;
        }
        return setStatus(caseId, status, comment);
    }

    /** Minimal DTO for the testrunitems response. */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestRunItems {
        public List<Item> items;

        public List<Item> getItems() {
            return items == null ? List.of() : items;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Item {
        @JsonProperty("$lastTestResult")
        public LastResult lastTestResult;

        Integer getCaseId() {
            return lastTestResult != null ? lastTestResult.id : null;
        }

        String getCaseKey() {
            return lastTestResult != null && lastTestResult.testCase != null
                    ? lastTestResult.testCase.key : null;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class LastResult {
        public Integer id;
        public TestCaseRef testCase;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class TestCaseRef {
        public String key;
    }
}
