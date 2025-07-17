package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceImplTest {

    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUserEntity = new UserEntity();
        testUserEntity.setUsername("ram");
        testUserEntity.setPassword("encodedPassword"); // Assuming it's encoded
        testUserEntity.setRoles(Collections.singletonList("USER")); // Or whatever roles
        testUserEntity.setJournalEntries(Collections.emptyList()); // Initialize to avoid NPEs later
    }

    @Test
    void loadUserByUsername_UserFound() {
        // Mock the behavior of userRepository.findByUsername
        Mockito.when(userRepository.findByUsername("ram")).thenReturn(testUserEntity);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("ram");

        assertNotNull(userDetails);
        assertEquals("ram", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        // Add more assertions for roles etc.
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername("ram"); // Verify interaction
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        // Mock the behavior for a user not found
        Mockito.when(userRepository.findByUsername("nonexistentUser")).thenReturn(null);

        // Assert that UsernameNotFoundException is thrown
        assertThrows(UsernameNotFoundException.class, () -> {
            customUserDetailsService.loadUserByUsername("nonexistentUser");
        });
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername("nonexistentUser");
    }
}