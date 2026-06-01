package ar.edu.utn.frc.mycar.domain.repository;

import ar.edu.utn.frc.mycar.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Persistence operations for {@link User} entities. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Returns the user with the given email, or empty if none exists. */
    Optional<User> findByEmail(String email);

    /** Returns {@code true} if an account is already registered with the given email. */
    boolean existsByEmail(String email);
}
