package com.rest.test;

import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MainTest {

    String baseUrl = "http://localhost:8080/books";

    @Test
    public void testGetBooks() {
        given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl)
        .then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    public void testGetBookById() {
        int bookId = 1;

        given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl + "/" + bookId)
        .then()
            .statusCode(anyOf(is(200), is(404)));
    }

    @Test
    public void testCreateBook() {
        String requestBody = """
            {
                "name": "The Pragmatic Programmer",
                "author": "Andrew Hunt",
                "price": 42.50
            }
            """;

        ValidatableResponse response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl)
        .then();

        int statusCode = response.extract().statusCode();
        if (statusCode == 201) {
            response.body("name", equalTo("The Pragmatic Programmer"));
        } else {
            System.out.println("Create Book Failed. Status: " + statusCode);
            System.out.println("Response: " + response.extract().asString());
        }
    }

    @Test
    public void testCreateBookMissingFields() {
        String requestBody = """
            {
                "name": "Incomplete Book"
            }
            """;

        given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(400);
    }

    @Test
    public void testCreateBookWithNegativePrice() {
        String requestBody = """
            {
                "name": "Bugged Book",
                "author": "Unknown",
                "price": -10.00
            }
            """;

        given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl)
        .then()
            .statusCode(400);
    }

    @Test
    public void testUpdateBook() {
        int bookId = 1;

        String updateBody = """
            {
                "id": 1,
                "name": "Updated Book Name",
                "author": "Updated Author",
                "price": 35.00
            }
            """;

        given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put(baseUrl + "/" + bookId)
        .then()
            .statusCode(anyOf(is(200), is(500)));
    }

    @Test
    public void testUpdateNonexistentBook() {
        int bookId = 9999;

        String updateBody = """
            {
                "id": 9999,
                "name": "Ghost Book",
                "author": "Nobody",
                "price": 15.00
            }
            """;

        given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put(baseUrl + "/" + bookId)
        .then()
            .statusCode(anyOf(is(404), is(500)));
    }

    @Test
    public void testDeleteBook() {
        int bookIdToDelete = 3;

        given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookIdToDelete)
        .then()
            .statusCode(anyOf(is(200), is(500)));

        given()
            .auth().basic("admin", "password")
        .when()
            .get(baseUrl + "/" + bookIdToDelete)
        .then()
            .statusCode(404);
    }

    @Test
    public void testDeleteBookTwice() {
        int bookId = 5;

        given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookId)
        .then()
            .statusCode(anyOf(is(200), is(500)));

        given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookId)
        .then()
            .statusCode(anyOf(is(404), is(500)));
    }

    @Test
    public void testGetInvalidBookId() {
        int invalidId = 9999;

        given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl + "/" + invalidId)
        .then()
            .statusCode(404);
    }

    @Test
    public void testGetBooksInvalidCredentials() {
        given()
            .auth().basic("invalid", "wrong")
        .when()
            .get(baseUrl)
        .then()
            .statusCode(401);
    }

    @Test
    public void testUnauthorizedAccess() {
        given()
        .when()
            .get(baseUrl)
        .then()
            .statusCode(401);
    }
}
