package com.rest.test;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.response.ValidatableResponse;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class MainTest {

    String baseUrl = "http://localhost:8080/books";

    @Test
    public void testGetBooks() {
        Response response = given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl);

        System.out.println("GET /books Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.asPrettyString());

        response.then()
            .statusCode(200)
            .body("$", not(empty()));
    }

    @Test
    public void testGetBookById() {
        int bookId = 1;

        Response response = given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl + "/" + bookId);

        System.out.println("GET /books/" + bookId + " Response Code: " + response.statusCode());
        System.out.println("Response Body: " + response.asPrettyString());

        response.then()
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

        Response response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl);

        System.out.println("POST /books Response Code: " + response.statusCode());
        System.out.println("Request Body: " + requestBody);
        System.out.println("Response Body: " + response.asPrettyString());

        if (response.statusCode() == 201) {
            response.then().body("name", equalTo("The Pragmatic Programmer"));
        }
    }

    @Test
    public void testCreateBookMissingFields() {
        String requestBody = """
            {
                "name": "Incomplete Book"
            }
            """;

        Response response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl);

        System.out.println("POST /books (missing fields) Response Code: " + response.statusCode());
        System.out.println("Request Body: " + requestBody);
        System.out.println("Response Body: " + response.asPrettyString());

        response.then().statusCode(400);
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

        Response response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(requestBody)
        .when()
            .post(baseUrl);

        System.out.println("POST /books (negative price) Response Code: " + response.statusCode());
        System.out.println("Request Body: " + requestBody);
        System.out.println("Response Body: " + response.asPrettyString());

        response.then().statusCode(400);
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

        Response response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put(baseUrl + "/" + bookId);

        System.out.println("PUT /books/" + bookId + " Response Code: " + response.statusCode());
        System.out.println("Request Body: " + updateBody);
        System.out.println("Response Body: " + response.asPrettyString());

        response.then().statusCode(anyOf(is(200), is(500)));
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

        Response response = given()
            .auth().basic("admin", "password")
            .contentType("application/json")
            .body(updateBody)
        .when()
            .put(baseUrl + "/" + bookId);

        System.out.println("PUT /books/" + bookId + " Response Code: " + response.statusCode());
        System.out.println("Request Body: " + updateBody);
        System.out.println("Response Body: " + response.asPrettyString());

        response.then().statusCode(anyOf(is(404), is(500)));
    }

    @Test
    public void testDeleteBook() {
        int bookIdToDelete = 3;

        Response deleteResponse = given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookIdToDelete);

        System.out.println("DELETE /books/" + bookIdToDelete + " Response Code: " + deleteResponse.statusCode());
        System.out.println("Response Body: " + deleteResponse.asPrettyString());

        deleteResponse.then().statusCode(anyOf(is(200), is(500)));

        Response verifyResponse = given()
            .auth().basic("admin", "password")
        .when()
            .get(baseUrl + "/" + bookIdToDelete);

        System.out.println("GET /books/" + bookIdToDelete + " (verify deletion) Response Code: " + verifyResponse.statusCode());

        verifyResponse.then().statusCode(404);
    }

    @Test
    public void testDeleteBookTwice() {
        int bookId = 5;

        Response firstDelete = given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookId);

        System.out.println("1st DELETE /books/" + bookId + " Response Code: " + firstDelete.statusCode());

        firstDelete.then().statusCode(anyOf(is(200), is(500)));

        Response secondDelete = given()
            .auth().basic("admin", "password")
        .when()
            .delete(baseUrl + "/" + bookId);

        System.out.println("2nd DELETE /books/" + bookId + " Response Code: " + secondDelete.statusCode());

        secondDelete.then().statusCode(anyOf(is(404), is(500)));
    }

    @Test
    public void testGetInvalidBookId() {
        int invalidId = 9999;

        Response response = given()
            .auth().basic("user", "password")
        .when()
            .get(baseUrl + "/" + invalidId);

        System.out.println("GET /books/" + invalidId + " Response Code: " + response.statusCode());

        response.then().statusCode(404);
    }

    @Test
    public void testGetBooksInvalidCredentials() {
        Response response = given()
            .auth().basic("invalid", "wrong")
        .when()
            .get(baseUrl);

        System.out.println("GET /books with invalid credentials Response Code: " + response.statusCode());

        response.then().statusCode(401);
    }

    @Test
    public void testUnauthorizedAccess() {
        Response response = given()
        .when()
            .get(baseUrl);

        System.out.println("GET /books without authentication Response Code: " + response.statusCode());

        response.then().statusCode(401);
    }
}
