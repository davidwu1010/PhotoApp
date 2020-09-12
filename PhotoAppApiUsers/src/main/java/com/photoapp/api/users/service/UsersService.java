package com.photoapp.api.users.service;

import com.photoapp.api.users.shared.UserDto;

public interface UsersService {
    UserDto createUser(UserDto userDetails);
}
