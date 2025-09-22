package com.beartrail.user.repository;

import com.beartrail.user.model.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  Optional<User> findByEmail(String email);

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmail(String email);

  boolean existsByEmailIgnoreCase(String email);
}
