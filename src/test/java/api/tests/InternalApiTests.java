package api.tests;

import api.helpers.DataGenerator;
import api.models.ResultResponse;
import api.specs.InternalSpecs;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.assertj.core.api.Assertions.assertThat;

@Epic("Тестирование внутреннего API")
@Feature("Операции пользователя: LOGIN, ACTION, LOGOUT")
public class InternalApiTests extends TestBase {

    @Test
    @Tag("positive")
    @DisplayName("Успешный LOGIN: Внешний сервис вернул 200")
    void successLoginTest() {
        String token = DataGenerator.generateToken();

        step("Настроить внешний мок /auth на 200 OK", () ->
                stubExternal("/auth", 200));

        ResultResponse response = step("Отправить запрос LOGIN", () ->
                given()
                        .spec(InternalSpecs.requestSpec)
                        .formParam("token", token)
                        .formParam("action", "LOGIN")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(200)
                        .body(matchesJsonSchemaInClasspath("schema.json"))
                        .extract()
                        .as(ResultResponse.class));

        step("Проверить, что результат OK", () ->
                assertThat(response.getResult()).isEqualTo("OK"));
    }

    @Test
    @Tag("positive")
    @DisplayName("Успешный ACTION после LOGIN")
    void successActionTest() {
        String token = DataGenerator.generateToken();

        step("Подготовить моки для /auth и /doAction", () -> {
            stubExternal("/auth", 200);
            stubExternal("/doAction", 200);
        });

        step("Выполнить LOGIN", () ->
                given()
                        .spec(InternalSpecs.requestSpec)
                        .formParam("token", token)
                        .formParam("action", "LOGIN")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(200));

        ResultResponse response = step("Выполнить ACTION", () ->
                given()
                        .spec(InternalSpecs.requestSpec)
                        .formParam("token", token)
                        .formParam("action", "ACTION")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(ResultResponse.class));

        step("Проверить успешное выполнение действия", () ->
                assertThat(response.getResult()).isEqualTo("OK"));
    }

    @Test
    @Tag("positive")
    @DisplayName("Успешный LOGOUT")
    void successLogoutTest() {
        String token = DataGenerator.generateToken();

        step("Подготовить мок и выполнить предварительный LOGIN", () -> {
            stubExternal("/auth", 200);
            given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .then()
                    .statusCode(200);
        });

        ResultResponse response = step("Запрос LOGOUT", () ->
                given()
                        .spec(InternalSpecs.requestSpec)
                        .formParam("token", token)
                        .formParam("action", "LOGOUT")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(200)
                        .extract()
                        .as(ResultResponse.class));

        step("Проверка: результат OK", () ->
                assertThat(response.getResult()).isEqualTo("OK"));
    }

    @Test
    @Tag("negative")
    @DisplayName("Ошибка: Невалидный формат токена (буква Z)")
    void invalidTokenFormatTest() {
        String invalidToken = "1234567890ABCDEF1234567890ABCDEZ";

        ResultResponse response = step("Отправить запрос с невалидным токеном", () ->
                given()
                        .spec(InternalSpecs.requestSpec)
                        .formParam("token", invalidToken)
                        .formParam("action", "LOGIN")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(400)
                        .extract()
                        .as(ResultResponse.class));

        step("Проверить наличие сообщения об ошибке формата", () -> {
            assertThat(response.getResult()).isEqualTo("ERROR");
            assertThat(response.getMessage()).contains("должно соответствовать");
        });
    }

    @Test
    @Tag("negative")
    @DisplayName("Ошибка: Неверный X-Api-Key")
    void wrongApiKeyTest() {
        String token = DataGenerator.generateToken();

        step("Отправить запрос с фейковым ключом", () ->
                given()
                        .baseUri("http://localhost:8080")
                        .header("X-Api-Key", "WRONG_TOKEN_123")
                        .queryParam("token", token)
                        .queryParam("action", "LOGIN")
                        .when()
                        .post("/endpoint")
                        .then()
                        .statusCode(401));
    }
}
