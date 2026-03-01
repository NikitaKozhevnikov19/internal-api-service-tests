package api.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import static api.helpers.CustomApiListener.withCustomTemplates; // Импортируем твой листенер

public class InternalSpecs {
    public static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost:8080")
            .setContentType(ContentType.URLENC)
            .addHeader("X-Api-Key", "qazWSXedc")
            .addFilter(withCustomTemplates())
            .build();
}
