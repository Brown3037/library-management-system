package com.example.library.reader;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReaderRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 80, message = "Name must be at most 80 characters")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 160, message = "Email must be at most 160 characters")
        String email,

        @Size(max = 30, message = "Phone must be at most 30 characters")
        String phone,

        Boolean active
) {
    public boolean activeOrDefault() {
        return active == null || active;
    }
}
