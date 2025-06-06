package org.example.collaborativecodeeditor.model.user.repository;

import org.example.collaborativecodeeditor.model.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
}