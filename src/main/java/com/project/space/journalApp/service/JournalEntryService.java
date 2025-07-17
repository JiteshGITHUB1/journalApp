package com.project.space.journalApp.service;

import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface JournalEntryService {

    List<JournalEntry> getAllEntries();

    @Transactional
    JournalDTO saveEntry(JournalDTO journalDTO, String userName);

    void saveEntry(JournalEntry journalEntry);

    Optional<JournalEntry> findById(ObjectId id);

    boolean deleteById(ObjectId id, String userName);

    JournalDTO convertToJournalDTO(JournalEntry journalEntry);

    JournalEntry convertToJournalEntity(JournalDTO journalDTO);
}
