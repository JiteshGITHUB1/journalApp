package com.project.space.journalApp.controller;

import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/all-users")
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> allUser = userService.getAllUser();
        if(allUser != null && !allUser.isEmpty()){
            return new ResponseEntity<>(allUser, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin-user")
    public ResponseEntity<User> createUser(@RequestBody User user){
        userService.saveAdminUser(user);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
