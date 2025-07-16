package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.User;
import com.project.space.journalApp.repository.UserRepository;
import com.project.space.journalApp.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


import static org.junit.jupiter.api.Assertions.*;

@Disabled
@SpringBootTest
class UserServiceImplTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Initialize any necessary data or mocks before each test
        // For example, you can create a user in the database if needed
        // MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test if necessary
//        userRepository.deleteAll(); // This will clear the database after each test
    }

    @Test
     void testFindByUserName() {
        User user = userRepository.findByUserName("jit");
        assertFalse(user.getJournalEntries().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "jit", "admin"
    })
     void testFindByUserNameByValue(String name) {
        assertNotNull(userRepository.findByUserName(name), "failed for username: " + name + "");
    }


    @ParameterizedTest
    @ArgumentsSource(UserArgumentsProvider.class)
     void testSaveNewUserByArg(User user){
        assertTrue(userService.saveNewUser(user));
    }
}