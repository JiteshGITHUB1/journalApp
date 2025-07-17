package com.project.space.journalApp.controller;

import com.project.space.journalApp.dto.UserDTO;
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
    public ResponseEntity<List<UserDTO>> getAllUsers(){
        List<UserDTO> userDTO = userService.getAllUser();
        if(userDTO != null && !userDTO.isEmpty()){
            return new ResponseEntity<>(userDTO, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/create-admin-user")
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO){
        UserDTO responseDTO = userService.saveAdminUser(userDTO);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }
}
