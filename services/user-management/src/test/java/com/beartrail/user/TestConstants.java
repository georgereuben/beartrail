package com.beartrail.user;

/**
 * Test constants to avoid string literal duplication in PMD reports.
 */
public final class TestConstants {

    // Test user data
    public static final String TEST_EMAIL = "john.doe@example.com";
    public static final String TEST_FIRST_NAME = "John";
    public static final String TEST_LAST_NAME = "Doe";
    public static final String TEST_PASSWORD = "password123";

    // API endpoints
    public static final String LOGIN_ENDPOINT = "/api/auth/login";
    public static final String REGISTER_ENDPOINT = "/api/auth/register";

    // JSON path expressions
    public static final String JSON_SUCCESS = "$.success";
    public static final String JSON_MESSAGE = "$.message";

    // Messages
    public static final String AUTH_SUCCESS_MESSAGE = "Authentication successful";
    public static final String REGISTRATION_SUCCESS_MESSAGE = "User registered successfully";

    private TestConstants() {
    }
}
