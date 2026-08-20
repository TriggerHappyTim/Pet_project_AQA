package com.bft.pw;

/**
 * Условия ожидания, совместимые с Selenide {@code Condition}.
 * Проверка выполняется через {@link PwElement}.
 */
public abstract class Condition {

    private final String name;

    protected Condition(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract boolean check(PwElement element);

    @Override
    public String toString() {
        return name;
    }

    // ================= Стандартные условия =================

    public static final Condition visible = new Condition("visible") {
        @Override
        public boolean check(PwElement element) {
            return element.isVisible();
        }
    };

    public static final Condition exist = new Condition("exist") {
        @Override
        public boolean check(PwElement element) {
            return element.count() > 0;
        }
    };

    public static final Condition enabled = new Condition("enabled") {
        @Override
        public boolean check(PwElement element) {
            return element.isEnabled();
        }
    };

    public static final Condition hidden = new Condition("hidden") {
        @Override
        public boolean check(PwElement element) {
            return !element.isVisible();
        }
    };

    public static final Condition disappear = new Condition("disappear") {
        @Override
        public boolean check(PwElement element) {
            return !element.exists() || !element.isVisible();
        }
    };

    public static final Condition appear = visible;

    public static final Condition checked = new Condition("checked") {
        @Override
        public boolean check(PwElement element) {
            return element.isChecked();
        }
    };

    public static final Condition empty = new Condition("empty") {
        @Override
        public boolean check(PwElement element) {
            String value = element.getValue();
            return value == null || value.isEmpty();
        }
    };

    public static final Condition notEmpty = new Condition("not empty") {
        @Override
        public boolean check(PwElement element) {
            String value = element.getValue();
            return value != null && !value.isEmpty();
        }
    };

    // ================= Условия с параметрами =================

    public static Condition text(String text) {
        return new Condition("text(" + text + ")") {
            @Override
            public boolean check(PwElement element) {
                return element.getText().contains(text);
            }
        };
    }

    public static Condition exactText(String text) {
        return new Condition("exactText(" + text + ")") {
            @Override
            public boolean check(PwElement element) {
                return element.getText().trim().equals(text.trim());
            }
        };
    }

    public static Condition value(String value) {
        return new Condition("value(" + value + ")") {
            @Override
            public boolean check(PwElement element) {
                String actual = element.getValue();
                return actual != null && actual.equals(value);
            }
        };
    }

    public static Condition cssClass(String cssClass) {
        return new Condition("cssClass(" + cssClass + ")") {
            @Override
            public boolean check(PwElement element) {
                String attr = element.getAttribute("class");
                return attr != null && attr.contains(cssClass);
            }
        };
    }

    public static Condition id(String id) {
        return new Condition("id(" + id + ")") {
            @Override
            public boolean check(PwElement element) {
                return id.equals(element.getAttribute("id"));
            }
        };
    }

    public static Condition attribute(String name, String value) {
        return new Condition("attribute(" + name + "=" + value + ")") {
            @Override
            public boolean check(PwElement element) {
                return value.equals(element.getAttribute(name));
            }
        };
    }

    public static Condition and(String name, Condition... conditions) {
        return new Condition(name) {
            @Override
            public boolean check(PwElement element) {
                for (Condition condition : conditions) {
                    if (!condition.check(element)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public static Condition or(String name, Condition... conditions) {
        return new Condition(name) {
            @Override
            public boolean check(PwElement element) {
                for (Condition condition : conditions) {
                    if (condition.check(element)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Condition not(String name, Condition condition) {
        return new Condition("not " + name) {
            @Override
            public boolean check(PwElement element) {
                return !condition.check(element);
            }
        };
    }
}