package com.thong.feature.user;


import com.thong.feature.user.dto.CreateUserRequest;
import com.thong.feature.user.dto.UpdateProfileRequest;
import com.thong.feature.user.dto.UserProfileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserSerivce {
    UserProfileResponse register(CreateUserRequest createUserRequest);
    UserProfileResponse getUserProfile(Integer id);
    UserProfileResponse UpdateProfileById(Integer id, UpdateProfileRequest updateProfileRequest);
    List<UserProfileResponse> getUserProfiles();

    void deleteUserById(Integer id);
    void adminCreateNewAdmin(CreateUserRequest createUserRequest);
    UserProfileResponse toggleUserStatus(Integer userId);

    // UserService.java
    UserProfileResponse updateProfileImage(String email, MultipartFile file);
    UserProfileResponse removeProfileImage(String email);
}
