package com.bft.pw;

import com.microsoft.playwright.Locator;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Обёртка над Playwright {@link Locator} для коллекций элементов.
 * Реализует {@link List} с кэшированием экземпляров для стабильной идентичности
 * (необходимо для {@code indexOf}/{@code stream}).
 */
public class PwElements implements ElementsCollection {

    private final Locator locator;
    private List<PwElement> cached;
    private int cachedSize = -1;
    private final boolean preset;

    public PwElements(Locator locator) {
        this.locator = locator;
        this.preset = false;
    }

    public PwElements(List<PwElement> preset) {
        this.locator = null;
        this.preset = true;
        this.cached = new ArrayList<>(preset);
        this.cachedSize = cached.size();
    }

    private List<PwElement> snapshot() {
        if (preset) {
            return cached;
        }
        int count = locator.count();
        if (cached == null || cachedSize != count) {
            cached = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                cached.add(new PwElement(locator.nth(i)));
            }
            cachedSize = count;
        }
        return cached;
    }

    private PwElement elementAt(int index) {
        return snapshot().get(index);
    }

    @Override
    public int size() {
        return preset ? cachedSize : locator.count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public SelenideElement get(int index) {
        return elementAt(index);
    }

    @Override
    public SelenideElement first() {
        return elementAt(0);
    }

    @Override
    public SelenideElement last() {
        return elementAt(Math.max(0, size() - 1));
    }

    @Override
    public List<String> texts() {
        try {
            return locator.allInnerTexts();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    @Override
    public ElementsCollection shouldHave(CollectionCondition condition) {
        return waitFor(condition, Configuration.timeout, true);
    }

    @Override
    public ElementsCollection shouldHave(CollectionCondition condition, Duration timeout) {
        return waitFor(condition, timeout, true);
    }

    @Override
    public ElementsCollection should(CollectionCondition condition) {
        return shouldHave(condition);
    }

    @Override
    public ElementsCollection should(CollectionCondition condition, Duration timeout) {
        return shouldHave(condition, timeout);
    }

    @Override
    public ElementsCollection shouldBe(CollectionCondition condition) {
        return shouldHave(condition);
    }

    @Override
    public ElementsCollection shouldBe(CollectionCondition condition, Duration timeout) {
        return shouldHave(condition, timeout);
    }

    private ElementsCollection waitFor(CollectionCondition condition, Duration timeout, boolean expected) {
        long deadline = System.currentTimeMillis() + timeout.toMillis();
        while (System.currentTimeMillis() < deadline) {
            try {
                if (condition.check(this) == expected) {
                    return this;
                }
            } catch (Exception ignored) {
                // продолжаем опрос
            }
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw new TimeoutException(String.format(
                "Collection did not %s condition '%s' within %s (actual size=%d)",
                expected ? "satisfy" : "NOT satisfy", condition, timeout, size()));
    }

    @Override
    public ElementsCollection filter(Condition condition) {
        List<PwElement> filtered = new ArrayList<>();
        for (PwElement candidate : snapshot()) {
            if (condition.check(candidate)) {
                filtered.add(candidate);
            }
        }
        return new PwElements(filtered);
    }

    @Override
    public ElementsCollection filterBy(Condition condition) {
        return filter(condition);
    }

    @Override
    public SelenideElement find(Condition condition) {
        int count = size();
        for (int i = 0; i < count; i++) {
            PwElement candidate = elementAt(i);
            if (condition.check(candidate)) {
                return candidate;
            }
        }
        return elementAt(0);
    }

    @Override
    public SelenideElement findBy(Condition condition) {
        return find(condition);
    }

    // ================= List methods =================

    @Override
    public boolean contains(Object o) {
        return snapshot().contains(o);
    }

    @Override
    public Iterator<SelenideElement> iterator() {
        return new ArrayList<SelenideElement>(snapshot()).iterator();
    }

    @Override
    public Object[] toArray() {
        return snapshot().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return snapshot().toArray(a);
    }

    @Override
    public boolean add(SelenideElement selenideElement) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return snapshot().containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends SelenideElement> c) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public boolean addAll(int index, Collection<? extends SelenideElement> c) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public SelenideElement set(int index, SelenideElement element) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public void add(int index, SelenideElement element) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public SelenideElement remove(int index) {
        throw new UnsupportedOperationException("PwElements is read-only");
    }

    @Override
    public int indexOf(Object o) {
        return snapshot().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return snapshot().lastIndexOf(o);
    }

    @Override
    public ListIterator<SelenideElement> listIterator() {
        return new ArrayList<SelenideElement>(snapshot()).listIterator();
    }

    @Override
    public ListIterator<SelenideElement> listIterator(int index) {
        return new ArrayList<SelenideElement>(snapshot()).listIterator(index);
    }

    @Override
    public List<SelenideElement> subList(int fromIndex, int toIndex) {
        return new ArrayList<SelenideElement>(snapshot().subList(fromIndex, toIndex));
    }
}