package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.repository.JournalEntryRepository;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class JournalEntryServiceImpl implements JournalEntryService {


    private final JournalEntryRepository journalEntryRepository;

    private final UserService userService;

    public JournalEntryServiceImpl(JournalEntryRepository journalEntryRepository, UserService userService) {
        this.journalEntryRepository = journalEntryRepository;
        this.userService = userService;
    }

    @Override
    public List<JournalEntry> getAllEntries() {
        return journalEntryRepository.findAll();
    }

    @Override
    @Transactional
    public JournalDTO saveEntry(JournalDTO journalDTO, String userName){
        log.info("Saving journal entry: {}", journalDTO);
        if (journalDTO == null) {
            throw new IllegalArgumentException("Journal entry cannot be null");
        }

        JournalDTO responseDTO;
        try {
            UserEntity userEntity = userService.findByUserName(userName);
            if (userEntity == null) {
                throw new IllegalArgumentException("User not found for userName: " + userName);
            }

            // Set modified date and convert DTO to Entity
            journalDTO.setDateModified(LocalDateTime.now()); // Set current modified date
            JournalEntry journalEntry = convertToJournalEntity(journalDTO);

            // Set dateCreated only if it's a new entry
            if (journalEntry.getId() == null) {
                journalEntry.setDateCreated(LocalDateTime.now()); // Set creation date for new entries
            } else {
                // If it's an update, preserve the original creation date if possible
                // You might need to fetch the existing entry to get its creation date
                Optional<JournalEntry> existingEntry = journalEntryRepository.findById(journalEntry.getId());
                existingEntry.ifPresent(entry -> journalEntry.setDateCreated(entry.getDateCreated()));
            }

            JournalEntry saved = journalEntryRepository.save(journalEntry); // Save to the database

            // --- CRITICAL FIX: Update or Add to user's journalEntries list ---
            boolean foundAndReplaced = false;
            if (saved.getId() != null) { // Only attempt to replace if the saved entry has an ID
                for (int i = 0; i < userEntity.getJournalEntries().size(); i++) {
                    // Use .equals() because JournalEntry has @EqualsAndHashCode(of = "id")
                    if (userEntity.getJournalEntries().get(i).equals(saved)) {
                        userEntity.getJournalEntries().set(i, saved); // Replace the old entry with the updated one
                        foundAndReplaced = true;
                        break;
                    }
                }
            }
            if (!foundAndReplaced) {
                userEntity.getJournalEntries().add(saved); // If no existing entry was found (new entry) or ID was null
            }
            // --- END CRITICAL FIX ---

            userService.saveEntry(userEntity); // Save the updated user entity (with modified journalEntries list)
            responseDTO = convertToJournalDTO(saved);

        }catch (Exception e) {
            log.error("Error saving journal entry: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to save journal entry", e);
        }
        return responseDTO;
    }

    @Override
    public JournalEntry saveEntry(JournalEntry journalEntry){
        JournalEntry saved;
        try {
            log.info("Saving journal entry: {}", journalEntry);
            if (journalEntry == null) { // Direct check before try-catch
                log.info("Journal entry cannot be null");
                throw new IllegalArgumentException("Journal entry cannot be null");
            }
            saved = journalEntryRepository.save(journalEntry);
            log.info("Journal entry saved successfully with ID: {}", saved.getId());
        } catch (Exception e) {
            log.error("Error saving journal entry: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to save journal entry", e);
        }
        return saved;
    }

    @Override
    public Optional<JournalEntry> findById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Override
    @Transactional
    public boolean deleteById(ObjectId id, String userName) {
        boolean removed = false;
        try {
            UserEntity userEntity = userService.findByUserName(userName);
            if (userEntity == null) { // Add check for null user entity
                throw new IllegalArgumentException("User not found for userName: " + userName);
            }
            removed = userEntity.getJournalEntries().removeIf(entry -> entry.getId().equals(id));
            if (removed) {
                userService.saveEntry(userEntity);
                journalEntryRepository.deleteById(id);
            }
            return removed;
        } catch (Exception e) {
            log.error("Error deleting journal entry with ID {}: {}", id, e.getMessage());
            throw new IllegalArgumentException("Failed to delete journal entry", e);
        }
    }

    @Override
    public JournalDTO convertToJournalDTO(JournalEntry journalEntry){
        if (journalEntry == null) {
            return null;
        }
        JournalDTO journalDTO = new JournalDTO();
        journalDTO.setId(String.valueOf(journalEntry.getId()));
        journalDTO.setTitle(journalEntry.getTitle());
        journalDTO.setContent(journalEntry.getContent());
        journalDTO.setDateCreated(journalEntry.getDateCreated());
        journalDTO.setDateModified(journalEntry.getDateModified());
        return journalDTO;
    }

    @Override
    public JournalEntry convertToJournalEntity(JournalDTO journalDTO) {
        JournalEntry journalEntry = new JournalEntry();
        journalEntry.setTitle(journalDTO.getTitle());
        journalEntry.setContent(journalDTO.getContent());
        if (journalDTO.getId() != null) {
            journalEntry.setId(new ObjectId(journalDTO.getId()));
        }
        return journalEntry;
    }
}
