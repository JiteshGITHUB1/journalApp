package com.project.space.journalApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.entity.JournalEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.http.converter.json.GsonFactoryBean;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/_journal")
public class JournalEntryController {


    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryController.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();
    public Map<String, JournalEntry> journalEntries = new HashMap<>();

    @GetMapping
    public List<JournalEntry> getAll() {
        LOGGER.info("Fetching all journal entries : {} {} {}",
                journalEntries.isEmpty() ? "No entries found" : "Found" , journalEntries.size() , "entries in the journal");
        return new ArrayList<>(journalEntries.values());
    }

    @PostMapping
    public boolean createEntry(@RequestBody JournalEntry entry) throws JsonProcessingException {
        journalEntries.put(entry.getId().toString(), entry);
        LOGGER.info("Created journal entry with ID: {}", objectMapper.writeValueAsString(journalEntries));
        return true;
    }

    @GetMapping("/id/{myId}")
    public JournalEntry getJournalEntryById(@PathVariable Long myId) {
        return journalEntries.get(myId);
    }

    @DeleteMapping("/id/{myId}")
    public JournalEntry deleteJournalEntryById(@PathVariable Long myId) {
        return journalEntries.remove(myId);
    }

    @PutMapping("/id/{myId}")
    public JournalEntry updateJournalEntryById(@PathVariable Long myId, @RequestBody JournalEntry entry) {
        return journalEntries.put(myId.toString(), entry);
    }

}