package com.bft.ui.pages;

import com.bft.enums.TimeoutConstants;
import com.bft.security.masking.SecureLogger;
import com.bft.pw.Condition;
import com.bft.pw.Selenide;
import com.bft.enums.UIType;
import java.time.Duration;

import static com.bft.pw.Selenide.$;
import static com.bft.pw.Selenide.$x;

/**
 * Page Object для страницы авторизации в системе EVS
 * 
 * Предоставляет методы для:
 * - Открытия страницы авторизации различных окружений
 * - Стандартной авторизации (логин/пароль)
 * - Авторизации через ЕПГУ
 * - Выхода из системы
 * 
 * <p>Все методы возвращают this для поддержки fluent API (цепочки вызовов).
 * 
 * <p>Пример использования:
 * <pre>{@code
 * new LoginPage()
 *     .open(UIType.EVS_UAT_LKS)
 *     .authorize("user@test.com", "password");
 * }</pre>
 * 
 * @author QA Automation Team
 * @version 2.0
 * @see UIType для доступных типов окружений
 * @see MainPage для работы после авторизации
 */
public class LoginPage {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    /**
     * Открывает страницу авторизации для указанного окружения
     * 
     * Навигирует браузер на URL страницы авторизации, соответствующий
     * выбранному типу UI окружения.
     * 
     * @param uiType тип UI окружения из enum {@link UIType}
     *               (например, EVS_UAT_LKS, EVS_TEST_LKS, EVS_PROD_LKS)
     * @return текущий экземпляр LoginPage для цепочки вызовов (fluent API)
     * @see UIType для полного списка доступных окружений
     */
    public LoginPage open(UIType uiType) {

        Selenide.open(uiType.value);
        return this;
    }

    /**
     * Авторизует пользователя в системе EVS через стандартную форму
     * 
     * Выполняет вход в систему через форму авторизации:
     * 1. Вводит логин в первое поле ввода
     * 2. Вводит пароль во второе поле ввода
     * 3. Нажимает кнопку "Войти"
     * 4. Ожидает появления элемента с именем пользователя (подтверждение входа)
     * 
     * <p>Метод использует {@link SecureLogger} - пароль не логируется в открытом виде.
     * 
     * @param login логин пользователя (email или username)
     * @param password пароль пользователя
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.bft.pw.ex.ElementNotFoundException 
     *         если элементы формы авторизации не найдены
     * @throws com.bft.pw.ex.ElementShould 
     *         если элемент подтверждения входа не появился в течение 60 секунд
     */
    public LoginPage authorize(String login, String password) {
        logger.info("Выполняем авторизацию пользователя: {}", login);

        /*$x(String.format("//*[@id= '%s')", login)).setValue(login);*/
        /*$("input.loginInput#login").setValue(login);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput']").setValue(login);
        /*$("input.loginInput#pass").setValue(password);*/
        $x("//div[@class = 'field loginControl']//input[@class = 'loginInput secondInput']").setValue(password);
        /*$("button.loginButton").click();*/
        $x("//button[contains(@class,'button is-')]").click();
        waitForLoading();
        return this;
    }

    /**
     * Авторизует пользователя через ЕПГУ (Единый портал государственных услуг)
     * 
     * Выполняет авторизацию через внешний провайдер ESIA (ЕПГУ):
     * 1. Кликает по ссылке "Войти через ЕПГУ"
     * 2. Ожидает загрузки формы ЕПГУ (до 70 секунд)
     * 3. Вводит логин и пароль
     * 4. Нажимает кнопку "Войти"
     * 
     * <p>После успешной авторизации необходимо выбрать карточку пользователя
     * через метод {@link #selectUserCardEPGU(String)}.
     * 
     * @param login логин пользователя на портале ЕПГУ
     * @param password пароль пользователя на портале ЕПГУ
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.bft.pw.ex.ElementNotFoundException 
     *         если ссылка ЕПГУ или элементы формы не найдены
     * @see #selectUserCardEPGU(String) для выбора карточки после авторизации
     */
    public LoginPage authorizeEPGU(String login, String password) {
        logger.info("Выполняем авторизацию через ЕПГУ для пользователя: {}", login);

        $x("//a[starts-with(@href,'/esia')]").click();
        $("input#login").shouldBe(Condition.enabled,Duration.ofSeconds(70));
        $("input#login").setValue(login);
        $("input#password").setValue(password);
        $x("//button[contains(text(), 'Войти')]").shouldBe(Condition.enabled,Duration.ofSeconds(70)).click();
        
        // Ожидаем завершения авторизации - форма ЕПГУ должна исчезнуть
        // или появиться страница выбора карточки, или пользователь уже авторизован
        try {
            // Используем умное ожидание через SmartWaits
            com.bft.test.helpers.SmartWaits.waitForPageLoad(TimeoutConstants.EPGU_AUTH_WAIT);
            
            // Дополнительная проверка: ожидаем либо исчезновения формы авторизации,
            // либо появления страницы выбора карточки, либо успешной авторизации
            com.bft.pw.Selenide.Wait().until(webDriver -> {
                // Проверяем, что форма авторизации исчезла
                boolean loginFormGone = webDriver.findElements(com.bft.pw.By.id("login")).isEmpty() ||
                    !webDriver.findElement(com.bft.pw.By.id("login")).isDisplayed();
                
                // Проверяем появление страницы выбора карточки
                boolean cardSelectionPageAppeared = !webDriver.findElements(
                    com.bft.pw.By.xpath("//*[@class = 'selectUserCardName']")).isEmpty();
                
                // Проверяем успешную авторизацию (если карточка одна, выбор может быть пропущен)
                boolean userLoggedIn = !webDriver.findElements(
                    com.bft.pw.By.xpath("//div[contains(@class, 'user-name')]")).isEmpty();
                
                boolean authCompleted = loginFormGone && (cardSelectionPageAppeared || userLoggedIn);
                
                if (authCompleted) {
                    logger.debug("Авторизация завершена. Карточки: {}, Авторизован: {}", 
                        cardSelectionPageAppeared, userLoggedIn);
                }
                
                return authCompleted;
            });
            
            logger.debug("Авторизация через ЕПГУ завершена");
        } catch (Exception e) {
            logger.warn("Ожидание завершения авторизации прервано: {}", e.getMessage());
            // Продолжаем выполнение - возможно, авторизация уже завершена
            // Метод selectUserCardEPGU сам проверит состояние страницы
        }
        
        return this;
    }

    /**
     * Выбирает карточку пользователя после авторизации через ЕПГУ
     * 
     * После авторизации через ЕПГУ система может предложить выбрать
     * одну из нескольких карточек пользователя (организация, ИП, физлицо).
     * Метод ожидает появления карточки и кликает по ней.
     * 
     * <p>Метод выполняет следующие проверки:
     * 1. Ожидает завершения авторизации (исчезновение формы ЕПГУ или появление страницы выбора)
     * 2. Ожидает появления страницы выбора карточки
     * 3. Ожидает появления карточки с указанным текстом (с учетом возможных проблем кодировки)
     * 4. Кликает по карточке
     * 5. Ожидает завершения выбора (появление имени пользователя)
     * 
     * @param usercard текст на карточке пользователя для выбора
     *                 (например, "ОРГАНИЗАЦИЯ -1546025669")
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.bft.pw.ex.ElementNotFoundException 
     *         если карточка с указанным текстом не найдена
     * @throws com.bft.pw.ex.ElementShould 
     *         если карточка не появилась в течение 60 секунд
     */
    public LoginPage selectUserCardEPGU(String usercard) {
        logger.info("Выбор карточки пользователя: {}", usercard);
        
        // Шаг 1: Ожидаем завершения авторизации и появления страницы выбора карточки
        // Используем умное ожидание через SmartWaits
        com.bft.test.helpers.SmartWaits.waitForPageLoad(TimeoutConstants.EPGU_AUTH_WAIT);
        
        // Шаг 2: Ожидаем появления страницы выбора карточки "Войти как"
        // Это происходит ПОСЛЕ ввода логина и пароля, но ДО выбора организации
        logger.debug("Ожидаем появления страницы выбора карточки...");
        try {
            $x("//*[@class = 'selectUserCardName']").shouldBe(Condition.exist, Duration.ofSeconds(60));
            logger.debug("Страница выбора карточки загружена");
        } catch (Exception e) {
            // Проверяем, может быть пользователь уже авторизован (если карточка одна, выбор может быть пропущен)
            try {
                $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(5));
                logger.info("Пользователь уже авторизован, выбор карточки не требуется");
                return this;
            } catch (Exception e2) {
                logger.error("Страница выбора карточки не появилась и пользователь не авторизован. Текущий URL: {}", 
                    com.bft.pw.WebDriverRunner.getWebDriver().getCurrentUrl());
                throw new RuntimeException(
                    com.bft.test.helpers.AssertionHelper.formatPageStateError(
                        "Страница выбора карточки",
                        "должна быть загружена после авторизации",
                        "не загружена. Возможно, авторизация не прошла успешно"
                    ), e
                );
            }
        }
        
        // Шаг 4: Ищем карточку с указанным текстом
        // Используем несколько стратегий для надежности (проблемы с кодировкой)
        String normalizedCard = usercard.trim();
        // Извлекаем номер организации из строки (последние цифры), если он есть.
        // Если номера нет — оставляем null и полагаемся на текстовое сравнение.
        java.util.regex.Matcher numMatcher = java.util.regex.Pattern.compile("(-?\\d+)\\s*$").matcher(normalizedCard);
        String orgNumber = numMatcher.find() ? numMatcher.group(1) : null;
        logger.debug("Ищем карточку: '{}', номер организации: '{}'", normalizedCard, orgNumber);
        
        boolean cardFound = false;
        
        // Стратегия 1: Поиск по номеру организации (самый надежный способ)
        // Используем contains() по normalize-space(.) — работает и для текста во вложенных элементах
        if (orgNumber != null) {
            try {
                logger.debug("Стратегия 1: Поиск по номеру организации '{}'", orgNumber);
                $x("//*[@class = 'selectUserCardName' and contains(normalize-space(.), '" + orgNumber + "')]")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .click();
                logger.info("Карточка найдена по номеру организации: {}", orgNumber);
                cardFound = true;
            } catch (Exception e) {
                logger.debug("Поиск по номеру организации не удался: {}", e.getMessage());
            }
        }
        
        // Стратегия 2: Перебор всех карточек и поиск по номеру или части текста
        if (!cardFound) {
            try {
                logger.debug("Стратегия 2: Перебор всех карточек");
                com.bft.pw.ElementsCollection cards = com.bft.pw.Selenide.$$x("//*[@class = 'selectUserCardName']");
                logger.debug("Найдено карточек на странице: {}", cards.size());
                
                String normTarget = normalizeForMatch(normalizedCard);
                
                // Проход 1: Точное совпадение после нормализации (без регистра и пунктуации).
                // Предпочтительный вариант — не позволяет ошибочно выбрать чужую карточку.
                for (com.bft.pw.SelenideElement card : cards) {
                    String cardText = card.getText().trim();
                    if (orgNumber != null && cardText.contains(orgNumber)) {
                        logger.info("Найдена карточка по номеру: '{}'", cardText);
                        card.shouldBe(Condition.visible).click();
                        cardFound = true;
                        break;
                    }
                    String normCard = normalizeForMatch(cardText);
                    if (!normTarget.isEmpty() && normTarget.equals(normCard)) {
                        logger.info("Найдена карточка по точному совпадению: '{}'", cardText);
                        card.shouldBe(Condition.visible).click();
                        cardFound = true;
                        break;
                    }
                }
                
                // Проход 2: Частичное совпадение (карточка содержит искомое название).
                // Только если точного совпадения не нашлось.
                if (!cardFound) {
                    for (com.bft.pw.SelenideElement card : cards) {
                        String cardText = card.getText().trim();
                        String normCard = normalizeForMatch(cardText);
                        if (!normTarget.isEmpty() && !normCard.isEmpty() && normCard.contains(normTarget)) {
                            logger.info("Найдена карточка по частичному совпадению: '{}'", cardText);
                            card.shouldBe(Condition.visible).click();
                            cardFound = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Ошибка при переборе карточек: {}", e.getMessage());
            }
        }
        
        // Стратегия 3: Точное совпадение (последняя попытка, может не сработать из-за проблем с кодировкой)
        if (!cardFound) {
            try {
                logger.debug("Стратегия 3: Точное совпадение текста");
                // Используем concat() для правильной обработки UTF-8 в XPath
                String escapedCard = normalizedCard.replace("'", "''"); // Экранируем одинарные кавычки
                $x("//*[@class = 'selectUserCardName' and normalize-space(.) = '" + escapedCard + "']")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .click();
                logger.info("Карточка найдена по точному совпадению");
                cardFound = true;
            } catch (Exception e) {
                logger.debug("Точное совпадение не найдено: {}", e.getMessage());
            }
        }
        
        if (!cardFound) {
            // Получаем список всех доступных карточек для отладки
            com.bft.pw.ElementsCollection allCards = com.bft.pw.Selenide.$$x("//*[@class = 'selectUserCardName']");
            StringBuilder availableCards = new StringBuilder();
            for (com.bft.pw.SelenideElement card : allCards) {
                availableCards.append("'").append(card.getText()).append("', ");
            }
            
            String availableCardsStr = availableCards.length() > 0 
                ? availableCards.substring(0, availableCards.length() - 2) 
                : "не найдены";
            
            throw new RuntimeException(
                com.bft.test.helpers.AssertionHelper.formatElementError(
                    "Карточка пользователя",
                    "должна быть найдена",
                    String.format("Искали: '%s'. Доступные карточки: [%s]", normalizedCard, availableCardsStr),
                    com.bft.pw.WebDriverRunner.getWebDriver().getCurrentUrl()
                )
            );
        }
        
        // Шаг 5: Ожидаем завершения выбора (появление имени пользователя)
        // После клика по карточке страница может перезагрузиться
        logger.debug("Карточка выбрана, ожидаем завершения авторизации...");
        
        try {
            // Ожидаем загрузки страницы после выбора карточки
            com.bft.test.helpers.SmartWaits.waitForPageLoad(TimeoutConstants.PAGE_LOAD_WAIT);
            
            // Ожидаем появления элемента user-name с несколькими стратегиями
            boolean userElementFound = false;
            
            // Стратегия 1: Стандартный селектор user-name
            try {
                $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(30));
                userElementFound = true;
                logger.debug("Элемент user-name найден по стандартному селектору");
            } catch (Exception e) {
                logger.debug("Стандартный селектор user-name не сработал: {}", e.getMessage());
            }
            
            // Стратегия 2: Альтернативные селекторы
            if (!userElementFound) {
                try {
                    // Пробуем другие варианты селекторов
                    $x("//div[contains(@class, 'user')]").shouldBe(Condition.visible, Duration.ofSeconds(10));
                    userElementFound = true;
                    logger.debug("Элемент найден по альтернативному селектору");
                } catch (Exception e) {
                    logger.debug("Альтернативный селектор не сработал: {}", e.getMessage());
                }
            }
            
            // Стратегия 3: Проверка по URL - если мы на главной странице, значит авторизованы
            if (!userElementFound) {
                try {
                    String currentUrl = com.bft.pw.WebDriverRunner.getWebDriver().getCurrentUrl();
                    logger.debug("Текущий URL: {}", currentUrl);
                    
                    // Если URL содержит признаки авторизованной страницы (не страница логина)
                    if (!currentUrl.contains("/login") && !currentUrl.contains("/esia") && 
                        !currentUrl.contains("auth") && currentUrl.contains("evs")) {
                        logger.info("Авторизация завершена (определено по URL)");
                        userElementFound = true;
                    }
                } catch (Exception e) {
                    logger.warn("Не удалось проверить URL: {}", e.getMessage());
                }
            }
            
            if (!userElementFound) {
                // Получаем текущий URL для отладки
                String currentUrl = com.bft.pw.WebDriverRunner.getWebDriver().getCurrentUrl();
                logger.error("Элемент user-name не найден. URL: {}", currentUrl);
                
                throw new RuntimeException(
                    com.bft.test.helpers.AssertionHelper.formatPageStateError(
                        "Элемент авторизации",
                        "должен появиться после выбора карточки",
                        "не найден. URL: " + currentUrl
                    )
                );
            }
            
            logger.info("Авторизация успешно завершена после выбора карточки");
        } catch (RuntimeException e) {
            // Пробрасываем RuntimeException дальше
            throw e;
        } catch (Exception e) {
            logger.error("Ошибка при ожидании завершения авторизации: {}", e.getMessage());
            throw new RuntimeException("Не удалось дождаться завершения авторизации после выбора карточки", e);
        }
        
        logger.info("Карточка пользователя успешно выбрана: {}", usercard);
        return this;
    }

    /**
     * Нормализует строку для сравнения при поиске карточки:
     * нижний регистр, удаление всех символов кроме букв и цифр
     * и приведение визуально похожих букв кириллицы/латиницы к общему виду.
     *
     * <p>Устойчиво к кавычкам, скобкам, дефисам, неразрывным пробелам и
     * латинским буквам-двойникам (например «С» вместо «С», «O» вместо «О»).
     *
     * @param value исходная строка (название организации)
     * @return нормализованная строка для case/punctuation-insensitive сравнения
     */
    private static String normalizeForMatch(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char base = mapToBaseLetter(value.charAt(i));
            if (base != 0) {
                sb.append(base);
            }
        }
        return sb.toString();
    }

    /**
     * Приводит букву к «базовому» латинскому виду, объединяя кириллицу и
     * визуально неотличимые латинские буквы (гомоглифы). Не-буквы возвращают 0.
     */
    private static char mapToBaseLetter(char c) {
        switch (c) {
            case 'А': case 'а': case 'A': return 'a';
            case 'Б': case 'б': case 'В': case 'в': case 'Ь': case 'ъ': return 'b';
            case 'B': return 'b';
            case 'Г': case 'г': return 'g';
            case 'Д': case 'д': case 'D': return 'd';
            case 'Е': case 'е': case 'Ё': case 'ё': case 'Э': case 'э': return 'e';
            case 'E': return 'e';
            case 'Ж': case 'ж': return 'z';
            case 'З': case 'з': case 'Z': return 'z';
            case 'И': case 'и': case 'Й': case 'й': case 'Ы': case 'ы': return 'i';
            case 'I': return 'i';
            case 'К': case 'к': return 'k';
            case 'K': return 'k';
            case 'Л': case 'л': return 'l';
            case 'L': return 'l';
            case 'М': case 'м': return 'm';
            case 'M': return 'm';
            case 'Н': case 'н': case 'Ч': case 'ч': return 'h';
            case 'H': return 'h';
            case 'О': case 'о': case 'O': return 'o';
            case 'П': case 'п': case 'Р': case 'р': return 'p';
            case 'P': return 'p';
            case 'С': case 'с': case 'Ц': case 'ц': return 'c';
            case 'C': return 'c';
            case 'Т': case 'т': return 't';
            case 'T': return 't';
            case 'У': case 'у': case 'Ю': case 'ю': return 'y';
            case 'Y': return 'y';
            case 'Ф': case 'ф': return 'f';
            case 'F': return 'f';
            case 'Х': case 'х': case 'X': return 'x';
            case 'Ш': case 'ш': case 'Щ': case 'щ': return 's';
            case 'S': return 's';
            case 'Я': case 'я': return 'a';
            default:
                return Character.isLetterOrDigit(c) ? Character.toLowerCase(c) : 0;
        }
    }

    /**
     * Ожидает завершения загрузки страницы после авторизации
     * 
     * Ожидает появления элемента с именем пользователя в шапке сайта,
     * что является индикатором успешной авторизации.
     */
    private void waitForLoading() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }

    /**
     * Выполняет выход из системы
     * 
     * Выполняет logout пользователя:
     * 1. Закрывает всплывающее сообщение об ошибке если оно есть
     * 2. Кликает по элементу с именем пользователя
     * 3. Кликает по кнопке "Выйти"
     * 4. Ожидает появления сообщения "Войдите в систему"
     * 
     * @return текущий экземпляр LoginPage для цепочки вызовов
     * @throws com.bft.pw.ex.ElementShould 
     *         если сообщение "Войдите в систему" не появилось в течение 60 секунд
     */
    public LoginPage logOut() {
        if (
                $x("//div[@class= 'ps-alert-top']").isDisplayed())
        {$x("//i[@class= 'ps-icon-x ps-alert-close']").click();}

        // если сообщение об ошибке запроса сервера,закрыть ее
        $x("//div[contains(@class, 'user-name')]").click();
        $x("//button[contains(@class, 'logout')]").click();
        $x("//div[contains(text(), 'Войдите в систему')]").shouldBe(Condition.visible, Duration.ofSeconds(60));

        return this;
    }
}