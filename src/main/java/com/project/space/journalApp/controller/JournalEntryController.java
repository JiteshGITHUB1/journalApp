package com.project.space.journalApp.controller;

import com.project.space.journalApp.dto.JournalDTO;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/_journal")
@Slf4j
public class JournalEntryController {
    protected static final Map<String, JournalDTO> journalEntries = new HashMap<>();

    @GetMapping
    public List<JournalDTO> getAll() {
        log.info("Fetching all journal entries : {} {} {}",
                journalEntries.isEmpty() ? "No entries found" : "Found" , journalEntries.size() , "entries in the journal");
        return new ArrayList<>(journalEntries.values());
    }

    @PostMapping
    public boolean createEntry(@RequestBody JournalDTO journalDTO) {
        journalEntries.put(journalDTO.getId(), journalDTO);
        log.info("Created journal entry with ID: {}", journalEntries);
        return true;
    }

    @GetMapping("/id/{myId}")
    public JournalDTO getJournalEntryById(@PathVariable Long myId) {
        return journalEntries.get(myId);
    }

    @DeleteMapping("/id/{myId}")
    public JournalDTO deleteJournalEntryById(@PathVariable Long myId) {
        return journalEntries.remove(myId);
    }

    @PutMapping("/id/{myId}")
    public JournalDTO updateJournalEntryById(@PathVariable Long myId, @RequestBody JournalDTO journalDTO) {
        return journalEntries.put(myId.toString(), journalDTO);
    }

}