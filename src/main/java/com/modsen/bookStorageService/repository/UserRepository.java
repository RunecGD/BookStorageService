package com.modsen.bookStorageService.repository;

import com.modsen.bookStorageService.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
