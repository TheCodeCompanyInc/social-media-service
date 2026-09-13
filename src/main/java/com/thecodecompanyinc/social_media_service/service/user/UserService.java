package com.thecodecompanyinc.social_media_service.service.user;

import com.thecodecompanyinc.social_media_service.dto.auth.GoogleLoginDto;
import com.thecodecompanyinc.social_media_service.dto.auth.UpdateUserDto;
import com.thecodecompanyinc.social_media_service.entity.Role;
import com.thecodecompanyinc.social_media_service.entity.User;
import java.text.ParseException;
import java.util.List;
import java.util.Optional;

public interface UserService {
  User saveUser(User user);

  User findOrCreateGoogleUser(GoogleLoginDto dto);

  Optional<User> getUser(String email);

  User getUserByJwt(String authHeader);

  User getUserById(Long id);

  User getUserByEmail(String email);

  List<User> getUsers();

  long countUsers();

  long countUsersByRole(Role role);

  User updateUser(String authHeader, UpdateUserDto updateUserDto) throws ParseException;

  void updateUserPhoto(String authHeader, String url);

  void updateFcmToken(String email, String fcmToken);

  User updateUserRole(Long userId, Role newRole);

  User updateEmailVerified(Long userId, boolean verified);
}
