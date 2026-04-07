package com.ey.interview.service;

import com.ey.interview.config.ValidationProperties;
import com.ey.interview.dto.CreateUserRequest;
import com.ey.interview.dto.PhoneRequest;
import com.ey.interview.dto.UserResponse;
import com.ey.interview.entity.Phone;
import com.ey.interview.entity.User;
import com.ey.interview.exception.EmailAlreadyExistsException;
import com.ey.interview.exception.InvalidEmailException;
import com.ey.interview.exception.InvalidPasswordException;
import com.ey.interview.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ValidationProperties validationProperties;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (!request.email().matches(validationProperties.getEmailRegex())) {
            throw new InvalidEmailException();
        }
        if (!request.password().matches(validationProperties.getPasswordRegex())) {
            throw new InvalidPasswordException();
        }
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new EmailAlreadyExistsException();
        }

        var user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        var phones = request.phones().stream()
                .map(p -> Phone.builder()
                        .user(user)
                        .number(p.number())
                        .citycode(p.citycode())
                        .contrycode(p.contrycode())
                        .build())
                .toList();

        user.getPhones().addAll(phones);

        var saved = userRepository.save(user);

        var token = jwtService.generateToken(saved.getId(), saved.getEmail());
        saved.setToken(token);
        saved = userRepository.save(saved);

        return toResponse(saved, request.phones());
    }

    private UserResponse toResponse(User user, List<PhoneRequest> phones) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                phones,
                user.getCreated(),
                user.getModified(),
                user.getLastLogin(),
                user.getToken(),
                user.isActive()
        );
    }
}
