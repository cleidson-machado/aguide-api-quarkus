package br.com.aguideptbr.features.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Testes de integração para UserResource.
 *
 * IMPORTANTE: O AuthenticationFilter está desabilitado em testes via
 * quarkus.arc.exclude-types em src/test/resources/application.properties.
 * Por isso, NÃO é necessário passar header Authorization nos testes.
 */
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
                .body("totalItems", notNullValue())
                .body("totalPages", notNullValue())
                .body("currentPage", notNullValue());
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
