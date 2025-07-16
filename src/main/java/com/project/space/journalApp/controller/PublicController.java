package com.project.space.journalApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.service.JournalEntryService;
import com.project.space.journalApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private static final Logger LOGGER = LoggerFactory.getLogger(JournalEntryControllerV2.class);

    @Autowired
    private UserService userService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping("/create-user")
    public ResponseEntity<User> createUser(@RequestBody User user){
        userService.saveNewUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
