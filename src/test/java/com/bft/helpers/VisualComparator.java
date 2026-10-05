package com.bft.helpers;

import com.bft.pw.PwElement;
import com.bft.pw.PwSession;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Visual regression comparator: compares current page/element screenshots
 * with reference images stored in {@code src/test/resources/screens/}.
 *
 * <p>Configuration (system property / env var):
 * <ul>
 *   <li>{@code evs.visual.threshold} / {@code EVS_VISUAL_THRESHOLD} —
 *       max allowed percent of differing pixels, default 1.0</li>
 *   <li>{@code evs.visual.update} / {@code EVS_VISUAL_UPDATE} —
 *       when true, actual screenshots overwrite references instead of failing</li>
 * </ul>
 *
 * <p>Artifacts:
 * <ul>
 *   <li>References: {@code src/test/resources/screens/<name>.png}</li>
 *   <li>Diffs: {@code build/reports/visual-diff/<name>-diff.png}</li>
 *   <li>Allure attachments: reference / actual / diff images</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * VisualComparator visual = new VisualComparator();
 * // soft: returns result and attaches artifacts to Allure
 * boolean ok = visual.matchesPage("login-page");
 * // hard integration with soft assertions:
 * visual.assertMatchesPage(assertions, "login-page");
 * }</pre>
 */
public class VisualComparator {

    private static final Logger log = LoggerFactory.getLogger(VisualComparator.class);

    private static final String REFERENCE_DIR = "screens";
    private static final String DIFF_DIR = "build/reports/visual-diff";
    private static final double DEFAULT_THRESHOLD = 1.0;

    /**
     * Comparison outcome.
     */
    public enum Outcome { MATCH, MISMATCH, REFERENCE_MISSING }

    /** Result of a single comparison. */
    public static class Result {
        public final Outcome outcome;
        public final double differencePercent;
        public final String referencePath;
        public final String diffPath;

        Result(Outcome outcome, double differencePercent, String referencePath, String diffPath) {
            this.outcome = outcome;
            this.differencePercent = differencePercent;
            this.referencePath = referencePath;
            this.diffPath = diffPath;
        }
    }

    // ==================== Page-level API ====================

    /**
     * Compares the current full-page screenshot against reference {@code screens/<name>.png}.
     *
     * @param name reference name (without extension)
     * @return true if within threshold (or updated)
     */
    public boolean matchesPage(String name) {
        byte[] shot = capturePage();
        if (shot == null || shot.length == 0) {
            log.warn("Visual: cannot capture page screenshot for '{}'", name);
            return false;
        }
        return compare(name, shot).outcome == Outcome.MATCH;
    }

    /**
     * Soft-asserts that the current page matches the reference.
     */
    public void assertMatchesPage(com.bft.test.TestAssertions assertions, String name) {
        byte[] shot = capturePage();
        if (shot == null || shot.length == 0) {
            log.warn("Visual: cannot capture page screenshot for '{}', skipping assertion", name);
            return;
        }
        assertWithResult(assertions, name, compare(name, shot));
    }

    // ==================== Element-level API ====================

    /**
     * Compares an element screenshot against reference.
     */
    public boolean matchesElement(PwElement element, String name) {
        return compare(name, element.screenshot()).outcome == Outcome.MATCH;
    }

    /**
     * Soft-asserts that the element matches the reference.
     */
    public void assertMatchesElement(com.bft.test.TestAssertions assertions,
                                     PwElement element, String name) {
        assertWithResult(assertions, name, compare(name, element.screenshot()));
    }

    // ==================== Core comparison ====================

    /**
     * Compares raw PNG bytes against the stored reference.
     */
    public Result compare(String name, byte[] actualPng) {
        try {
            BufferedImage actual = ImageIO.read(new ByteArrayInputStream(actualPng));
            Path referenceFile = Paths.get("src", "test", "resources", REFERENCE_DIR, name + ".png");

            boolean updateMode = resolveBool("evs.visual.update", "EVS_VISUAL_UPDATE");

            if (!Files.exists(referenceFile)) {
                if (updateMode) {
                    save(referenceFile, actual);
                    log.info("Visual: reference '{}' created in update mode", referenceFile);
                } else {
                    log.warn("Visual: reference '{}' not found. Run with -Devs.visual.update=true to create",
                            referenceFile);
                    attachImage("visual/" + name + "/actual", actual);
                }
                return new Result(Outcome.REFERENCE_MISSING, 0, referenceFile.toString(), null);
            }

            BufferedImage expected = ImageIO.read(referenceFile.toFile());

            ImageComparison comparison = new ImageComparison(expected, actual)
                    .setAllowingPercentOfDifferentPixels(resolveThreshold());

            ImageComparisonResult comparisonResult = comparison.compareImages();

            float diffPercent = comparisonResult.getDifferencePercent();
            boolean matched = comparisonResult.getImageComparisonState() == ImageComparisonState.MATCH
                    || (comparisonResult.getImageComparisonState() == ImageComparisonState.MISMATCH
                        && diffPercent <= resolveThreshold());

            if (!matched) {
                String diffPath = saveDiff(name, comparisonResult.getResult());
                attachImage("visual/" + name + "/reference", expected);
                attachImage("visual/" + name + "/actual", actual);
                attachImage("visual/" + name + "/diff", comparisonResult.getResult());
                log.warn("Visual mismatch for '{}': {}% differs (threshold {}%), diff: {}",
                        name, diffPercent, resolveThreshold(), diffPath);
            } else {
                log.debug("Visual match for '{}': {}% differs", name, diffPercent);
            }

            return new Result(matched ? Outcome.MATCH : Outcome.MISMATCH,
                    diffPercent, referenceFile.toString(), null);
        } catch (Exception e) {
            log.error("Visual comparison failed for '{}': {}", name, e.getMessage(), e);
            return new Result(Outcome.MISMATCH, 100, null, null);
        }
    }

    private void assertWithResult(com.bft.test.TestAssertions assertions, String name, Result result) {
        switch (result.outcome) {
            case MATCH:
                break;
            case REFERENCE_MISSING:
                log.warn("Visual '{}': no reference — skipping assertion", name);
                break;
            case MISMATCH:
            default:
                assertions.assertTrue(false,
                        "Visual mismatch for '" + name + "': "
                                + String.format("%.2f%%", result.differencePercent)
                                + " of pixels differ; diff saved at " + result.diffPath);
                break;
        }
    }

    // ==================== Helpers ====================

    private byte[] capturePage() {
        try {
            return PwSession.page().screenshot(
                    new com.microsoft.playwright.Page.ScreenshotOptions().setFullPage(true));
        } catch (Exception e) {
            log.warn("Visual: failed to capture page screenshot: {}", e.getMessage());
            return new byte[0];
        }
    }

    private String saveDiff(String name, BufferedImage diff) {
        try {
            Path dir = Paths.get(DIFF_DIR);
            Files.createDirectories(dir);
            String safe = name.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path file = dir.resolve(safe + "-diff.png");
            ImageComparisonUtil.saveImage(file.toFile(), diff);
            return file.toAbsolutePath().toString();
        } catch (Exception e) {
            log.warn("Visual: failed to save diff image: {}", e.getMessage());
            return null;
        }
    }

    private void save(Path path, BufferedImage image) throws Exception {
        Files.createDirectories(path.getParent());
        ImageIO.write(image, "png", path.toFile());
    }

    private void attachImage(String attachmentName, BufferedImage image) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ImageIO.write(image, "png", buffer);
            InputStream is = new ByteArrayInputStream(buffer.toByteArray());
            Allure.addAttachment(attachmentName, "image/png", is, "png");
        } catch (Exception e) {
            log.debug("Visual: failed to attach image '{}': {}", attachmentName, e.getMessage());
        }
    }

    private double resolveThreshold() {
        String raw = System.getProperty("evs.visual.threshold",
                System.getenv("EVS_VISUAL_THRESHOLD"));
        if (raw == null || raw.isEmpty()) {
            return DEFAULT_THRESHOLD;
        }
        try {
            double v = Double.parseDouble(raw.trim());
            return v < 0 ? 0 : Math.min(v, 100);
        } catch (NumberFormatException e) {
            return DEFAULT_THRESHOLD;
        }
    }

    private boolean resolveBool(String sysProp, String envVar) {
        String value = System.getProperty(sysProp);
        if (value != null && !value.isEmpty()) {
            return Boolean.parseBoolean(value);
        }
        value = System.getenv(envVar);
        return value != null && Boolean.parseBoolean(value);
    }
}
