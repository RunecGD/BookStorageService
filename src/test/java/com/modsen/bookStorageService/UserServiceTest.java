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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister() {

        UserDTO userDTO = new UserDTO("testUser", "password");
        User user = new User();
        user.setUsername(userDTO.username());

        String encodedPassword = "encodedPassword";
        user.setPassword(encodedPassword);

        when(passwordEncoder.encode(userDTO.password())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.register(userDTO);

        assertEquals("testUser", result.getUsername());
        assertEquals(encodedPassword, result.getPassword());
    }

    @Test
    public void testFindByUsername_UserFound() {
        String username = "testUser";
        User user = new User();
        user.setUsername(username);

        when(userRepository.findByUsername(username)).thenReturn(user);

        User result = userService.findByUsername(username);

        assertEquals(username, result.getUsername());
    }

    @Test
    public void testFindByUsername_UserNotFound() {
        String username = "nonExistentUser";
        when(userRepository.findByUsername(username)).thenReturn(null);

        User result = userService.findByUsername(username);

        assertEquals(null, result);
    }
}