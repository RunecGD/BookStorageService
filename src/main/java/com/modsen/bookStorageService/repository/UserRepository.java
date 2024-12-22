package com.modsen.bookStorageService.repository;

import com.modsen.bookStorageService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
