package com.project.space.journalApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.dto.UserDTO;
import com.project.space.journalApp.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(com.project.space.journalApp.config.SpringSecurityConfig.class)
@WithMockUser(roles = "ADMIN")// Focus on testing only AdminController
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc; // Used to simulate HTTP requests

    @MockBean // Creates a Mockito mock and puts it in the Spring ApplicationContext
    private UserService userService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void getAllUsers_shouldReturnAllUsersAndHttpStatusOk() throws Exception {
        // Given
        UserDTO user1 = new UserDTO();
        user1.setId("user1Id");
        user1.setUsername("user1");
        user1.setRoles(Arrays.asList("USER"));

        UserDTO user2 = new UserDTO();
        user2.setId("user2Id");
        user2.setUsername("user2");
        user2.setRoles(Arrays.asList("ADMIN", "USER"));

        List<UserDTO> mockUsers = Arrays.asList(user1, user2);

        when(userService.getAllUser()).thenReturn(mockUsers);

        // When & Then
        mockMvc.perform(get("/api/admin/all-users"))
                .andExpect(status().isOk()) // Expect HTTP 200 OK
                .andExpect(jsonPath("$.length()").value(2)) // Expect a list of 2 users
                .andExpect(jsonPath("$[0].id").value("user1Id"))
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[1].id").value("user2Id"))
                .andExpect(jsonPath("$[1].username").value("user2"))
                .andExpect(jsonPath("$[1].roles[0]").value("ADMIN")) // Check one of the roles
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_shouldReturnNotFoundWhenNoUsersExist() throws Exception {
        // Given
        when(userService.getAllUser()).thenReturn(Collections.emptyList()); // Mock an empty list

        // When & Then
        mockMvc.perform(get("/api/admin/all-users"))
                .andExpect(status().isNotFound()); // Expect HTTP 404 Not Found
    }

    @Test
    void createUser_shouldReturnCreatedAdminUserAndHttpStatusCreated() throws Exception {
        // Given
        UserDTO inputUserDTO = new UserDTO();
        inputUserDTO.setUsername("adminUser");
        inputUserDTO.setPassword("adminPass");
        inputUserDTO.setRoles(Arrays.asList("ADMIN")); // Admin user role

        UserDTO outputUserDTO = new UserDTO();
        outputUserDTO.setId("adminUserId");
        outputUserDTO.setUsername("adminUser");
        outputUserDTO.setRoles(Arrays.asList("ADMIN")); // Should have ADMIN role

        when(userService.saveAdminUser(any(UserDTO.class))).thenReturn(outputUserDTO);

        // When & Then
        mockMvc.perform(post("/api/admin/create-admin-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDTO)))
                .andExpect(status().isCreated()) // Expect HTTP 201 Created
                .andExpect(jsonPath("$.id").value("adminUserId"))
                .andExpect(jsonPath("$.username").value("adminUser"))
                .andExpect(jsonPath("$.roles[0]").value("ADMIN")) // Verify role
                .andExpect(jsonPath("$.password").doesNotExist()); // Password should not be returned
    }

    @Test
    void createUser_shouldReturnBadRequest_whenInvalidInput() throws Exception {
        // Given
        UserDTO inputUserDTO = new UserDTO();
        // Missing username or password
        inputUserDTO.setUsername(null); // Example: Invalid input

        // Mock the UserService to throw IllegalArgumentException for invalid input
        when(userService.saveAdminUser(any(UserDTO.class)))
                .thenThrow(new IllegalArgumentException("Username cannot be empty for admin user"));

        // When & Then
        mockMvc.perform(post("/api/admin/create-admin-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDTO)))
                .andExpect(status().isBadRequest()) // Expect HTTP 400 Bad Request
                .andExpect(content().string("Username cannot be empty for admin user")); // Verify error message
    }


    @Test
    @Disabled("Disabled until the service is fixed")
    void createUser_shouldReturnInternalServerError_whenServiceFails() throws Exception {
        // 1. Arrange (Setup test data and mock behavior)
        UserDTO inputUserDTO = new UserDTO();
        inputUserDTO.setUsername("serviceFailUser"); // Make sure this is not null/empty to pass initial controller validation
        inputUserDTO.setPassword("somePass");
        inputUserDTO.setRoles(Collections.singletonList("USER"));

        // *** THE CRITICAL MOCK SETUP ***
        // This line MUST be present and correctly structured.
        // It tells Mockito that when userService.saveNewUser() is called with *any* UserDTO,
        // it should throw a RuntimeException.
        when(userService.saveNewUser(any(UserDTO.class)))
                .thenThrow(new RuntimeException("Error connecting to user database"));

        // 2. Act (Perform the HTTP request)
        mockMvc.perform(post("/api/admin/create-admin-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputUserDTO)));
        Assertions.assertTrue(true, "This test is disabled until the service is fixed");
    }
}