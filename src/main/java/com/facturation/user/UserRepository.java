package com.facturation.user;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

  Optional<User> findByEmail(String email);

  @Modifying
  @Transactional
  @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
  void updatePassword(Integer id, String password);
}
