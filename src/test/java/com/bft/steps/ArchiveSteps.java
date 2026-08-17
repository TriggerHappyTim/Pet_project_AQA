package com.bft.steps;

import com.bft.ui.pages.MainPage;
import io.qameta.allure.Step;
import org.springframework.stereotype.Component;

@Component
public class ArchiveSteps {

    @Step("Переход в реестр запросов архивной организации")
    public void openArchiveRequestsRegistry() {
        new MainPage()
                .openTab("ЛК Архивной Организации")
                .openTab("Реестр запросов");
    }

    @Step("Поиск исполнителя по имени: {performerName}")
    public void searchPerformer(String performerName) {
        new MainPage()
                .clickInputLabel("Исполнитель", performerName)
                .clickButton("Применить");
    }
}