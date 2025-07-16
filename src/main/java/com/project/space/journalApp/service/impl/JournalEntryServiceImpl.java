package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.repository.JournalEntryRepository;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void saveEntry(JournalEntry journalEntry, String userName){
        log.info("Saving journal entry: {}", journalEntry);
        try {
            if (journalEntry != null) {
                User user = userService.findByUserName(userName);
                journalEntry.setDateModified(java.time.LocalDateTime.now());
                JournalEntry saved = journalEntryRepository.save(journalEntry);
                user.getJournalEntries().add(saved);
                userService.saveEntry(user);
            } else {
                throw new IllegalArgumentException("Journal entry cannot be null");
            }
        }catch (Exception e) {
            log.error("Error saving journal entry: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to save journal entry", e);
        }
    }

    @Override
    public void saveEntry(JournalEntry journalEntry){
        try {
            log.info("Saving journal entry: {}", journalEntry);
            if (journalEntry != null) {
                journalEntryRepository.save(journalEntry);
            } else {
                log.info("Journal entry cannot be null");
                throw new IllegalArgumentException("Journal entry cannot be null");
            }
        } catch (Exception e) {
            log.error("Error saving journal entry: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to save journal entry", e);
        }
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
            User user = userService.findByUserName(userName);
            removed = user.getJournalEntries().removeIf(entry -> entry.getId().equals(id));
            if (removed) {
                userService.saveEntry(user);
                journalEntryRepository.deleteById(id);
            }
            return removed;
        } catch (Exception e) {
            log.error("Error deleting journal entry with ID {}: {}", id, e.getMessage());
            throw new IllegalArgumentException("Failed to delete journal entry", e);
        }
    }
}
