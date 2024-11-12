package com.rental.camp.user.repository;

import com.rental.camp.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUuid(UUID uuid);
}
