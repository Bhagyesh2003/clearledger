package com.clearledger.user_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String userId;
    private String email;
    private String fullName;
}

//This is what both /register and /login return.
// The frontend stores the accessToken and sends it as "Authorization: Bearer {token}" on every request.

