package org.example.collaborativecodeeditor.model.user.service;

import org.example.collaborativecodeeditor.model.user.User;
import org.example.collaborativecodeeditor.model.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User getUser(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        return optionalUser.get();
    }

}
