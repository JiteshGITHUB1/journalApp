package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.repository.UserRepository;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public boolean saveEntry(User user){
        try {
            log.info("Saving User Entry: {}", user);
            if (user != null) {
                userRepository.save(user);
            } else {
                throw new IllegalArgumentException("User entry cannot be null");
            }
        } catch (Exception e) {
            log.error("Error saving user entry {} : {}", user.getUsername(), e.getMessage());
            return false;
        }
      return true;
    }

    @Override
    public boolean saveNewUser(User user) {
        try{
            log.info("Saving New User Entry: {}", user);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(List.of("USER")); // Default role for new users
            userRepository.save(user);
        } catch (Exception e) {
            log.error("Error saving new user: {}", e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public void saveAdminUser(User user) {
        log.info("Saving Admin User Entry: {}", user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(List.of("ADMIN","USER")); // Default role for new users
        userRepository.save(user);
    }

    @Override
    public List<User> getAllUser() {
        log.info("Fetching all users");
        if (userRepository.count() == 0) {
            log.warn("No users found in the repository");
        }
        return userRepository.findAll();
    }

    @Override
    public User findByUserName(String username) {
        return userRepository.findByUserName(username);
    }

    @Override
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteByUserName(String userName) {
        log.info("Deleting User with username: {}", userName);
        userRepository.deleteByUserName(userName);
    }

}
