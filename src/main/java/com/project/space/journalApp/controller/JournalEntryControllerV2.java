package com.project.space.journalApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/journal")
@Slf4j
public class JournalEntryControllerV2 {

    private final JournalEntryService journalEntryService;

    private final UserService userService;

    public JournalEntryControllerV2(JournalEntryService journalEntryService, UserService userService) {
        this.journalEntryService = journalEntryService;
        this.userService = userService;
    }


    @GetMapping("/all")
    public ResponseEntity<List<JournalEntry>> getAllJournalEntriesOfUser() {
        log.info("Fetching all journal entries for the authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findByUserName(authentication.getName());
        List<JournalEntry> allEntries = userEntity.getJournalEntries();
        if (allEntries != null && !allEntries.isEmpty()) {
            return new ResponseEntity<>(allEntries, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping({"/create"})
    public ResponseEntity<JournalDTO> createEntry(@RequestBody JournalDTO journalDTO) throws JsonProcessingException {
        log.info("Creating journal entry: {}", journalDTO);
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            journalDTO.setDateCreated(java.time.LocalDateTime.now());
            JournalDTO responseDTO = journalEntryService.saveEntry(journalDTO, authentication.getName());
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("Error creating journal entry: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> getJournalEntryById(@PathVariable ObjectId myId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        UserEntity userEntity = userService.findByUserName(name);
        List<JournalEntry> collect = userEntity.getJournalEntries().stream()
                .filter(entry -> entry.getId().equals(myId))
                .toList();
        if (!collect.isEmpty()) {
            Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
            if (journalEntry.isPresent()) {
                return new ResponseEntity<>(journalEntry.get(), HttpStatus.OK);
            }
        }
        log.warn("Journal entry with ID {} not found", myId);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/id/{myId}")
    public ResponseEntity<JournalEntry> deleteJournalEntryById(@PathVariable ObjectId myId) {
        Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
        if (journalEntry.isPresent()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean removed = journalEntryService.deleteById(myId, authentication.getName());
            if (removed) {
                log.info("Deleted journal entry with ID {}", myId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            } else {
                log.warn("Failed to delete journal entry with ID {}", myId);
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } else {
            log.warn("Journal entry with ID {} not found for deletion", myId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/id/{myId}")
    public ResponseEntity<JournalDTO> updateJournalEntryById(@PathVariable ObjectId myId,
                                                             @RequestBody JournalDTO journalDTO) {
        log.info("Updating journal entry with ID {}: {}", myId, journalDTO);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserEntity userEntity = userService.findByUserName(authentication.getName());
        List<JournalEntry> collect = userEntity.getJournalEntries().stream()
                .filter(entry -> entry.getId().equals(myId))
                .toList();
        if (!collect.isEmpty()) {
            Optional<JournalEntry> journalEntry = journalEntryService.findById(myId);
            if (journalEntry.isPresent()) {
                JournalEntry oldEntry = journalEntry.get();
                oldEntry.setTitle(journalDTO.getTitle() != null && !journalDTO.getTitle().equals("") ? journalDTO.getTitle() : oldEntry.getTitle());
                oldEntry.setContent(journalDTO.getContent() != null && !journalDTO.getContent().equals("") ? journalDTO.getContent() : oldEntry.getContent());
                journalEntryService.saveEntry(oldEntry);
                JournalDTO responseDTO = journalEntryService.convertToJournalDTO(oldEntry);
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}