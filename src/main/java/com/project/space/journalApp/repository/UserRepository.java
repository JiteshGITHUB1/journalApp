package com.project.space.journalApp.repository;

import com.project.space.journalApp.entity.UserEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, ObjectId>{

    @Query("{ 'username' : ?0 }")
    UserEntity findByUserName(String userName);

    @Query("{ 'username' : ?0 }")
    void deleteByUserName(String userName);
}
