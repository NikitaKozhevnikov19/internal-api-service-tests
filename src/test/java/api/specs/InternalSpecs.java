package api.specs;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import static api.helpers.CustomApiListener.withCustomTemplates;

public class InternalSpecs {
    public static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost:8080")
            .setContentType(ContentType.URLENC)
            .addHeader("X-Api-Key", "qazWSXedc")
            .addFilter(withCustomTemplates())
            .build();


    public static ResponseSpecification responseSpec = new ResponseSpecBuilder()
            .expectContentType(ContentType.JSON)
            .build();
}
