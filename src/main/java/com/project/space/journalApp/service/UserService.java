package com.project.space.journalApp.service;

import com.project.space.journalApp.entity.User;
import org.bson.types.ObjectId;

import java.util.List;

public interface UserService {
    void saveAdminUser(User user);

    List<User> getAllUser();

    boolean saveEntry(User user);

    boolean saveNewUser(User user);

    User findByUserName(String userName);

    void deleteById(ObjectId id);

    void deleteByUserName(String userName);
}
