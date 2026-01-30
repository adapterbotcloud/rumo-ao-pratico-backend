package com.rumoaopratico.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateUserRequest {
    private String role;    // "USER" or "ADMIN"
    private Boolean enabled; // true or false
}
