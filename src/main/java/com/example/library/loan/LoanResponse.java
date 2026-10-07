package com.example.library.loan;

import java.time.LocalDateTime;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long readerId,
        String readerName,
        LocalDateTime borrowedAt,
        LocalDateTime dueAt,
        LocalDateTime returnedAt,
        LoanStatus status,
        boolean overdue
) {
    public static LoanResponse from(Loan loan) {
        boolean overdue = loan.getStatus() == LoanStatus.ACTIVE
                && loan.getDueAt().isBefore(LocalDateTime.now());
        return new LoanResponse(
                loan.getId(),
                loan.getBook().getId(),
                loan.getBook().getTitle(),
                loan.getReader().getId(),
                loan.getReader().getName(),
                loan.getBorrowedAt(),
                loan.getDueAt(),
                loan.getReturnedAt(),
                loan.getStatus(),
                overdue
        );
    }
}
