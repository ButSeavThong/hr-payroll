package com.thong.feature.user.dto;


import java.time.LocalDate;

public record UserProfileResponse(
        Integer id,
        String username,
        String email,
        String gender,
        LocalDate dob,
        Boolean isDeleted,
        String profileImage,
        Boolean isEnabled
) {
}
