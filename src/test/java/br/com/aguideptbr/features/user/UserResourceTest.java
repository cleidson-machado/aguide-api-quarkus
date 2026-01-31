package br.com.aguideptbr.features.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class UserResourceTest {

    private static final String AUTH_TOKEN = "Bearer my-token-super-recur-12345";

    @Test
    void testGetAllUsersEndpoint() {
        given()
                .header("Authorization", AUTH_TOKEN)
                .when().get("/users")
                .then()
                .statusCode(200)
                .body(notNullValue());
    }

    @Test
    void testGetUsersPaginatedEndpoint() {
        given()
                .header("Authorization", AUTH_TOKEN)
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/users/paginated")
                .then()
                .statusCode(200)
                .body("content", notNullValue())
                .body("totalItems", notNullValue())
                .body("totalPages", notNullValue())
                .body("currentPage", notNullValue());
    }

    @Test
    void testGetUserByIdNotFound() {
        given()
                .header("Authorization", AUTH_TOKEN)
                .pathParam("id", 99999L)
                .when().get("/users/{id}")
                .then()
                .statusCode(404);
    }
}
