package com.example.library.loan;

import com.example.library.book.Book;
import com.example.library.book.BookRepository;
import com.example.library.common.BusinessRuleException;
import com.example.library.common.PageResponse;
import com.example.library.common.ResourceNotFoundException;
import com.example.library.reader.Reader;
import com.example.library.reader.ReaderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    public LoanService(
            LoanRepository loanRepository,
            BookRepository bookRepository,
            ReaderRepository readerRepository
    ) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
    }

    @Transactional
    public LoanResponse borrow(BorrowRequest request) {
        Book book = bookRepository.findByIdForUpdate(request.bookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book " + request.bookId() + " was not found"));
        Reader reader = readerRepository.findById(request.readerId())
                .orElseThrow(() -> new ResourceNotFoundException("Reader " + request.readerId() + " was not found"));

        if (!reader.isActive()) {
            throw new BusinessRuleException("Inactive readers cannot borrow books");
        }
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No copies of this book are currently available");
        }
        if (loanRepository.existsByBookIdAndReaderIdAndStatus(
                book.getId(), reader.getId(), LoanStatus.ACTIVE)) {
            throw new BusinessRuleException("This reader already has an active loan for this book");
        }

        LocalDateTime now = LocalDateTime.now();
        Loan loan = new Loan(book, reader, now, now.plusDays(request.loanDaysOrDefault()));
        book.borrowCopy();
        return LoanResponse.from(loanRepository.save(loan));
    }

    @Transactional
    public LoanResponse returnBook(Long loanId) {
        Loan loan = loanRepository.findByIdForUpdate(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + loanId + " was not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("This loan has already been returned");
        }

        Book book = bookRepository.findByIdForUpdate(loan.getBook().getId())
                .orElseThrow(() -> new ResourceNotFoundException("The loan's book was not found"));
        loan.markReturned(LocalDateTime.now());
        book.returnCopy();
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public LoanResponse get(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan " + id + " was not found"));
        return LoanResponse.from(loan);
    }

    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> list(Long readerId, LoanStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("borrowedAt").descending());
        Page<Loan> loans;

        if (readerId != null && status != null) {
            loans = loanRepository.findByReaderIdAndStatus(readerId, status, pageable);
        } else if (readerId != null) {
            loans = loanRepository.findByReaderId(readerId, pageable);
        } else if (status != null) {
            loans = loanRepository.findByStatus(status, pageable);
        } else {
            loans = loanRepository.findAll(pageable);
        }
        return PageResponse.from(loans, LoanResponse::from);
    }
}
