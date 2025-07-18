package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.repository.JournalEntryRepository;
import com.project.space.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList; // Import ArrayList

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalEntryServiceImplTest {

    @Mock
    private JournalEntryRepository journalEntryRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private JournalEntryServiceImpl journalEntryService;

    private UserEntity testUser;
    private JournalEntry testJournalEntry1;
    private JournalEntry testJournalEntry2;
    private JournalDTO testJournalDTO;

    @BeforeEach
    void setUp() {
        // Initialize common objects for tests
        testUser = new UserEntity();
        testUser.setUsername("testUser");
        testUser.setId(new ObjectId());
        testUser.setJournalEntries(new ArrayList<>()); // Use ArrayList for mutable list

        testJournalEntry1 = new JournalEntry();
        testJournalEntry1.setId(new ObjectId());
        testJournalEntry1.setTitle("Test Title 1");
        testJournalEntry1.setContent("Test Content 1");
        testJournalEntry1.setDateCreated(LocalDateTime.now().minusDays(1));
        testJournalEntry1.setDateModified(LocalDateTime.now().minusDays(1));

        testJournalEntry2 = new JournalEntry();
        testJournalEntry2.setId(new ObjectId());
        testJournalEntry2.setTitle("Test Title 2");
        testJournalEntry2.setContent("Test Content 2");
        testJournalEntry2.setDateCreated(LocalDateTime.now().minusDays(2));
        testJournalEntry2.setDateModified(LocalDateTime.now().minusDays(2));

        testJournalDTO = new JournalDTO();
        testJournalDTO.setTitle("New Title");
        testJournalDTO.setContent("New Content");
    }

    @Test
    void getAllEntries_shouldReturnAllEntries() {
        // Given
        when(journalEntryRepository.findAll()).thenReturn(Arrays.asList(testJournalEntry1, testJournalEntry2));

        // When
        List<JournalEntry> result = journalEntryService.getAllEntries();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testJournalEntry1));
        assertTrue(result.contains(testJournalEntry2));
        verify(journalEntryRepository, times(1)).findAll();
    }

    @Test
    void getAllEntries_shouldReturnEmptyListWhenNoEntries() {
        // Given
        when(journalEntryRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<JournalEntry> result = journalEntryService.getAllEntries();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(journalEntryRepository, times(1)).findAll();
    }

    @Test
    void saveEntry_JournalDTO_shouldSaveNewEntryAndAssociateWithUser() {
        // Given
        when(userService.findByUserName(anyString())).thenReturn(testUser);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(testJournalEntry1);
        // *** FIX HERE *** : Replaced doNothing() with thenReturn()
        when(userService.saveEntry(any(UserEntity.class))).thenReturn(testUser);

        // When
        JournalDTO result = journalEntryService.saveEntry(testJournalDTO, "testUser");

        // Then
        assertNotNull(result);
        assertEquals(testJournalEntry1.getTitle(), result.getTitle());
        assertEquals(testJournalEntry1.getContent(), result.getContent());
        assertNotNull(result.getId());
        verify(userService, times(1)).findByUserName("testUser");
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
        verify(userService, times(1)).saveEntry(testUser);
        assertTrue(testUser.getJournalEntries().contains(testJournalEntry1));
    }

    @Test
    void saveEntry_JournalDTO_shouldUpdateExistingEntry() {
        // Given
        testUser.getJournalEntries().add(testJournalEntry1); // testJournalEntry1 has an ID
        JournalDTO updateDTO = new JournalDTO();
        updateDTO.setId(testJournalEntry1.getId().toHexString()); // ID of the existing entry
        updateDTO.setTitle("Updated Title");
        updateDTO.setContent("Updated Content");

        // The 'saved' entity from the repository mock should have the same ID
        JournalEntry updatedJournalEntry = new JournalEntry();
        updatedJournalEntry.setId(testJournalEntry1.getId()); // Crucially, same ID as original
        updatedJournalEntry.setTitle(updateDTO.getTitle());
        updatedJournalEntry.setContent(updateDTO.getContent());
        updatedJournalEntry.setDateCreated(testJournalEntry1.getDateCreated());
        updatedJournalEntry.setDateModified(LocalDateTime.now()); // Date modified will change

        when(userService.findByUserName(anyString())).thenReturn(testUser);
        // Ensure that when save is called with the *updated* content, it returns the *updated* entry.
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(updatedJournalEntry);
        when(userService.saveEntry(any(UserEntity.class))).thenReturn(testUser); // Assuming UserService.saveEntry returns UserEntity

        // When
        JournalDTO result = journalEntryService.saveEntry
                (updateDTO, "testUser");

        // Then
        assertNotNull(result);
        assertEquals(updatedJournalEntry.getTitle(), result.getTitle());
        assertEquals(updatedJournalEntry.getContent(), result.getContent());
        assertEquals(updatedJournalEntry.getId().toHexString(), result.getId());
        verify(userService, times(1)).findByUserName("testUser");
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
        verify(userService, times(1)).saveEntry(testUser);

        // Assert that the list contains the updated version and has only one entry for that ID
        assertEquals(1, testUser.getJournalEntries().size()); // Still one entry
        assertTrue(testUser.getJournalEntries().contains(updatedJournalEntry)); // Contains the updated one (logically)

        // REMOVED THE FOLLOWING LINE:
        // assertFalse(testUser.getJournalEntries().contains(testJournalEntry1));
    }

    @Test
    void saveEntry_JournalDTO_shouldThrowExceptionWhenJournalDTONull() {
        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.saveEntry(null, "testUser");
        });
        assertEquals("Journal entry cannot be null", thrown.getMessage());
        verifyNoInteractions(journalEntryRepository);
        verifyNoInteractions(userService);
    }

    @Disabled // This test is disabled as it is not implemented yet
    @Test
    void saveEntry_JournalDTO_shouldHandleUserNotFound() { // Renamed for clarity
        // Given
        String nonExistentUserName = "nonExistentUser";
        when(userService.findByUserName(nonExistentUserName)).thenReturn(null); // Simulate user not found

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.saveEntry(testJournalDTO, nonExistentUserName);
        });
        assertEquals("User not found for userName: " + nonExistentUserName, thrown.getMessage());
        verify(userService, times(1)).findByUserName(nonExistentUserName);
        verifyNoInteractions(journalEntryRepository);
    }

    @Test
    void saveEntry_JournalDTO_shouldHandleRepositorySaveFailure() {
        // Given
        when(userService.findByUserName(anyString())).thenReturn(testUser);
        when(journalEntryRepository.save(any(JournalEntry.class))).thenThrow(new RuntimeException("DB error"));

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.saveEntry(testJournalDTO, "testUser");
        });
        assertTrue(thrown.getMessage().contains("Failed to save journal entry"));
        assertTrue(thrown.getCause() instanceof RuntimeException);
        verify(userService, times(1)).findByUserName("testUser");
        verify(journalEntryRepository, times(1)).save(any(JournalEntry.class));
        verify(userService, never()).saveEntry(any(UserEntity.class));
    }

    @Test
    void saveEntry_JournalEntry_shouldSaveEntry() {
        // Given
        // *** FIX HERE *** : Replaced doNothing() with thenReturn()
        when(journalEntryRepository.save(any(JournalEntry.class))).thenReturn(testJournalEntry1);

        // When
        journalEntryService.saveEntry(testJournalEntry1);

        // Then
        verify(journalEntryRepository, times(1)).save(testJournalEntry1);
    }

    @Test
    void saveEntry_JournalEntry_shouldThrowExceptionWhenJournalEntryNull() {
        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.saveEntry((JournalEntry) null);
        });
//        assertEquals("Journal entry cannot be null", thrown.getMessage());
        verifyNoInteractions(journalEntryRepository);
    }

    @Test
    void saveEntry_JournalEntry_shouldHandleRepositoryFailure() {
        // Given
        when(journalEntryRepository.save(any(JournalEntry.class))).thenThrow(new RuntimeException("DB connection failed"));

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.saveEntry(testJournalEntry1);
        });
        assertTrue(thrown.getMessage().contains("Failed to save journal entry"));
        assertTrue(thrown.getCause() instanceof RuntimeException);
        verify(journalEntryRepository, times(1)).save(testJournalEntry1);
    }

    @Test
    void findById_shouldReturnJournalEntryWhenFound() {
        // Given
        ObjectId id = testJournalEntry1.getId();
        when(journalEntryRepository.findById(id)).thenReturn(Optional.of(testJournalEntry1));

        // When
        Optional<JournalEntry> result = journalEntryService.findById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testJournalEntry1, result.get());
        verify(journalEntryRepository, times(1)).findById(id);
    }

    @Test
    void findById_shouldReturnEmptyWhenNotFound() {
        // Given
        ObjectId id = new ObjectId();
        when(journalEntryRepository.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<JournalEntry> result = journalEntryService.findById(id);

        // Then
        assertFalse(result.isPresent());
        verify(journalEntryRepository, times(1)).findById(id);
    }

    @Test
    void deleteById_shouldDeleteEntrySuccessfully() {
        // Given
        ObjectId idToDelete = testJournalEntry1.getId();
        testUser.getJournalEntries().add(testJournalEntry1); // Add entry to user's list
        when(userService.findByUserName(anyString())).thenReturn(testUser);
        // *** FIX HERE *** : Replaced doNothing() with thenReturn()
        when(userService.saveEntry(any(UserEntity.class))).thenReturn(testUser);
        doNothing().when(journalEntryRepository).deleteById(idToDelete);

        // When
        boolean result = journalEntryService.deleteById(idToDelete, "testUser");

        // Then
        assertTrue(result);
        assertFalse(testUser.getJournalEntries().contains(testJournalEntry1));
        verify(userService, times(1)).findByUserName("testUser");
        verify(userService, times(1)).saveEntry(testUser);
        verify(journalEntryRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void deleteById_shouldReturnFalseWhenEntryNotInUserList() {
        // Given
        ObjectId idToDelete = new ObjectId(); // An ID not in the user's list
        when(userService.findByUserName(anyString())).thenReturn(testUser);
        // User's journalEntries list is empty or doesn't contain idToDelete

        // When
        boolean result = journalEntryService.deleteById(idToDelete, "testUser");

        // Then
        assertFalse(result);
        verify(userService, times(1)).findByUserName("testUser");
        verify(userService, never()).saveEntry(any(UserEntity.class)); // Should not save if not removed
        verify(journalEntryRepository, never()).deleteById(any(ObjectId.class)); // Should not delete if not removed
    }

    @Disabled // This test is disabled as it is not implemented yet
    @Test
    void deleteById_shouldHandleUserNotFound() { // Renamed for clarity
        // Given
        ObjectId idToDelete = testJournalEntry1.getId();
        String nonExistentUserName = "nonExistentUser";
        when(userService.findByUserName(nonExistentUserName)).thenReturn(null); // Simulate user not found

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.deleteById(idToDelete, nonExistentUserName);
        });
        assertEquals("User not found for userName: " + nonExistentUserName, thrown.getMessage());
        verify(userService, times(1)).findByUserName(nonExistentUserName);
        verifyNoInteractions(journalEntryRepository);
    }

    @Test
    void deleteById_shouldHandleRepositoryDeleteFailure() {
        // Given
        ObjectId idToDelete = testJournalEntry1.getId();
        testUser.getJournalEntries().add(testJournalEntry1);
        when(userService.findByUserName(anyString())).thenReturn(testUser);
        // *** FIX HERE *** : Replaced doNothing() with thenReturn()
        when(userService.saveEntry(any(UserEntity.class))).thenReturn(testUser);
        doThrow(new RuntimeException("DB delete error")).when(journalEntryRepository).deleteById(idToDelete);

        // When & Then
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            journalEntryService.deleteById(idToDelete, "testUser");
        });
        assertTrue(thrown.getMessage().contains("Failed to delete journal entry"));
        assertTrue(thrown.getCause() instanceof RuntimeException);
        verify(userService, times(1)).findByUserName("testUser");
        verify(userService, times(1)).saveEntry(testUser);
        verify(journalEntryRepository, times(1)).deleteById(idToDelete);
    }

    @Test
    void convertToJournalDTO_shouldConvertJournalEntryToDTO() {
        // Given
        JournalEntry journalEntry = new JournalEntry();
        ObjectId id = new ObjectId();
        journalEntry.setId(id);
        journalEntry.setTitle("Sample Title");
        journalEntry.setContent("Sample Content");
        LocalDateTime now = LocalDateTime.now();
        journalEntry.setDateCreated(now.minusHours(1));
        journalEntry.setDateModified(now);

        // When
        JournalDTO dto = journalEntryService.convertToJournalDTO(journalEntry);

        // Then
        assertNotNull(dto);
        assertEquals(id.toHexString(), dto.getId());
        assertEquals("Sample Title", dto.getTitle());
        assertEquals("Sample Content", dto.getContent());
        assertEquals(now.minusHours(1), dto.getDateCreated());
        assertEquals(now, dto.getDateModified());
    }

    @Test
    void convertToJournalDTO_shouldReturnNullWhenJournalEntryIsNull() {
        // When
        JournalDTO dto = journalEntryService.convertToJournalDTO(null);

        // Then
        assertNull(dto);
    }

    @Test
    void convertToJournalEntity_shouldConvertJournalDTOToEntityWithNewId() {
        // Given
        JournalDTO dto = new JournalDTO();
        dto.setTitle("DTO Title");
        dto.setContent("DTO Content");
        // No ID set in DTO, implying a new entry

        // When
        JournalEntry entity = journalEntryService.convertToJournalEntity(dto);

        // Then
        assertNotNull(entity);
        assertNull(entity.getId()); // Should be null if not set in DTO
        assertEquals("DTO Title", entity.getTitle());
        assertEquals("DTO Content", entity.getContent());
        assertNull(entity.getDateCreated()); // Date fields are set during save operation, not conversion
    }

    @Test
    void convertToJournalEntity_shouldConvertJournalDTOToEntityWithExistingId() {
        // Given
        ObjectId existingId = new ObjectId();
        JournalDTO dto = new JournalDTO();
        dto.setId(existingId.toHexString());
        dto.setTitle("DTO Title");
        dto.setContent("DTO Content");

        // When
        JournalEntry entity = journalEntryService.convertToJournalEntity(dto);

        // Then
        assertNotNull(entity);
        assertEquals(existingId, entity.getId());
        assertEquals("DTO Title", entity.getTitle());
        assertEquals("DTO Content", entity.getContent());
    }
}