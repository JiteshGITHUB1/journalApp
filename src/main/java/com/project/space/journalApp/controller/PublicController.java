package com.project.space.journalApp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.dto.UserDTO;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Slf4j
public class PublicController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final UserService userService;

    public PublicController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        return "OK";
    }

    @PostMapping("/create-user")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        log.info("Creating new user: {}", userDTO);
        UserDTO responseDTO = userService.saveNewUser(userDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
}
