package com.bft.pw;

/**
 * Имитация {@code org.openqa.selenium.interactions.Actions}.
 */
public class Actions {

    private final PwDriver driver;
    private SelenideElement target;
    private CharSequence[] keys;

    private enum Operation { NONE, DOUBLE_CLICK, CLICK, MOVE, SEND_KEYS, DRAG_AND_DROP }

    private Operation operation = Operation.NONE;
    private SelenideElement source;
    private SelenideElement destination;

    public Actions(PwDriver driver) {
        this.driver = driver;
    }

    public Actions(PwDriver driver, int delay) {
        this(driver);
    }

    public Actions doubleClick(SelenideElement element) {
        this.target = element;
        this.operation = Operation.DOUBLE_CLICK;
        return this;
    }

    public Actions click(SelenideElement element) {
        this.target = element;
        this.operation = Operation.CLICK;
        return this;
    }

    public Actions click() {
        this.operation = Operation.CLICK;
        return this;
    }

    public Actions moveToElement(SelenideElement element) {
        this.target = element;
        this.operation = Operation.MOVE;
        return this;
    }

    public Actions sendKeys(CharSequence... keys) {
        this.keys = keys;
        this.operation = Operation.SEND_KEYS;
        return this;
    }

    public Actions pause(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return this;
    }

    public Actions keyDown(CharSequence key) {
        String mapped = Keys.toPlaywrightKey(key.toString());
        if (mapped != null) {
            PwSession.page().keyboard().down(mapped);
        }
        return this;
    }

    public Actions keyUp(CharSequence key) {
        String mapped = Keys.toPlaywrightKey(key.toString());
        if (mapped != null) {
            PwSession.page().keyboard().up(mapped);
        }
        return this;
    }

    public Actions dragAndDrop(SelenideElement source, SelenideElement destination) {
        this.source = source;
        this.destination = destination;
        this.operation = Operation.DRAG_AND_DROP;
        return this;
    }

    public Actions perform() {
        switch (operation) {
            case DOUBLE_CLICK:
                if (target instanceof PwElement) {
                    ((PwElement) target).doubleClick();
                } else if (target != null) {
                    target.doubleClick();
                }
                break;
            case CLICK:
                if (target != null) {
                    target.click();
                } else {
                    PwSession.page().mouse().click(0, 0);
                }
                break;
            case MOVE:
                if (target != null) {
                    target.hover();
                }
                break;
            case SEND_KEYS:
                if (keys != null && keys.length > 0) {
                    String single = keys[0].toString();
                    String mapped = Keys.toPlaywrightKey(single);
                    if (single.length() == 1 && mapped != null) {
                        PwSession.page().keyboard().press(mapped);
                    } else {
                        PwSession.page().keyboard().type(single);
                    }
                }
                break;
            case DRAG_AND_DROP:
                if (source instanceof PwElement && destination instanceof PwElement) {
                    com.microsoft.playwright.Locator src = ((PwElement) source).locator();
                    com.microsoft.playwright.Locator dst = ((PwElement) destination).locator();
                    com.microsoft.playwright.options.BoundingBox srcBox = src.boundingBox();
                    com.microsoft.playwright.options.BoundingBox dstBox = dst.boundingBox();
                    if (srcBox != null && dstBox != null) {
                        com.microsoft.playwright.Page page = PwSession.page();
                        page.mouse().move(srcBox.x + srcBox.width / 2, srcBox.y + srcBox.height / 2);
                        page.mouse().down();
                        page.mouse().move(dstBox.x + dstBox.width / 2, dstBox.y + dstBox.height / 2,
                                new com.microsoft.playwright.Mouse.MoveOptions().setSteps(5));
                        page.mouse().up();
                    }
                }
                break;
            default:
                break;
        }
        return this;
    }

    public Actions build() {
        return this;
    }
}