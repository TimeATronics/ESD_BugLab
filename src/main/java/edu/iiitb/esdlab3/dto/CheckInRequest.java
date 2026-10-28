package edu.iiitb.esdlab3.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInRequest {

    @NotBlank(message = "Student email is required")
    @Email(message = "Invalid email format")
    private String studentEmail;
}
