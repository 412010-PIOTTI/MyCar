package ar.edu.utn.frc.mycar.application.service;

import ar.edu.utn.frc.mycar.domain.entity.User;
import ar.edu.utn.frc.mycar.domain.repository.UserRepository;
import ar.edu.utn.frc.mycar.web.dto.request.ChangePasswordRequest;
import ar.edu.utn.frc.mycar.web.dto.request.UpdateProfileRequest;
import ar.edu.utn.frc.mycar.web.dto.response.UserProfileResponse;
import ar.edu.utn.frc.mycar.web.exception.InvalidCredentialsException;
import ar.edu.utn.frc.mycar.web.exception.PasswordMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RevokedTokenService revokedTokenService;

    @Transactional(readOnly = true)
    public UserProfileResponse getMe(String email) {
        User user = findByEmail(email);
        return toResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = findByEmail(email);
        user.setName(request.getName());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Soft-deletes the account: sets {@code active=false} and revokes the current token.
     * Throws {@link PasswordMismatchException} if the provided password is incorrect.
     */
    @Transactional
    public void deleteAccount(String email, String password, String token) {
        User user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new PasswordMismatchException();
        }
        user.setActive(false);
        userRepository.save(user);
        revokedTokenService.revokeToken(token, user);
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);
    }

    private UserProfileResponse toResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
