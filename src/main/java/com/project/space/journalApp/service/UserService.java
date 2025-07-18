package com.project.space.journalApp.service;

import com.project.space.journalApp.dto.UserDTO;
import com.project.space.journalApp.entity.UserEntity;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    UserDTO saveNewUser(UserDTO userDTO);

    UserDTO saveAdminUser(UserDTO userDTO);

    List<UserDTO> getAllUser();

    UserEntity saveEntry(UserEntity userEntity);

    UserEntity findByUserName(String userName);

    void deleteById(ObjectId id);

    void deleteByUserName(String userName);

    // Helper method to convert UserEntity to UserDTO
    UserDTO convertToUserDTO(UserEntity userEntity);

    // Helper method to convert UserDTO to UserEntity
    UserEntity convertToUserEntity(UserDTO userDTO);
}
