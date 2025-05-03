package com.example.demo.Repositories;

import com.example.demo.Domain.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepo extends JpaRepository<UserInfo, UUID> {
    Optional<UserInfo> findByCandidatesProfiles_ID(long ID);
    boolean existsByEmail(String email);

    Optional<UserInfo> findByEmail(String email);

}
