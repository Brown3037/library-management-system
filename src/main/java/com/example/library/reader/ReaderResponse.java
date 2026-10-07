package com.example.library.reader;

import java.time.LocalDateTime;

public record ReaderResponse(
        Long id,
        String name,
        String email,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ReaderResponse from(Reader reader) {
        return new ReaderResponse(
                reader.getId(),
                reader.getName(),
                reader.getEmail(),
                reader.getPhone(),
                reader.isActive(),
                reader.getCreatedAt(),
                reader.getUpdatedAt()
        );
    }
}
