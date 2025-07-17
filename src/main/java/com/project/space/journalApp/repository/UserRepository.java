package com.project.space.journalApp.repository;

import com.project.space.journalApp.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, ObjectId>{

    UserEntity findByUsername(String username);

    Long deleteByUsername(String username);
}
