package com.bft.pw;

/**
 * Условия для коллекций элементов, совместимые с Selenide {@code CollectionCondition}.
 */
public abstract class CollectionCondition {

    private final String name;

    protected CollectionCondition(String name) {
        this.name = name;
    }

    public abstract boolean check(PwElements elements);

    @Override
    public String toString() {
        return name;
    }

    public static CollectionCondition size(int expectedSize) {
        return new CollectionCondition("size(" + expectedSize + ")") {
            @Override
            public boolean check(PwElements elements) {
                return elements.size() == expectedSize;
            }
        };
    }

    public static CollectionCondition sizeGreaterThan(int expectedSize) {
        return new CollectionCondition("sizeGreaterThan(" + expectedSize + ")") {
            @Override
            public boolean check(PwElements elements) {
                return elements.size() > expectedSize;
            }
        };
    }

    public static CollectionCondition sizeGreaterThanOrEqual(int expectedSize) {
        return new CollectionCondition("sizeGreaterThanOrEqual(" + expectedSize + ")") {
            @Override
            public boolean check(PwElements elements) {
                return elements.size() >= expectedSize;
            }
        };
    }

    public static CollectionCondition empty() {
        return new CollectionCondition("empty") {
            @Override
            public boolean check(PwElements elements) {
                return elements.isEmpty();
            }
        };
    }
}