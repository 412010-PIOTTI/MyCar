package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.enums.Role;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.infrastructure.security.JwtService;
import ar.edu.utn.frc.mycar.web.dto.request.LoginRequest;
import ar.edu.utn.frc.mycar.web.dto.request.RegisterRequest;
import ar.edu.utn.frc.mycar.web.dto.response.AuthResponse;
import ar.edu.utn.frc.mycar.web.dto.response.LoginResponse;
import ar.edu.utn.frc.mycar.web.exception.EmailAlreadyExistsException;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.UserInactiveException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Handles user registration, authentication, and logout. */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RevokedTokenService revokedTokenService;
    private final TwoFactorService twoFactorService;

    /**
     * Registers a new user and returns a JWT.
     * Throws {@link EmailAlreadyExistsException} if the email is already taken.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        return new AuthResponse(
                token,
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getRole()
        );
    }

    /**
     * Authenticates a user.
     * <ul>
     *   <li>If 2FA is disabled: returns a {@link LoginResponse} with the JWT immediately.</li>
     *   <li>If 2FA is enabled: generates and emails a code, returns {@link LoginResponse}
     *       with {@code requires2FA=true} and the masked email. The JWT is issued later
     *       via {@link #verifyTwoFactor(String, String)}.</li>
     * </ul>
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) {
            throw new UserInactiveException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        if (user.isTwoFactorEnabled()) {
            twoFactorService.generateAndSend(user);
            return LoginResponse.builder()
                    .requires2FA(Boolean.TRUE)
                    .email(TwoFactorService.maskEmail(user.getEmail()))
                    .build();
        }

        String token = jwtService.generateToken(user);
        return LoginResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Completes the 2FA challenge and issues the JWT if the code is correct.
     * Delegates all validation logic to {@link TwoFactorService#verify(String, String)}.
     */
    @Transactional
    public AuthResponse verifyTwoFactor(String email, String code) {
        return twoFactorService.verify(email, code);
    }

    /**
     * Revokes the given JWT by adding it to the blacklist.
     */
    @Transactional
    public void logout(String token) {
        String email = jwtService.extractEmail(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
        revokedTokenService.revokeToken(token, user);
    }
}
