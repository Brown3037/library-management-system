package com.example.library.loan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BorrowRequest(
        @NotNull(message = "Book id is required")
        Long bookId,

        @NotNull(message = "Reader id is required")
        Long readerId,

        @Min(value = 1, message = "Loan days must be at least 1")
        @Max(value = 60, message = "Loan days must be at most 60")
        Integer loanDays
) {
    public int loanDaysOrDefault() {
        return loanDays == null ? 14 : loanDays;
    }
}
