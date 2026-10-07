package com.example.library.loan;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    boolean existsByBookIdAndStatus(Long bookId, LoanStatus status);

    boolean existsByReaderIdAndStatus(Long readerId, LoanStatus status);

    boolean existsByBookId(Long bookId);

    boolean existsByReaderId(Long readerId);

    boolean existsByBookIdAndReaderIdAndStatus(Long bookId, Long readerId, LoanStatus status);

    Page<Loan> findByStatus(LoanStatus status, Pageable pageable);

    Page<Loan> findByReaderId(Long readerId, Pageable pageable);

    Page<Loan> findByReaderIdAndStatus(Long readerId, LoanStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Loan l where l.id = :id")
    Optional<Loan> findByIdForUpdate(@Param("id") Long id);
}
