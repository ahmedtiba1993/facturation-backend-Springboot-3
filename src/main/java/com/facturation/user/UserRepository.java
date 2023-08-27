package com.facturation.user;

import com.facturation.dto.UserDto;
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

  @Query(
      "SELECT new com.facturation.dto.UserDto(u.firstname, u.lastname, u.email, u.tel, u.fax, u.mobile) from User u where u.email = :email")
  UserDto findUserByEmail(String email);

  @Modifying
  @Transactional
  @Query(
      "UPDATE User u SET u.firstname = :firstName,"
          + " u.lastname = :lastName,"
          + " u.email = :email, u.tel = :tel,"
          + " u.fax = :fax,"
          + " u.mobile = :mobile"
          + " WHERE u.id = :id")
  void modifierUserInfo(
      Integer id,
      String firstName,
      String lastName,
      String email,
      Integer tel,
      Integer fax,
      Integer mobile);
}
