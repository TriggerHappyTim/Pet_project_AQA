package com.bft.pw;

import java.util.HashMap;
import java.util.Map;

/**
 * Клавиши клавиатуры, совместимые с {@code org.openqa.selenium.Keys}.
 */
public class Keys implements CharSequence {

    public static final CharSequence NULL = new Keys("\uE000");
    public static final CharSequence CANCEL = new Keys("\uE001");
    public static final CharSequence HELP = new Keys("\uE002");
    public static final CharSequence BACK_SPACE = new Keys("\uE003");
    public static final CharSequence TAB = new Keys("\uE004");
    public static final CharSequence CLEAR = new Keys("\uE005");
    public static final CharSequence RETURN = new Keys("\uE006");
    public static final CharSequence ENTER = new Keys("\uE007");
    public static final CharSequence SHIFT = new Keys("\uE008");
    public static final CharSequence LEFT_SHIFT = SHIFT;
    public static final CharSequence CONTROL = new Keys("\uE009");
    public static final CharSequence LEFT_CONTROL = CONTROL;
    public static final CharSequence ALT = new Keys("\uE00A");
    public static final CharSequence LEFT_ALT = ALT;
    public static final CharSequence PAUSE = new Keys("\uE00B");
    public static final CharSequence ESCAPE = new Keys("\uE00C");
    public static final CharSequence SPACE = new Keys("\uE00D");
    public static final CharSequence PAGE_UP = new Keys("\uE00E");
    public static final CharSequence PAGE_DOWN = new Keys("\uE00F");
    public static final CharSequence END = new Keys("\uE010");
    public static final CharSequence HOME = new Keys("\uE011");
    public static final CharSequence LEFT = new Keys("\uE012");
    public static final CharSequence ARROW_LEFT = LEFT;
    public static final CharSequence UP = new Keys("\uE013");
    public static final CharSequence ARROW_UP = UP;
    public static final CharSequence RIGHT = new Keys("\uE014");
    public static final CharSequence ARROW_RIGHT = RIGHT;
    public static final CharSequence DOWN = new Keys("\uE015");
    public static final CharSequence ARROW_DOWN = DOWN;
    public static final CharSequence INSERT = new Keys("\uE016");
    public static final CharSequence DELETE = new Keys("\uE017");
    public static final CharSequence SEMICOLON = new Keys("\uE018");
    public static final CharSequence EQUALS = new Keys("\uE019");
    public static final CharSequence NUMPAD0 = new Keys("\uE01A");
    public static final CharSequence NUMPAD1 = new Keys("\uE01B");
    public static final CharSequence NUMPAD2 = new Keys("\uE01C");
    public static final CharSequence NUMPAD3 = new Keys("\uE01D");
    public static final CharSequence NUMPAD4 = new Keys("\uE01E");
    public static final CharSequence NUMPAD5 = new Keys("\uE01F");
    public static final CharSequence NUMPAD6 = new Keys("\uE020");
    public static final CharSequence NUMPAD7 = new Keys("\uE021");
    public static final CharSequence NUMPAD8 = new Keys("\uE022");
    public static final CharSequence NUMPAD9 = new Keys("\uE023");
    public static final CharSequence MULTIPLY = new Keys("\uE024");
    public static final CharSequence ADD = new Keys("\uE025");
    public static final CharSequence SEPARATOR = new Keys("\uE026");
    public static final CharSequence SUBTRACT = new Keys("\uE027");
    public static final CharSequence DECIMAL = new Keys("\uE028");
    public static final CharSequence DIVIDE = new Keys("\uE029");
    public static final CharSequence F1 = new Keys("\uE031");
    public static final CharSequence F2 = new Keys("\uE032");
    public static final CharSequence F3 = new Keys("\uE033");
    public static final CharSequence F4 = new Keys("\uE034");
    public static final CharSequence F5 = new Keys("\uE035");
    public static final CharSequence F6 = new Keys("\uE036");
    public static final CharSequence F7 = new Keys("\uE037");
    public static final CharSequence F8 = new Keys("\uE038");
    public static final CharSequence F9 = new Keys("\uE039");
    public static final CharSequence F10 = new Keys("\uE03A");
    public static final CharSequence F11 = new Keys("\uE03B");
    public static final CharSequence F12 = new Keys("\uE03C");
    public static final CharSequence META = new Keys("\uE03D");
    public static final CharSequence COMMAND = META;

    private static final Map<String, String> KEY_MAP = new HashMap<>();

    static {
        KEY_MAP.put("\uE007", "Enter");
        KEY_MAP.put("\uE006", "Enter");
        KEY_MAP.put("\uE004", "Tab");
        KEY_MAP.put("\uE00C", "Escape");
        KEY_MAP.put("\uE009", "Control");
        KEY_MAP.put("\uE008", "Shift");
        KEY_MAP.put("\uE00A", "Alt");
        KEY_MAP.put("\uE003", "Backspace");
        KEY_MAP.put("\uE017", "Delete");
        KEY_MAP.put("\uE015", "ArrowDown");
        KEY_MAP.put("\uE013", "ArrowUp");
        KEY_MAP.put("\uE012", "ArrowLeft");
        KEY_MAP.put("\uE014", "ArrowRight");
        KEY_MAP.put("\uE011", "Home");
        KEY_MAP.put("\uE010", "End");
        KEY_MAP.put("\uE00F", "PageDown");
        KEY_MAP.put("\uE00E", "PageUp");
        KEY_MAP.put("\uE00D", " ");
        KEY_MAP.put("\uE016", "Insert");
        KEY_MAP.put("\uE03D", "Meta");
    }

    private final String key;

    private Keys(String key) {
        this.key = key;
    }

    public static String toPlaywrightKey(String seleniumKeyChar) {
        return KEY_MAP.get(seleniumKeyChar);
    }

    public static String chord(CharSequence... values) {
        StringBuilder sb = new StringBuilder();
        for (CharSequence value : values) {
            sb.append(value);
        }
        return sb.toString();
    }

    @Override
    public int length() {
        return key.length();
    }

    @Override
    public char charAt(int index) {
        return key.charAt(index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        return key.subSequence(start, end);
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CharSequence)) {
            return false;
        }
        return key.equals(o.toString());
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}