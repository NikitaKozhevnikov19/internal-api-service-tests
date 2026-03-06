package api.helpers;

import io.qameta.allure.Attachment;
import org.apache.commons.lang3.RandomStringUtils;

public class DataGenerator {

    @Attachment("Сгенерированный тестовый токен")
    public static String generateToken() {
        String token = RandomStringUtils.random(32, "0123456789ABCDEF");
        return token;
    }
}