package br.com.aguideptbr.features.user;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class UserResourceTest {

    @Test
    void testGetAllUsersEndpoint() {
        given()
            .when().get("/users")
            .then()
            .statusCode(200)
            .body(notNullValue());
    }

    @Test
    void testGetUsersPaginatedEndpoint() {
        given()
            .queryParam("page", 0)
            .queryParam("size", 10)
            .when().get("/users/paginated")
            .then()
            .statusCode(200)
            .body("content", notNullValue())
            .body("totalElements", notNullValue())
            .body("totalPages", notNullValue());
    }

    @Test
    void testGetUserByIdNotFound() {
        given()
            .pathParam("id", 99999L)
            .when().get("/users/{id}")
            .then()
            .statusCode(404);
    }
}
