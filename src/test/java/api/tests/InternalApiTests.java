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
    @DisplayName("Успешная авторизация (LOGIN): Внешний сервис вернул 200")
    void successLoginTest() {
        String token = DataGenerator.generateToken();
        stubExternal("/auth", 200);

        step("Выполнить LOGIN для токена: " + token, () -> {
            ResultResponse response = given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .statusCode(200)
                    .body(matchesJsonSchemaInClasspath("schema.json"))
                    .extract().as(ResultResponse.class);

            step("Проверить, что результат операции: OK", () ->
                    assertThat(response.getResult()).isEqualTo("OK"));
        });
    }

    @Test
    @Tag("positive")
    @DisplayName("Успешное выполнение ACTION после LOGIN")
    void successActionTest() {
        String token = DataGenerator.generateToken();
        stubExternal("/auth", 200);
        stubExternal("/doAction", 200);

        step("1. Выполнить LOGIN для токена: " + token, () ->
                given().spec(InternalSpecs.requestSpec).formParam("token", token).formParam("action", "LOGIN").post("/endpoint")
                        .then().statusCode(200));

        step("2. Выполнить ACTION для токена: " + token, () -> {
            ResultResponse response = given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "ACTION")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .statusCode(200)
                    .extract().as(ResultResponse.class);

            step("Проверить успешное выполнение действия (result: OK)", () ->
                    assertThat(response.getResult()).isEqualTo("OK"));
        });
    }

    @Test
    @Tag("negative")
    @DisplayName("Отказ в ACTION: Токен не прошел предварительный LOGIN")
    void actionWithoutLoginTest() {
        String token = DataGenerator.generateToken();
        stubExternal("/doAction", 200);

        step("Попытка выполнить ACTION без предварительного LOGIN для токена: " + token, () -> {
            given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "ACTION")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .statusCode(403);
        });
    }

    @Test
    @Tag("negative")
    @DisplayName("Отказ в ACTION: Токен отозван через LOGOUT")
    void actionAfterLogoutTest() {
        String token = DataGenerator.generateToken();
        stubExternal("/auth", 200);
        stubExternal("/doAction", 200);

        step("1. Выполнить LOGIN для токена: " + token, () ->
                given().spec(InternalSpecs.requestSpec).formParam("token", token).formParam("action", "LOGIN").post("/endpoint"));

        step("2. Выполнить LOGOUT для токена: " + token, () ->
                given().spec(InternalSpecs.requestSpec).formParam("token", token).formParam("action", "LOGOUT").post("/endpoint"));

        step("3. Попытка ACTION после LOGOUT для токена: " + token, () -> {
            given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "ACTION")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .statusCode(403);
        });
    }

    @Test
    @Tag("negative")
    @DisplayName("Ошибка LOGIN: Внешний сервис авторизации недоступен (500)")
    void externalAuthErrorTest() {
        String token = DataGenerator.generateToken();
        stubExternal("/auth", 500);

        step("Попытка LOGIN при сбое внешнего сервиса для токена: " + token, () -> {
            ResultResponse response = given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", token)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .extract().as(ResultResponse.class);

            step("Проверить, что приложение вернуло ERROR из-за ошибки внешней системы", () -> {
                assertThat(response.getResult()).isEqualTo("ERROR");
                assertThat(response.getMessage()).isNotNull();
            });
        });
    }

    @Test
    @Tag("negative")
    @DisplayName("Ошибка LOGIN: Невалидный формат токена (символ Z)")
    void invalidTokenFormatTest() {
        String invalidToken = "1234567890ABCDEF1234567890ABCDEZ";

        step("Запрос LOGIN с некорректным форматом токена: " + invalidToken, () -> {
            ResultResponse response = given()
                    .spec(InternalSpecs.requestSpec)
                    .formParam("token", invalidToken)
                    .formParam("action", "LOGIN")
                    .when()
                    .post("/endpoint")
                    .then()
                    .spec(InternalSpecs.responseSpec)
                    .statusCode(400)
                    .extract().as(ResultResponse.class);

            step("Проверить текст ошибки валидации формата", () -> {
                assertThat(response.getResult()).isEqualTo("ERROR");
                assertThat(response.getMessage()).contains("должно соответствовать");
            });
        });
    }
}
