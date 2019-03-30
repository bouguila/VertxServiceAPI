package se.kry.codetest;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(VertxExtension.class)
public class TestMainVerticle {

  @BeforeEach
  void deploy_verticle(Vertx vertx, VertxTestContext testContext) {
    vertx.deployVerticle(new MainVerticle(), testContext.succeeding(id -> testContext.completeNow()));
  }

  @Test
  @DisplayName("Start a web server on localhost responding to path /service on port 8080")
  @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
  void start_http_server(Vertx vertx, VertxTestContext testContext) {
    WebClient.create(vertx)
        .get(8080, "::1", "/service")
        .send(response -> testContext.verify(() -> {
          assertEquals(200, response.result().statusCode());
          JsonArray body = response.result().bodyAsJsonArray();
          assertEquals(1, body.size());
          testContext.completeNow();
        }));
  }
    @Test
    @DisplayName("Adding a valid service via the POST API results in a 201")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_valid_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject object = new JsonObject().put("url", "http://www.kry.se");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(object, response -> testContext.verify(() -> {
                    assertEquals(201, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Adding an invalid service via the POST API results in a 400")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void add_invalid_service(Vertx vertx, VertxTestContext testContext) {
        JsonObject object = new JsonObject().put("url", "not_a_valid_url");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(object, response -> testContext.verify(() -> {
                    assertEquals(400, response.result().statusCode());
                    testContext.completeNow();
                }));
    }

    @Test
    @DisplayName("Existing services can be correctly deleted")
    @Timeout(value = 10, timeUnit = TimeUnit.SECONDS)
    void delete_service(Vertx vertx, VertxTestContext testContext) {
        // Add two services
        JsonObject object1 = new JsonObject().put("url", "http://www.one.com");
        JsonObject object2 = new JsonObject().put("url", "http://www.two.com");
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(object1, response -> testContext.verify(() -> {
                    assertEquals(201, response.result().statusCode());
                }));
        WebClient.create(vertx)
                .post(8080, "::1", "/service")
                .sendJsonObject(object2, response -> testContext.verify(() -> {
                    assertEquals(201, response.result().statusCode());
                }));
        // Then list them and verify we have two entries
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    assertEquals(2, body.size());
                }));
        // Delete the first entry and verify there is one entry less
        WebClient.create(vertx)
                .get(8080, "::1", "/service")
                .send(response -> testContext.verify(() -> {
                    assertEquals(200, response.result().statusCode());
                    JsonArray body = response.result().bodyAsJsonArray();
                    String id = body.getJsonObject(0).getString("id");
                    WebClient.create(vertx)
                            .delete(8080, "::1", "/service/" + id)
                            .send(deleteResponse -> testContext.verify(() -> {
                                assertEquals(204, deleteResponse.result().statusCode());
                            }));
                    WebClient.create(vertx)
                            .get(8080, "::1", "/service")
                            .send(getResponse -> testContext.verify(() -> {
                                assertEquals(200, response.result().statusCode());
                                JsonArray getBody = getResponse.result().bodyAsJsonArray();
                                assertEquals(1, getBody.size());
                                testContext.completeNow();
                            }));
                }));

    }

}
