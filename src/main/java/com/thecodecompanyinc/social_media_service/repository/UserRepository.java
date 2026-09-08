package com.thecodecompanyinc.social_media_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    long countByRole(Role role);
}
