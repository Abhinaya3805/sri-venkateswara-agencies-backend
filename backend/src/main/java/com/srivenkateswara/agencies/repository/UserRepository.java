package com.srivenkateswara.agencies.repository;

import com.srivenkateswara.agencies.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    Boolean existsByEmail(String email);

    Boolean existsByMobileNumber(String mobileNumber);

    Optional<User> findByFirebaseUid(String firebaseUid);
}
