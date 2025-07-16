package com.project.space.journalApp.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.service.UserService;
import com.project.space.journalApp.service.impl.JournalEntryServiceImpl;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    /*@GetMapping("/all")
    public ResponseEntity<List<User>> getAllUsers() {
        return new ResponseEntity<>(userService.getAllUser(), HttpStatus.OK);
    }*/


    @GetMapping("/get-user")
    public ResponseEntity<User> getUserById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> user = Optional.ofNullable(userService.findByUserName(authentication.getName()));
        return new ResponseEntity<>(user.get(), HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUserById() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userService.deleteByUserName(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        User existingUser = userService.findByUserName(userName);
        existingUser.setUsername(user.getUsername());
        existingUser.setPassword(user.getPassword());
        userService.saveNewUser(existingUser);
        return new ResponseEntity<>(existingUser, HttpStatus.OK);
    }

}