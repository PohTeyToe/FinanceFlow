package com.financeflow.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAccountRequest {

    @NotBlank(message = "Account name is required")
    @Size(max = 100, message = "Account name must not exceed 100 characters")
    private String accountName;
}
