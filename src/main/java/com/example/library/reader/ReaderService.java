package com.example.library.reader;

import com.example.library.common.BusinessRuleException;
import com.example.library.common.PageResponse;
import com.example.library.common.ResourceNotFoundException;
import com.example.library.loan.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class ReaderService {

    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;

    public ReaderService(ReaderRepository readerRepository, LoanRepository loanRepository) {
        this.readerRepository = readerRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional
    public ReaderResponse create(ReaderRequest request) {
        String email = normalizeEmail(request.email());
        if (readerRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("A reader with email " + email + " already exists");
        }

        Reader reader = new Reader(request.name().trim(), email, normalizePhone(request.phone()));
        if (!request.activeOrDefault()) {
            reader.update(reader.getName(), reader.getEmail(), reader.getPhone(), false);
        }
        return ReaderResponse.from(readerRepository.save(reader));
    }

    @Transactional(readOnly = true)
    public ReaderResponse get(Long id) {
        return ReaderResponse.from(findReader(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ReaderResponse> list(String query, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Reader> readers;
        if (query == null || query.isBlank()) {
            readers = readerRepository.findAll(pageable);
        } else {
            String keyword = query.trim();
            readers = readerRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword, keyword, pageable);
        }
        return PageResponse.from(readers, ReaderResponse::from);
    }

    @Transactional
    public ReaderResponse update(Long id, ReaderRequest request) {
        Reader reader = findReader(id);
        String email = normalizeEmail(request.email());
        if (readerRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new BusinessRuleException("A reader with email " + email + " already exists");
        }
        reader.update(
                request.name().trim(),
                email,
                normalizePhone(request.phone()),
                request.active() == null ? reader.isActive() : request.active()
        );
        return ReaderResponse.from(reader);
    }

    @Transactional
    public void delete(Long id) {
        Reader reader = findReader(id);
        if (loanRepository.existsByReaderId(id)) {
            throw new BusinessRuleException("Cannot delete a reader who has loan history");
        }
        readerRepository.delete(reader);
    }

    private Reader findReader(Long id) {
        return readerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reader " + id + " was not found"));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
