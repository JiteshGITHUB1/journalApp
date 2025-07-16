package com.project.space.journalApp.repository;

import com.project.space.journalApp.entity.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId>{

    @Query("{ 'username' : ?0 }")
    User findByUserName(String userName);

    @Query("{ 'username' : ?0 }")
    void deleteByUserName(String userName);
}
