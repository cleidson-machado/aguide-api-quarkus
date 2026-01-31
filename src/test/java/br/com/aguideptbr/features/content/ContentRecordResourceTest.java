package br.com.aguideptbr.features.content;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import br.com.aguideptbr.TestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

/**
 * Testes de integração para ContentRecordResource.
 * Valida operações CRUD e funcionalidades de busca/ordenação.
 *
 * IMPORTANTE: O AuthenticationFilter é desabilitado via TestProfile
 * que configura quarkus.arc.exclude-types programaticamente.
 * Isso permite que os testes executem sem necessidade de token Bearer.
 */
@QuarkusTest
@io.quarkus.test.junit.TestProfile(TestProfile.class)
class ContentRecordResourceTest {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Test
    void testListContents_WithDefaultParameters() {
        given()
                .when().get("/contents")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testListContents_WithPagination() {
        given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when().get("/contents")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("page", is(0))
                .body("items", notNullValue());
    }

    @Test
    void testListContents_SortByPublishedAt() {
        given()
                .queryParam("sort", "publishedAt")
                .queryParam("order", "desc")
                .when().get("/contents")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testCreateContent_WithPublishedAt() {
        String publishedAt = LocalDateTime.now().minusDays(7).format(ISO_FORMATTER);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "title": "Test Video with PublishedAt",
                            "description": "Testing publishedAt field",
                            "url": "https://example.com/test-published-at",
                            "channelName": "Test Channel",
                            "type": "VIDEO",
                            "publishedAt": "%s"
                        }
                        """, publishedAt))
                .when().post("/contents")
                .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("title", is("Test Video with PublishedAt"))
                .body("publishedAt", notNullValue());
    }

    @Test
    void testUpdateContent_WithPublishedAt() {
        // Primeiro cria um conteúdo
        String createResponse = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Original Content",
                            "url": "https://example.com/original",
                            "type": "ARTICLE"
                        }
                        """)
                .when().post("/contents")
                .then()
                .statusCode(201)
                .extract().body().jsonPath().getString("id");

        // Agora atualiza com publishedAt
        String publishedAt = LocalDateTime.now().minusDays(3).format(ISO_FORMATTER);

        given()
                .contentType(ContentType.JSON)
                .body(String.format("""
                        {
                            "title": "Updated Content",
                            "url": "https://example.com/updated",
                            "type": "ARTICLE",
                            "publishedAt": "%s"
                        }
                        """, publishedAt))
                .when().put("/contents/" + createResponse)
                .then()
                .statusCode(200)
                .body("title", is("Updated Content"))
                .body("publishedAt", notNullValue());
    }

    @Test
    void testSearchByTitle() {
        given()
                .queryParam("q", "test")
                .when().get("/contents/search")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON);
    }

    @Test
    void testGetContentById_InvalidId() {
        given()
                .when().get("/contents/invalid-id")
                .then()
                .statusCode(400)
                .body("plusInfoMsg", containsString("Found a error"));
    }

    @Test
    void testGetContentById_NotFound() {
        String nonExistentId = "00000000-0000-0000-0000-000000000000";

        given()
                .when().get("/contents/" + nonExistentId)
                .then()
                .statusCode(404)
                .body("plusInfoMsg", containsString("No content found"));
    }

    @Test
    void testDeleteContent() {
        // Cria um conteúdo para deletar
        String contentId = given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "title": "Content to Delete",
                            "url": "https://example.com/to-delete",
                            "type": "VIDEO"
                        }
                        """)
                .when().post("/contents")
                .then()
                .statusCode(201)
                .extract().body().jsonPath().getString("id");

        // Deleta o conteúdo
        given()
                .when().delete("/contents/" + contentId)
                .then()
                .statusCode(204);

        // Verifica que não existe mais
        given()
                .when().get("/contents/" + contentId)
                .then()
                .statusCode(404);
    }

    @Test
    void testListContents_InvalidSortField() {
        given()
                .queryParam("sort", "invalidField")
                .when().get("/contents")
                .then()
                .statusCode(400)
                .body(containsString("invalid sort field"));
    }
}
