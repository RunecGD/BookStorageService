package com.modsen.bookStorageService;

import com.modsen.bookStorageService.dto.UserDTO;
import com.modsen.bookStorageService.models.User;
import com.modsen.bookStorageService.repository.UserRepository;
import com.modsen.bookStorageService.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegister() {
        // Arrange
        UserDTO userDTO = new UserDTO("testuser", "password123");

        User user = new User();
        user.setUsername(userDTO.username());
        user.setPassword(passwordEncoder.encode(userDTO.password()));

        // Мокаем поведение
        when(passwordEncoder.encode(userDTO.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User registeredUser = userService.register(userDTO);

        // Assert
        assertNotNull(registeredUser);
        assertEquals("testuser", registeredUser.getUsername());
        assertEquals("encodedPassword", registeredUser.getPassword());
        verify(passwordEncoder).encode(userDTO.password());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testFindByUsername() {
        // Arrange
        String username = "testuser";
        User user = new User();
        user.setUsername(username);
        user.setPassword("encodedPassword");

        when(userRepository.findByUsername(username)).thenReturn(user);

        // Act
        User foundUser = userService.findByUsername(username);

        // Assert
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
        verify(userRepository).findByUsername(username);
    }

    @Test
    void testFindByUsername_NotFound() {
        // Arrange
        String username = "unknownuser";
        when(userRepository.findByUsername(username)).thenReturn(null);

        // Act
        User foundUser = userService.findByUsername(username);

        // Assert
        assertNull(foundUser);
        verify(userRepository).findByUsername(username);
    }
}