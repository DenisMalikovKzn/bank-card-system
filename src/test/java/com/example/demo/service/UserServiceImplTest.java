package com.example.demo.service;

import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.BankCard;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.ERole;
import com.example.demo.exceptions.EmailAlreadyExistsException;
import com.example.demo.exceptions.UserHasCardsException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName(ERole.ROLE_USER);

        adminRole = new Role();
        adminRole.setName(ERole.ROLE_ADMIN);

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setRoles(Set.of(userRole));
    }

    @Test
    void getAllUsers_WithEmailFilter_ReturnsFilteredPage() {
        // Given
        String email = "test";
        PageRequest pageable = PageRequest.of(0, 10);
        List<User> users = Collections.singletonList(testUser);

        when(userRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(users, pageable, users.size()));

        // When
        Page<UserResponse> result = userService.getAllUsers(email, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals(testUser.getEmail(), result.getContent().get(0).email());
        verify(userRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void createUser_WithNewEmail_ReturnsCreatedUser() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
                "test@example.com",
                "password",
                Set.of("ROLE_USER")
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.createUser(request);

        // Then
        assertNotNull(response);
        assertEquals(request.email(), response.email());
        assertEquals(1, response.roles().size());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WithExistingEmail_ThrowsException() {
        // Given
        UserCreateRequest request = new UserCreateRequest(
                "existing@example.com",
                "password",
                Set.of("ROLE_USER")
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // When & Then
        assertThrows(EmailAlreadyExistsException.class, () -> userService.createUser(request));
    }

    @Test
    void updateUserRoles_WithValidData_ReturnsUpdatedUser() {
        // Given
        Long userId = 1L;
        Set<String> newRoles = Set.of("ROLE_ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(ERole.ROLE_ADMIN)).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        UserResponse response = userService.updateUserRoles(userId, newRoles);

        // Then
        assertEquals(1, response.roles().size());
        assertTrue(response.roles().contains("ROLE_ADMIN"));
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserRoles_WithNonExistingUser_ThrowsException() {
        // Given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class,
                () -> userService.updateUserRoles(userId, Set.of("ROLE_USER")));
    }

    @Test
    void deleteUser_WithoutCards_DeletesSuccessfully() {
        // Given
        Long userId = 1L;
        testUser.setCards(Collections.emptyList());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).delete(testUser);
    }

    @Test
    void deleteUser_WithCards_ThrowsException() {
        // Given
        Long userId = 1L;
        BankCard card = new BankCard();
        testUser.addCard(card);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        // When & Then
        assertThrows(UserHasCardsException.class, () -> userService.deleteUser(userId));
        verify(userRepository, never()).delete((User) any());
    }

    @Test
    void mapToResponse_ReturnsCorrectResponse() {
        // Given
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRoles(Set.of(userRole, adminRole));

        // When
        UserResponse response = userService.mapToResponse(user);

        // Then
        assertEquals(user.getId(), response.id());
        assertEquals(user.getEmail(), response.email());
        assertEquals(2, response.roles().size());
        assertTrue(response.roles().containsAll(Set.of("ROLE_USER", "ROLE_ADMIN")));
    }
}