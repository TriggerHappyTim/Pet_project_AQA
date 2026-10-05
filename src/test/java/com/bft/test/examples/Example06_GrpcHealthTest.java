package com.bft.test.examples;

import com.bft.service.grpc.GrpcChannelFactory;
import com.bft.service.grpc.GrpcConfig;
import com.bft.service.grpc.GrpcHealthCheck;
import com.bft.test.base.BaseTest;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Duration;

/**
 * ============================== УЧЕБНЫЙ ПРИМЕР №6 ==============================
 *
 * gRPC: проверка доступности бэкенд-сервисов ЕВС.
 *
 * ЗАЧЕМ ЭТО НУЖНО:
 *   Перед долгим прогоном полезно проверить, что все внутренние сервисы живы.
 *   Если сервис недоступен — тесты упадут странным образом, потратив час.
 *   Лучше узнать об этом за 5 секунд на старте.
 *
 * КАКИЕ СЕРВИСЫ ИЗВЕСТНЫ ФРЕЙМВОРКУ (порты = port-forward'ам из .gitlab-ci.yml):
 *   METADATA         localhost:12099
 *   SEQUENCE         localhost:9090
 *   NOBLE_REGISTRY   localhost:12089
 *   TRANSACT_MANAGER localhost:13000
 *
 * ПЕРЕОПРЕДЕЛЕНИЕ ХОСТА/ПОРТА:
 *   -Dgrpc.metadata.host=172.18.32.153 -Dgrpc.metadata.port=12099
 *
 * ПРО СОГЛАШЕНИЕ ИМЁН:
 *   grpc.<имя>.host / grpc.<имя>.port / grpc.<имя>.tls
 *   Переменные окружения: GRPC_<ИМЯ>_HOST и т.д.
 * ==============================================================================
 */
@Disabled("Обучающий пример №6 — не запускать в CI")
@Tag("example")
@Tag("grpc")
public class Example06_GrpcHealthTest extends BaseTest {

    @Test
    public void backendServicesAreReachable() {
        GrpcHealthCheck health = new GrpcHealthCheck();

        // checkAll проверит КАЖДЫЙ эндпоинт и соберёт ВСЕ проблемы в одну ошибку:
        // AssertionError со списком недоступных сервисов. Так вы увидите всю картину
        // разом, а не будете чинить по одному падению за прогон.
        health.checkAll(Duration.ofSeconds(5),
                GrpcConfig.Endpoint.METADATA,
                GrpcConfig.Endpoint.SEQUENCE);
    }

    /**
     * Произвольный сервис, которого нет в enum Endpoint.
     */
    @Test
    public void customServiceEndpoint() {
        // Описываем адрес: хост берётся из -Dgrpc.my-service.host, порт задан дефолтом.
        GrpcConfig.NamedEndpoint custom =
                GrpcConfig.getInstance().namedEndpoint("my-service", 15000);

        ManagedChannel channel = GrpcChannelFactory.channel(
                "my-service", custom.host, custom.port, custom.tls);

        boolean ready = GrpcChannelFactory.awaitReady(channel, Duration.ofSeconds(5));
        assertions.assertTrue(ready,
                "Сервис my-service (" + custom.host + ":" + custom.port + ") должен быть доступен");
    }

    /**
     * После всех тестов класса закрываем gRPC-каналы.
     * @AfterAll-метод обязан быть static (требование JUnit 5).
     */
    @AfterAll
    static void tearDownChannels() {
        GrpcChannelFactory.shutdownAll();
    }
}
