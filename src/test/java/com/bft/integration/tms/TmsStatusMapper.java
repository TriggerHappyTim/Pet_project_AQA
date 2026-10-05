package com.bft.integration.tms;

/**
 * Maps internal {@link com.bft.integration.mapper.TestStatus} values
 * to TMS-specific test result status IDs.
 *
 * <p>Default IDs follow the reference TMS setup:
 * Pass=94, Failed=95, Disabled=96. Can be overridden via properties:
 * {@code tms.status.pass}, {@code tms.status.fail}, {@code tms.status.skip}.
 */
public final class TmsStatusMapper {

    private TmsStatusMapper() {
    }

    /**
     * Returns the TMS status ID for a given status name.
     *
     * @param status one of PASS / FAIL / SKIP / BLOCKED / etc.
     * @return TMS numeric status id, or -1 if not mappable
     */
    public static int toStatusId(String status) {
        switch (status) {
            case "PASS":
            case "BLOCKED":
                return statusIdOf("pass", 94);
            case "FAIL":
                return statusIdOf("fail", 95);
            case "SKIP":
            case "NOT_EXECUTED":
                return statusIdOf("skip", 96);
            default:
                return -1;
        }
    }

    /**
     * Returns the TMS status ID for a TestStatus enum value.
     */
    public static int toStatusId(com.bft.integration.mapper.TestStatus status) {
        if (status == null) return -1;
        return toStatusId(status.name());
    }

    private static int statusIdOf(String key, int defaultId) {
        String raw = System.getProperty("tms.status." + key,
                System.getenv("TMS_STATUS_" + key.toUpperCase()));
        if (raw == null || raw.isEmpty()) {
            return defaultId;
        }
        try {
            return Integer.parseInt(raw.trim());
        } catch (NumberFormatException e) {
            return defaultId;
        }
    }
}
