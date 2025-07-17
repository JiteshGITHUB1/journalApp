package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.dto.JournalDTO;
import com.project.space.journalApp.dto.UserDTO;
import com.project.space.journalApp.entity.JournalEntry;
import com.project.space.journalApp.entity.UserEntity;
import com.project.space.journalApp.repository.UserRepository;
import com.project.space.journalApp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean saveEntry(UserEntity userEntity){
        try {
            log.info("Saving User Entry: {}", userEntity);
            if (userEntity != null) {
                userRepository.save(userEntity);
            } else {
                throw new IllegalArgumentException("User entry cannot be null");
            }
        } catch (Exception e) {
            log.error("Error saving user entry {} : {}", userEntity.getUsername(), e.getMessage());
            return false;
        }
      return true;
    }

    @Override
    public UserDTO saveNewUser(UserDTO userDTO) {
        UserDTO responseDTO;
        try{
            log.info("Saving New User Entry: {}", userDTO);
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername(userDTO.getUsername());
            userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            userEntity.setRoles(List.of("USER")); // Default role for new users
            UserEntity saved = userRepository.save(userEntity);
            responseDTO = convertToUserDTO(saved);
        } catch (Exception e) {
            log.error("Error saving new user: {}", e.getMessage());
            throw new IllegalArgumentException("Failed to save new user", e);
        }
        return responseDTO;
    }

    @Override
    public UserDTO saveAdminUser(UserDTO userDTO) {
        log.info("Saving Admin User Entry: {}", userDTO);
        userDTO.setRoles(List.of("ADMIN","USER")); // Default role for new users
        UserEntity userEntity = convertToUserEntity(userDTO);
        UserEntity savedEntity = userRepository.save(userEntity);
        return convertToUserDTO(savedEntity);
    }

    @Override
    public List<UserDTO> getAllUser() {
        log.info("Fetching all users");
        if (userRepository.count() == 0) {
            log.warn("No users found in the repository");
        }
        List<UserDTO> userDTOList = new ArrayList<>();
        for (UserEntity userEntity : userRepository.findAll()) {
            userDTOList.add(convertToUserDTO(userEntity));
        }
        return userDTOList;
    }

    @Override
    public UserEntity findByUserName(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteByUserName(String userName) {
        log.info("Deleting User with username: {}", userName);
        userRepository.deleteByUsername(userName);
    }


    // Helper method to convert UserEntity to UserDTO
    @Override
    public UserDTO convertToUserDTO(UserEntity userEntity) {
        UserDTO userDTO = new UserDTO();
        if (userEntity.getId() != null) {
            userDTO.setId(userEntity.getId().toString());
        }
        userDTO.setUsername(userEntity.getUsername());
        userDTO.setPassword(userEntity.getPassword());
        userDTO.setRoles(userEntity.getRoles());
        // Do NOT set password or journalEntries here for security/simplicity
        List<JournalDTO> journalDTOList = new ArrayList<>();
        for (JournalEntry journalEntry : userEntity.getJournalEntries()) {
            JournalDTO journalDTO = new JournalDTO();
            journalDTO.setId(journalEntry.getId() != null ? journalEntry.getId().toString() : null);
            journalDTO.setTitle(journalEntry.getTitle());
            journalDTO.setContent(journalEntry.getContent());
            journalDTO.setDateCreated(journalEntry.getDateCreated());
            journalDTO.setDateModified(journalEntry.getDateModified());
            journalDTOList.add(journalDTO);
        }
        userDTO.getJournalEntries().addAll(journalDTOList);
        return userDTO;
    }

    // Helper method to convert UserDTO to UserEntity
    @Override
    public UserEntity convertToUserEntity(UserDTO userDTO) {
        return UserEntity.builder()
                .username(userDTO.getUsername())
                /* Password must be handled securely, typically hashed and not passed directly in a DTO like this
                 For admin creation, you'd typically have a raw password in a *separate* request DTO
                 or handle password setting in the service layer using a default or generated password.
                 For now, assuming password comes from somewhere else or is set in service, or it's a simple example.*/
                .password(passwordEncoder.encode(userDTO.getPassword())) // IMPORTANT: This should not be userDTO.getPassword() if it's not hashed!
                .roles(userDTO.getRoles())
                .build();
    }

}
