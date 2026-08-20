package com.bft.pw;

import java.time.Duration;
import java.util.List;

/**
 * Поверхность API, совместимая с Selenide {@code ElementsCollection}.
 * Является {@link List} для совместимости с stream/indexOf/итерацией.
 */
public interface ElementsCollection extends List<SelenideElement> {

    SelenideElement first();

    SelenideElement last();

    List<String> texts();

    ElementsCollection shouldHave(CollectionCondition condition);

    ElementsCollection shouldHave(CollectionCondition condition, Duration timeout);

    ElementsCollection should(CollectionCondition condition);

    ElementsCollection should(CollectionCondition condition, Duration timeout);

    ElementsCollection shouldBe(CollectionCondition condition);

    ElementsCollection shouldBe(CollectionCondition condition, Duration timeout);

    ElementsCollection filter(Condition condition);

    ElementsCollection filterBy(Condition condition);

    SelenideElement find(Condition condition);

    SelenideElement findBy(Condition condition);
}