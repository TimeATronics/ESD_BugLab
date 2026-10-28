package edu.iiitb.esdlab3.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull // without this, an omitted capacity reaches the NOT NULL column and returns 500
    @Min(value = 1)
    private Integer capacity;
}