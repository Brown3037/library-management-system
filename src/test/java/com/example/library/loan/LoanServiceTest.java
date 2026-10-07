package com.example.library.loan;

import com.example.library.book.Book;
import com.example.library.book.BookRepository;
import com.example.library.common.BusinessRuleException;
import com.example.library.reader.Reader;
import com.example.library.reader.ReaderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ReaderRepository readerRepository;

    private LoanService loanService;

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, bookRepository, readerRepository);
    }

    @Test
    void borrowingDecreasesAvailableCopies() {
        Book book = new Book("978-1", "Clean Code", "Robert C. Martin", "Software", 2);
        Reader reader = new Reader("Alice", "alice@example.com", null);
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(readerRepository.findById(2L)).thenReturn(Optional.of(reader));
        when(loanRepository.existsByBookIdAndReaderIdAndStatus(null, null, LoanStatus.ACTIVE))
                .thenReturn(false);
        when(loanRepository.save(any(Loan.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoanResponse response = loanService.borrow(new BorrowRequest(1L, 2L, 21));

        assertThat(response.status()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(response.dueAt()).isAfter(response.borrowedAt().plusDays(20));
        assertThat(book.getAvailableCopies()).isEqualTo(1);
    }

    @Test
    void borrowingFailsWhenNoCopyIsAvailable() {
        Book book = new Book("978-1", "Clean Code", "Robert C. Martin", "Software", 1);
        book.borrowCopy();
        Reader reader = new Reader("Alice", "alice@example.com", null);
        when(bookRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(book));
        when(readerRepository.findById(2L)).thenReturn(Optional.of(reader));

        assertThatThrownBy(() -> loanService.borrow(new BorrowRequest(1L, 2L, null)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("No copies");

        verify(loanRepository, never()).save(any());
    }

    @Test
    void returningBookRestoresAvailableCopy() {
        Book book = new Book("978-1", "Clean Code", "Robert C. Martin", "Software", 2);
        Reader reader = new Reader("Alice", "alice@example.com", null);
        book.borrowCopy();
        Loan loan = new Loan(book, reader, LocalDateTime.now().minusDays(2), LocalDateTime.now().plusDays(12));
        when(loanRepository.findByIdForUpdate(9L)).thenReturn(Optional.of(loan));
        when(bookRepository.findByIdForUpdate(null)).thenReturn(Optional.of(book));

        LoanResponse response = loanService.returnBook(9L);

        assertThat(response.status()).isEqualTo(LoanStatus.RETURNED);
        assertThat(response.returnedAt()).isNotNull();
        assertThat(book.getAvailableCopies()).isEqualTo(2);
    }
}
