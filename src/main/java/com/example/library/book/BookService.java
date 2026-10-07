package com.example.library.book;

import com.example.library.common.BusinessRuleException;
import com.example.library.common.PageResponse;
import com.example.library.common.ResourceNotFoundException;
import com.example.library.loan.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        String isbn = request.isbn().trim();
        if (bookRepository.existsByIsbnIgnoreCase(isbn)) {
            throw new BusinessRuleException("A book with ISBN " + isbn + " already exists");
        }

        Book book = new Book(
                isbn,
                request.title().trim(),
                request.author().trim(),
                request.category().trim(),
                request.totalCopies()
        );
        return BookResponse.from(bookRepository.save(book));
    }

    @Transactional(readOnly = true)
    public BookResponse get(Long id) {
        return BookResponse.from(findBook(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> list(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Book> books;
        if (query == null || query.isBlank()) {
            books = bookRepository.findAll(pageable);
        } else {
            String keyword = query.trim();
            books = bookRepository
                    .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
                            keyword, keyword, keyword, pageable);
        }
        return PageResponse.from(books, BookResponse::from);
    }

    @Transactional
    public BookResponse update(Long id, BookRequest request) {
        Book book = findBook(id);
        String isbn = request.isbn().trim();

        if (bookRepository.existsByIsbnIgnoreCaseAndIdNot(isbn, id)) {
            throw new BusinessRuleException("A book with ISBN " + isbn + " already exists");
        }

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (request.totalCopies() < borrowedCopies) {
            throw new BusinessRuleException(
                    "Total copies cannot be lower than the " + borrowedCopies + " currently borrowed copies");
        }

        book.update(
                isbn,
                request.title().trim(),
                request.author().trim(),
                request.category().trim(),
                request.totalCopies()
        );
        return BookResponse.from(book);
    }

    @Transactional
    public void delete(Long id) {
        Book book = findBook(id);
        if (loanRepository.existsByBookId(id)) {
            throw new BusinessRuleException("Cannot delete a book that has loan history");
        }
        bookRepository.delete(book);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " was not found"));
    }
}
