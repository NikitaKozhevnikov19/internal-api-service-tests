package api.tests;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class TestBase {
    protected static WireMockServer wireMock = new WireMockServer(8888);

    @BeforeAll
    static void start() {
        wireMock.start();
        configureFor("localhost", 8888);
    }

    @AfterAll
    static void stop() {
        wireMock.stop();
    }

    protected void stubExternal(String path, int status) {
        wireMock.stubFor(post(urlEqualTo(path))
                .willReturn(aResponse()
                        .withStatus(status)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"status\":\"" + (status == 200 ? "success" : "fail") + "\"}")));
    }
}
