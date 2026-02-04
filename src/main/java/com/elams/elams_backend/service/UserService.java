package com.elams.elams_backend.service;

import com.elams.elams_backend.entity.User;

public interface UserService {

    User createUser(User user);

    User getByEmail(String email);
}
