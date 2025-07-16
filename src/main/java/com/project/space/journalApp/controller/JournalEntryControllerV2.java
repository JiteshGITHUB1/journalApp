package com.project.space.journalApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/journal")
public class JournalEntryControllerV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryControllerV2.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();


    private final JournalEntryService journalEntryService;

    private final UserService userService;

    public JournalEntryControllerV2(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<JournalEntry>> getAllJournalEntriesOfUser() {
        LOGGER.info("Fetching all journal entries for the authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUserName(authentication.getName());
        List<JournalEntry> allEntries = user.getJournalEntries();
        if (allEntries != null && !allEntries.isEmpty()) {
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping({"/create"})
    public ResponseEntity<JournalEntry> createEntry(@RequestBody JournalEntry entry) throws JsonProcessingException {
        LOGGER.info("Creating journal entry: {}", objectMapper.writeValueAsString(entry));
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            entry.setDateCreated(java.time.LocalDateTime.now());
            journalEntryService.saveEntry(entry, authentication.getName());
            return new ResponseEntity<>(entry, HttpStatus.CREATED);
        }
        catch (Exception e) {
            LOGGER.error("Error creating journal entry: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable ObjectId myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        User user = userService.findByUserName(name);
        List<JournalEntry> collect = user.getJournalEntries().stream()
                .filter(entry -> entry.getId().equals(myId))
                .collect(Collectors.toList());
        if(!collect.isEmpty()){
            Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
            if (journalEntry.isPresent()) {
                return new ResponseEntity<>( journalEntry.get(), HttpStatus.OK);
            }
        }
        LOGGER.warn("Journal entry with ID {} not found", myId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> deleteJournalEntryById(@PathVariable ObjectId myId) {
        Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
        if (journalEntry.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean removed = journalEntryService.deleteById(myId, authentication.getName());
            if (removed) {
                LOGGER.info("Deleted journal entry with ID {}", myId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }else {
                LOGGER.warn("Failed to delete journal entry with ID {}", myId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            LOGGER.warn("Journal entry with ID {} not found for deletion", myId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> updateJournalEntryById(@PathVariable ObjectId myId,
                                                               @RequestBody JournalEntry newEntry) {
        LOGGER.info("Updating journal entry with ID {}: {}", myId, newEntry);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUserName(authentication.getName());
        List<JournalEntry> collect = user.getJournalEntries().stream()
                .filter(entry -> entry.getId().equals(myId))
                .collect(Collectors.toList());
        if(!collect.isEmpty()){
            Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
            if (journalEntry.isPresent()) {
                    JournalEntry oldEntry = journalEntry.get();
                    oldEntry.setTitle(newEntry.getTitle() != null && !newEntry.getTitle().equals("") ? newEntry.getTitle() : oldEntry.getTitle());
                    oldEntry.setContent(newEntry.getContent() != null && !newEntry.getContent().equals("") ? newEntry.getContent() : oldEntry.getContent());
                    journalEntryService.saveEntry(oldEntry);
                    return new ResponseEntity<>(oldEntry, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}