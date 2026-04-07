package com.ey.interview.service;

import com.ey.interview.dto.CreateUserRequest;
import com.ey.interview.dto.UserResponse;

public interface UserService {
    UserResponse createUser(CreateUserRequest request);
}
