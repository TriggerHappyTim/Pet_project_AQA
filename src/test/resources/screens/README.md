# Эталонные скриншоты для визуальной регрессии

Эталонные изображения для `VisualComparator` хранятся здесь: `screens/<name>.png`.

## Как создать эталон

```bash
# Режим update: фактические скриншоты сохраняются как эталоны
mvn test -Dtest=MyVisualTest -Devs.visual.update=true
```

## Как использовать в тесте

```java
public class MyVisualTest extends UITestBase {
    private final VisualComparator visual = new VisualComparator();

    @Test
    @Tag("web")
    public void loginPageLooksCorrect() {
        // ... открыть страницу
        visual.assertMatchesPage(assertions, "login-page");
    }
}
```

## Настройки

| Свойство | Переменная окружения | По умолчанию | Описание |
|----------|---------------------|--------------|----------|
| `evs.visual.threshold` | `EVS_VISUAL_THRESHOLD` | `1.0` | Допустимый % отличающихся пикселей |
| `evs.visual.update` | `EVS_VISUAL_UPDATE` | `false` | Перезаписывать эталоны актуальными скриншотами |

Diff-изображения при расхождении сохраняются в `build/reports/visual-diff/`
и прикрепляются к отчёту Allure (reference / actual / diff).
