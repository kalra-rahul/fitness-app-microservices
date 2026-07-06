package com.fitness.userservice.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email valid format")
    private String email;

    @NotBlank(message = "password is required")
    @Size(message = "password least have 6 charters", min = 6)
    private String password;
    private String keycloakId;
    private String firstName;
    private String lastName;
}
