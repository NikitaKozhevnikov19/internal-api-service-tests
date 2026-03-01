package api.helpers;

import org.apache.commons.lang3.RandomStringUtils;

public class DataGenerator {
    public static String generateToken() {
        return RandomStringUtils.random(32, "0123456789ABCDEF");
    }
}