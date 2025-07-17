package com.project.space.journalApp.service.impl;

import com.project.space.journalApp.entity.UserEntity;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;

import java.util.stream.Stream;

public class UserArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
        return Stream.of(
                Arguments.of(UserEntity.builder().username("user1").password("").build()),
                Arguments.of(UserEntity.builder().username("user2").password("user2").build())
        );
    }
}
