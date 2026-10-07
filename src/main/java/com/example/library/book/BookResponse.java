package com.example.library.book;

import java.time.LocalDateTime;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String category,
        int totalCopies,
        int availableCopies,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getCategory(),
                book.getTotalCopies(),
                book.getAvailableCopies(),
                book.getCreatedAt(),
                book.getUpdatedAt()
        );
    }
}
