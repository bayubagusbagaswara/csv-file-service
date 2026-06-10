package com.bayu.csvfileservice.dto.authorization;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLimitAuthorizationRequest {

    @NotBlank(message = "userId is required")
    private String userId;

    private String username;

    private String userGroup;

    @NotBlank(message = "limit is required")
    private String limit;
}