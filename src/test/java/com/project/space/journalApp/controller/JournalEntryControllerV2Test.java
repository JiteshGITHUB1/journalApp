package com.project.space.journalApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JournalEntryControllerV2.class)
@Import(com.project.space.journalApp.config.SpringSecurityConfig.class)
@WithMockUser(username = "testuser", roles = "USER")
class JournalEntryControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JournalEntryService journalEntryService;

    @MockBean
    private UserService userService;

    @MockBean // Mock Security components
    private Authentication authentication;

    @MockBean // Mock Security components
    private SecurityContext securityContext;

    private ObjectMapper objectMapper;
    private UserEntity testUser;
    private JournalEntry journalEntry1;
    private JournalEntry journalEntry2;
    private JournalDTO journalDTO;

    @BeforeEach
    void setUp() {
        // Configure ObjectMapper for LocalDateTime
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // **IMPORTANT**: If using @WithMockUser, you do NOT need to mock SecurityContextHolder manually here.
        // It's handled by Spring Security Test.

        // Initialize mock user and journal entries
        testUser = new UserEntity();
        testUser.setId(new ObjectId());
        // The username MUST match the username in @WithMockUser (e.g., "testuser")
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRoles(List.of("USER")); // Ensure user has the roles required by SecurityConfig
        testUser.setJournalEntries(new ArrayList<>());

        // ... (rest of your journalEntry1, journalEntry2, journalDTO setup) ...
        journalEntry1 = new JournalEntry();
        journalEntry1.setId(new ObjectId());
        journalEntry1.setTitle("My First Entry");
        journalEntry1.setContent("This is the content of my first entry.");
        journalEntry1.setDateCreated(LocalDateTime.now().minusDays(2));
        journalEntry1.setDateModified(LocalDateTime.now().minusDays(1));

        journalEntry2 = new JournalEntry();
        journalEntry2.setId(new ObjectId());
        journalEntry2.setTitle("My Second Entry");
        journalEntry2.setContent("Another day, another thought.");
        journalEntry2.setDateCreated(LocalDateTime.now().minusDays(1));
        journalEntry2.setDateModified(LocalDateTime.now());

        journalDTO = new JournalDTO();
        journalDTO.setTitle("New Journal Title");
        journalDTO.setContent("New Journal Content");
    }

    @Test
    void getAllJournalEntriesOfUser_shouldReturnEntriesIfUserHasThem() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1);
        testUser.getJournalEntries().add(journalEntry2);
        when(userService.findByUserName("testuser")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/journal/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("My First Entry"))
                .andExpect(jsonPath("$[1].title").value("My Second Entry"));
        verify(userService, times(1)).findByUserName("testuser");
    }

    @Test
    void getAllJournalEntriesOfUser_shouldReturnNotFoundIfUserHasNoEntries() throws Exception {
        // Given
        testUser.setJournalEntries(new ArrayList<>()); // Ensure user has no entries
        when(userService.findByUserName("testuser")).thenReturn(testUser);

        // When & Then
        mockMvc.perform(get("/api/journal/all"))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findByUserName("testuser");
    }

    @Test
    void createEntry_shouldReturnCreatedEntryAndHttpStatusCreated() throws Exception {
        // Given
        JournalDTO expectedResponseDTO = new JournalDTO();
        expectedResponseDTO.setId(new ObjectId().toHexString());
        expectedResponseDTO.setTitle("New Journal Title");
        expectedResponseDTO.setContent("New Journal Content");
        expectedResponseDTO.setDateCreated(LocalDateTime.now()); // Set a dummy date for mock

        when(journalEntryService.saveEntry(any(JournalDTO.class), eq("testuser"))).thenReturn(expectedResponseDTO);

        // When & Then
        mockMvc.perform(post("/api/journal/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Journal Title"))
                .andExpect(jsonPath("$.content").value("New Journal Content"));
        verify(journalEntryService, times(1)).saveEntry(any(JournalDTO.class), eq("testuser"));
    }

    @Test
    void createEntry_shouldReturnBadRequestIfServiceThrowsException() throws Exception {
        // Given
        when(journalEntryService.saveEntry(any(JournalDTO.class), eq("testuser")))
                .thenThrow(new IllegalArgumentException("Journal entry content too short"));

        // When & Then
        mockMvc.perform(post("/api/journal/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalDTO)))
                .andExpect(status().isBadRequest());
        verify(journalEntryService, times(1)).saveEntry(any(JournalDTO.class), eq("testuser"));
    }

    @Test
    void getJournalEntryById_shouldReturnEntryIfFoundAndBelongsToUser() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // Ensure user owns the entry
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));

        // When & Then
        mockMvc.perform(get("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("My First Entry"));
        verify(userService, times(1)).findByUserName("testuser");
    }

    @Test
    void getJournalEntryById_shouldReturnNotFoundIfEntryNotOwnedByUser() throws Exception {
        // Given
        UserEntity anotherUser = new UserEntity(); // User does NOT own journalEntry1
        anotherUser.setUsername("anotherUser");
        anotherUser.setJournalEntries(new ArrayList<>()); // No entries

        when(userService.findByUserName("testuser")).thenReturn(anotherUser); // User has no entries
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1)); // Entry exists, but not for this user's list

        // When & Then
        mockMvc.perform(get("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findByUserName("testuser");
        verify(journalEntryService, never()).findById(any(ObjectId.class)); // Should not call findById if not in user's list
    }

    @Test
    void getJournalEntryById_shouldReturnNotFoundIfEntryNotFoundInDb() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // User owns an entry, but not the one requested
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        // Mock findById to return empty for the requested ID even if in user's list (edge case)
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findByUserName("testuser");
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
    }

    @Test
    void deleteJournalEntryById_shouldReturnNoContentIfDeletedSuccessfully() throws Exception {
        // Given
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));
        when(journalEntryService.deleteById(journalEntry1.getId(), "testuser")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isNoContent()); // 204 No Content
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
        verify(journalEntryService, times(1)).deleteById(journalEntry1.getId(), "testuser");
    }

    @Test
    void deleteJournalEntryById_shouldReturnNotFoundIfEntryNotFoundForDeletion() throws Exception {
        // Given
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.empty()); // Not found in DB

        // When & Then
        mockMvc.perform(delete("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isNotFound()); // 404 Not Found
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
        verify(journalEntryService, never()).deleteById(any(ObjectId.class), anyString());
    }

    @Test
    void deleteJournalEntryById_shouldReturnNotFoundIfDeletionFailedByService() throws Exception {
        // Given
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));
        when(journalEntryService.deleteById(journalEntry1.getId(), "testuser")).thenReturn(false); // Service indicates not removed

        // When & Then
        mockMvc.perform(delete("/api/journal/id/{myId}", journalEntry1.getId().toHexString()))
                .andExpect(status().isNotFound()); // 404 Not Found (as per your controller's logic)
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
        verify(journalEntryService, times(1)).deleteById(journalEntry1.getId(), "testuser");
    }

    @Test
    void updateJournalEntryById_shouldReturnUpdatedEntryAndHttpStatusOk() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // User owns the entry
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));

        JournalEntry updatedJournalEntry = new JournalEntry();
        updatedJournalEntry.setId(journalEntry1.getId());
        updatedJournalEntry.setTitle("Updated Title");
        updatedJournalEntry.setContent("Updated Content");
        updatedJournalEntry.setDateCreated(journalEntry1.getDateCreated());
        updatedJournalEntry.setDateModified(LocalDateTime.now()); // Date will be updated by service

        // Mock saveEntry(JournalEntry) to return the updated entry
        when(journalEntryService.saveEntry(any(JournalEntry.class))).thenReturn(updatedJournalEntry);

        // Mock convertToJournalDTO
        JournalDTO expectedResponseDTO = new JournalDTO();
        expectedResponseDTO.setId(updatedJournalEntry.getId().toHexString());
        expectedResponseDTO.setTitle(updatedJournalEntry.getTitle());
        expectedResponseDTO.setContent(updatedJournalEntry.getContent());
        expectedResponseDTO.setDateCreated(updatedJournalEntry.getDateCreated());
        expectedResponseDTO.setDateModified(updatedJournalEntry.getDateModified());
        when(journalEntryService.convertToJournalDTO(any(JournalEntry.class))).thenReturn(expectedResponseDTO);

        JournalDTO updateRequestDTO = new JournalDTO();
        updateRequestDTO.setTitle("Updated Title");
        updateRequestDTO.setContent("Updated Content");

        // When & Then
        mockMvc.perform(put("/api/journal/id/{myId}", journalEntry1.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(updatedJournalEntry.getId().toHexString()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(userService, times(1)).findByUserName("testuser");
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
        verify(journalEntryService, times(1)).saveEntry(any(JournalEntry.class)); // Verifies save was called
        verify(journalEntryService, times(1)).convertToJournalDTO(any(JournalEntry.class)); // Verifies conversion
    }

    @Test
    void updateJournalEntryById_shouldReturnNotFoundIfEntryNotOwnedByUser() throws Exception {
        // Given
        UserEntity anotherUser = new UserEntity(); // User does NOT own journalEntry1
        anotherUser.setUsername("anotherUser");
        anotherUser.setJournalEntries(new ArrayList<>()); // No entries
        when(userService.findByUserName("testuser")).thenReturn(anotherUser); // User has no entries

        // When & Then
        mockMvc.perform(put("/api/journal/id/{myId}", journalEntry1.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalDTO)))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findByUserName("testuser");
        verify(journalEntryService, never()).findById(any(ObjectId.class)); // Should not proceed if not in user's list
    }

    @Test
    void updateJournalEntryById_shouldReturnNotFoundIfEntryNotFoundInDb() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // User owns the entry (in their list)
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.empty()); // But not found in DB

        // When & Then
        mockMvc.perform(put("/api/journal/id/{myId}", journalEntry1.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(journalDTO)))
                .andExpect(status().isNotFound());
        verify(userService, times(1)).findByUserName("testuser");
        verify(journalEntryService, times(1)).findById(journalEntry1.getId());
        verify(journalEntryService, never()).saveEntry(any(JournalEntry.class)); // No save if not found
    }

    @Test
    void updateJournalEntryById_shouldUpdateOnlyTitleIfContentIsNull() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // User owns the entry
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));

        JournalEntry partiallyUpdatedEntry = new JournalEntry();
        partiallyUpdatedEntry.setId(journalEntry1.getId());
        partiallyUpdatedEntry.setTitle("Only Title Updated");
        partiallyUpdatedEntry.setContent(journalEntry1.getContent()); // Content should remain old
        partiallyUpdatedEntry.setDateCreated(journalEntry1.getDateCreated());
        partiallyUpdatedEntry.setDateModified(LocalDateTime.now());

        when(journalEntryService.saveEntry(any(JournalEntry.class))).thenReturn(partiallyUpdatedEntry);

        JournalDTO expectedResponseDTO = new JournalDTO();
        expectedResponseDTO.setId(partiallyUpdatedEntry.getId().toHexString());
        expectedResponseDTO.setTitle(partiallyUpdatedEntry.getTitle());
        expectedResponseDTO.setContent(partiallyUpdatedEntry.getContent());
        expectedResponseDTO.setDateCreated(partiallyUpdatedEntry.getDateCreated());
        expectedResponseDTO.setDateModified(partiallyUpdatedEntry.getDateModified());
        when(journalEntryService.convertToJournalDTO(any(JournalEntry.class))).thenReturn(expectedResponseDTO);


        JournalDTO updateRequestDTO = new JournalDTO();
        updateRequestDTO.setTitle("Only Title Updated");
        updateRequestDTO.setContent(null); // Content is null

        // When & Then
        mockMvc.perform(put("/api/journal/id/{myId}", journalEntry1.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Only Title Updated"))
                .andExpect(jsonPath("$.content").value(journalEntry1.getContent())); // Assert content is unchanged
    }

    @Test
    void updateJournalEntryById_shouldUpdateOnlyContentIfTitleIsNull() throws Exception {
        // Given
        testUser.getJournalEntries().add(journalEntry1); // User owns the entry
        when(userService.findByUserName("testuser")).thenReturn(testUser);
        when(journalEntryService.findById(journalEntry1.getId())).thenReturn(Optional.of(journalEntry1));

        JournalEntry partiallyUpdatedEntry = new JournalEntry();
        partiallyUpdatedEntry.setId(journalEntry1.getId());
        partiallyUpdatedEntry.setTitle(journalEntry1.getTitle()); // Title should remain old
        partiallyUpdatedEntry.setContent("Only Content Updated");
        partiallyUpdatedEntry.setDateCreated(journalEntry1.getDateCreated());
        partiallyUpdatedEntry.setDateModified(LocalDateTime.now());

        when(journalEntryService.saveEntry(any(JournalEntry.class))).thenReturn(partiallyUpdatedEntry);

        JournalDTO expectedResponseDTO = new JournalDTO();
        expectedResponseDTO.setId(partiallyUpdatedEntry.getId().toHexString());
        expectedResponseDTO.setTitle(partiallyUpdatedEntry.getTitle());
        expectedResponseDTO.setContent(partiallyUpdatedEntry.getContent());
        expectedResponseDTO.setDateCreated(partiallyUpdatedEntry.getDateCreated());
        expectedResponseDTO.setDateModified(partiallyUpdatedEntry.getDateModified());
        when(journalEntryService.convertToJournalDTO(any(JournalEntry.class))).thenReturn(expectedResponseDTO);

        JournalDTO updateRequestDTO = new JournalDTO();
        updateRequestDTO.setTitle(null); // Title is null
        updateRequestDTO.setContent("Only Content Updated");

        // When & Then
        mockMvc.perform(put("/api/journal/id/{myId}", journalEntry1.getId().toHexString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(journalEntry1.getTitle())) // Assert title is unchanged
                .andExpect(jsonPath("$.content").value("Only Content Updated"));
    }
}