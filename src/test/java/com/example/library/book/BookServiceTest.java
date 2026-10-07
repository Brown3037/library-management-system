package com.example.library.book;

import com.example.library.common.BusinessRuleException;
import com.example.library.loan.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private LoanRepository loanRepository;

    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService = new BookService(bookRepository, loanRepository);
    }

    @Test
    void createsBookWhenIsbnIsUnique() {
        BookRequest request = new BookRequest("978-1", "Clean Code", "Robert C. Martin", "Software", 3);
        when(bookRepository.existsByIsbnIgnoreCase("978-1")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BookResponse response = bookService.create(request);

        assertThat(response.title()).isEqualTo("Clean Code");
        assertThat(response.availableCopies()).isEqualTo(3);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void rejectsDuplicateIsbn() {
        BookRequest request = new BookRequest("978-1", "Clean Code", "Robert C. Martin", "Software", 3);
        when(bookRepository.existsByIsbnIgnoreCase("978-1")).thenReturn(true);

        assertThatThrownBy(() -> bookService.create(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("already exists");

        verify(bookRepository, never()).save(any());
    }

    @Test
    void cannotReduceTotalBelowBorrowedCopies() {
        Book book = new Book("978-1", "Clean Code", "Robert C. Martin", "Software", 3);
        book.borrowCopy();
        book.borrowCopy();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookRequest request = new BookRequest("978-1", "Clean Code", "Robert C. Martin", "Software", 1);

        assertThatThrownBy(() -> bookService.update(1L, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("currently borrowed");
    }

    @Test
    void cannotDeleteBookWithActiveLoan() {
        Book book = new Book("978-1", "Clean Code", "Robert C. Martin", "Software", 3);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.existsByBookId(1L)).thenReturn(true);

        assertThatThrownBy(() -> bookService.delete(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("loan history");

        verify(bookRepository, never()).delete(any());
    }
}
