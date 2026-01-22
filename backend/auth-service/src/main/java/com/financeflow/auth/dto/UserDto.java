package com.financeflow.auth.dto;

import java.time.Instant;
import java.util.UUID;

import com.financeflow.auth.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private UUID id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Instant createdAt;

    public static UserDto fromUser(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
