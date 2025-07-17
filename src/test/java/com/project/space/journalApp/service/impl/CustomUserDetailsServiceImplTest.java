package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@Disabled
@SpringBootTest
class CustomUserDetailsServiceImplTest {

    @InjectMocks
//    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Mock
//    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Initialize mocks if necessary
        MockitoAnnotations.openMocks(this); // Uncomment if using Mockito annotations

    }

    @Test
    void loadUserByUsername() {
        when(userRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(UserEntity.builder().username("ram").password("%YTRFTYRFYTHsmdvmov").roles(List.of("USER")).build());
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("ram");
        assertNotNull(userDetails, "UserDetails should not be null");
    }
}