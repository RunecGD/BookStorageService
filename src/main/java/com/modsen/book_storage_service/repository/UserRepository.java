package com.modsen.book_storage_service.repository;

import com.modsen.book_storage_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
