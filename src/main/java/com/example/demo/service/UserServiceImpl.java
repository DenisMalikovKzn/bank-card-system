package com.example.demo.service;

import lombok.RequiredArgsConstructor;
import com.example.demo.dto.UserCreateRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.enums.ERole;
import com.example.demo.exceptions.EmailAlreadyExistsException;
import com.example.demo.exceptions.UserHasCardsException;
import com.example.demo.exceptions.UserNotFoundException;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.management.relation.RoleNotFoundException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public Page<UserResponse> getAllUsers(String email, Pageable pageable) {
        Specification<User> spec = Specification.where(null);

        if (email != null && !email.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%"));
        }

        return userRepository.findAll(spec, pageable)
                .map(this::mapToResponse);
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        Set<Role> roles = request.roles().stream()
                .map(ERole::valueOf)
                .map(role -> {
                    try {
                        return roleRepository.findByName(role)
                                .orElseThrow(() -> new RoleNotFoundException("Role not found: " + role));
                    } catch (RoleNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return mapToResponse(userRepository.save(user));
    }

    public UserResponse updateUserRoles(Long userId, Set<String> newRoles) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Set<Role> roles = newRoles.stream()
                .map(roleName -> roleRepository.findByName(ERole.valueOf(roleName)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return mapToResponse(userRepository.save(user));
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!user.getCards().isEmpty()) {
            throw new UserHasCardsException("Cannot delete user with active cards");
        }

        userRepository.delete(user);
    }

    UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet())
        );
    }
}
