package com.bft.gui;

import com.bft.security.masking.SecureLogger;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import com.bft.enums.UIType;
import java.time.Duration;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.$x;


public class LoginPage {

    private final SecureLogger logger = SecureLogger.getLogger(getClass());

    public LoginPage open(UIType uiType) {

        Selenide.open(uiType.value);
        return this;
    }

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
            com.bft.test.helpers.SmartWaits.waitForPageLoad(com.bft.constants.TimeoutConstants.EPGU_AUTH_WAIT);
            
            // Дополнительная проверка: ожидаем либо исчезновения формы авторизации,
            // либо появления страницы выбора карточки, либо успешной авторизации
            com.codeborne.selenide.Selenide.Wait().until(webDriver -> {
                // Проверяем, что форма авторизации исчезла
                boolean loginFormGone = webDriver.findElements(org.openqa.selenium.By.id("login")).isEmpty() ||
                    !webDriver.findElement(org.openqa.selenium.By.id("login")).isDisplayed();
                
                // Проверяем появление страницы выбора карточки
                boolean cardSelectionPageAppeared = !webDriver.findElements(
                    org.openqa.selenium.By.xpath("//*[@class = 'selectUserCardName']")).isEmpty();
                
                // Проверяем успешную авторизацию (если карточка одна, выбор может быть пропущен)
                boolean userLoggedIn = !webDriver.findElements(
                    org.openqa.selenium.By.xpath("//div[contains(@class, 'user-name')]")).isEmpty();
                
                return loginFormGone && (cardSelectionPageAppeared || userLoggedIn);
            });
            
            logger.debug("Авторизация через ЕПГУ завершена");
        } catch (Exception e) {
            logger.warn("Ожидание завершения авторизации прервано: {}", e.getMessage());
            // Продолжаем выполнение - возможно, авторизация уже завершена
            // Метод selectUserCardEPGU сам проверит состояние страницы
        }
        
        return this;
    }

    public LoginPage selectUserCardEPGU(String usercard) {
        // Используем улучшенную логику из com.bft.ui.pages.LoginPage
        // для надежности и обработки проблем с кодировкой
        logger.info("Выбор карточки пользователя: {}", usercard);
        
        // Ожидаем завершения авторизации и появления страницы выбора карточки
        com.bft.test.helpers.SmartWaits.waitForPageLoad(com.bft.constants.TimeoutConstants.EPGU_AUTH_WAIT);
        
        // Ожидаем появления страницы выбора карточки "Войти как"
        // Это происходит ПОСЛЕ ввода логина и пароля, но ДО выбора организации
        logger.debug("Ожидаем появления страницы выбора карточки...");
        try {
            $x("//*[@class = 'selectUserCardName']").shouldBe(Condition.exist, Duration.ofSeconds(60));
            logger.debug("Страница выбора карточки загружена");
        } catch (Exception e) {
            // Проверяем, может быть пользователь уже авторизован (если карточка одна)
            try {
                $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(5));
                logger.info("Пользователь уже авторизован, выбор карточки не требуется");
                return this;
            } catch (Exception e2) {
                logger.error("Страница выбора карточки не появилась и пользователь не авторизован");
                throw new RuntimeException(
                    com.bft.test.helpers.AssertionHelper.formatPageStateError(
                        "Страница выбора карточки",
                        "должна быть загружена после авторизации",
                        "не загружена. Возможно, авторизация не прошла успешно"
                    ), e
                );
            }
        }
        
        // Ищем карточку с несколькими стратегиями
        String normalizedCard = usercard.trim();
        // Извлекаем номер организации (последние цифры после пробела или дефиса)
        String orgNumber = normalizedCard.replaceAll(".*?(-?\\d+)$", "$1");
        logger.debug("Ищем карточку: '{}', номер организации: '{}'", normalizedCard, orgNumber);
        
        boolean cardFound = false;
        
        // Стратегия 1: Поиск по номеру организации (самый надежный способ)
        // Используем contains() для поиска по номеру, так как он уникален
        try {
            logger.debug("Стратегия 1: Поиск по номеру организации '{}'", orgNumber);
            $x("//*[@class = 'selectUserCardName' and contains(text(), '" + orgNumber + "')]")
                .shouldBe(Condition.visible, Duration.ofSeconds(10))
                .click();
            cardFound = true;
            logger.info("Карточка найдена по номеру организации: {}", orgNumber);
        } catch (Exception e) {
            logger.debug("Поиск по номеру организации не удался: {}", e.getMessage());
        }
        
        // Стратегия 2: Перебор всех карточек и поиск по номеру или части текста
        if (!cardFound) {
            try {
                logger.debug("Стратегия 2: Перебор всех карточек");
                com.codeborne.selenide.ElementsCollection cards = com.codeborne.selenide.Selenide.$$x("//*[@class = 'selectUserCardName']");
                logger.debug("Найдено карточек на странице: {}", cards.size());
                
                for (com.codeborne.selenide.SelenideElement card : cards) {
                    String cardText = card.getText().trim();
                    logger.debug("Проверяем карточку: '{}'", cardText);
                    
                    // Проверяем совпадение по номеру организации (самый надежный способ)
                    if (cardText.contains(orgNumber)) {
                        logger.info("Найдена карточка по номеру: '{}'", cardText);
                        card.shouldBe(Condition.visible).click();
                        cardFound = true;
                        break;
                    }
                    
                    // Дополнительная проверка: частичное совпадение текста (на случай проблем с кодировкой)
                    if (normalizedCard.toLowerCase().contains(cardText.toLowerCase()) ||
                        cardText.toLowerCase().contains(normalizedCard.toLowerCase())) {
                        logger.info("Найдена карточка по частичному совпадению: '{}'", cardText);
                        card.shouldBe(Condition.visible).click();
                        cardFound = true;
                        break;
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
                $x("//*[@class = 'selectUserCardName' and normalize-space(text()) = '" + escapedCard + "']")
                    .shouldBe(Condition.visible, Duration.ofSeconds(10))
                    .click();
                cardFound = true;
                logger.info("Карточка найдена по точному совпадению");
            } catch (Exception e) {
                logger.debug("Точное совпадение не найдено: {}", e.getMessage());
            }
        }
        
        if (!cardFound) {
            throw new RuntimeException("Карточка пользователя '" + normalizedCard + "' не найдена");
        }
        
        // Ожидаем завершения выбора карточки
        // После клика по карточке страница может перезагрузиться, поэтому:
        // 1. Сначала ждем загрузки страницы
        // 2. Затем ждем появления элемента user-name
        logger.debug("Карточка выбрана, ожидаем завершения авторизации...");
        
        try {
            // Ожидаем загрузки страницы после выбора карточки
            com.bft.test.helpers.SmartWaits.waitForPageLoad(com.bft.constants.TimeoutConstants.PAGE_LOAD_WAIT);
            
            // Ожидаем появления элемента user-name с несколькими стратегиями
            boolean userElementFound = false;
            
            // Стратегия 1: Стандартный селектор user-name (Selenide при таймауте бросает AssertionError — ловим Throwable)
            try {
                $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(30));
                userElementFound = true;
                logger.debug("Элемент user-name найден по стандартному селектору");
            } catch (Throwable e) {
                logger.debug("Стандартный селектор user-name не сработал: {}", e.getMessage());
            }
            
            // Стратегия 2: Альтернативные селекторы
            if (!userElementFound) {
                try {
                    $x("//div[contains(@class, 'user')]").shouldBe(Condition.visible, Duration.ofSeconds(10));
                    userElementFound = true;
                    logger.debug("Элемент найден по альтернативному селектору");
                } catch (Throwable e) {
                    logger.debug("Альтернативный селектор не сработал: {}", e.getMessage());
                }
            }
            
            // Стратегия 3: Проверка по URL - если мы на главной странице, значит авторизованы
            if (!userElementFound) {
                try {
                    String currentUrl = com.codeborne.selenide.WebDriverRunner.getWebDriver().getCurrentUrl();
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
                // Получаем текущий URL и HTML для отладки
                String currentUrl = com.codeborne.selenide.WebDriverRunner.getWebDriver().getCurrentUrl();
                String pageSource = com.codeborne.selenide.WebDriverRunner.getWebDriver().getPageSource();
                logger.error("Элемент user-name не найден. URL: {}", currentUrl);
                logger.error("Размер страницы: {} символов", pageSource.length());
                
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
        
        return this;
    }

    private void waitForLoading() {
        $x("//div[contains(@class, 'user-name')]").shouldBe(Condition.visible, Duration.ofSeconds(60));
    }

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
