package com.example.library.config;

import com.example.library.book.Book;
import com.example.library.book.BookRepository;
import com.example.library.reader.Reader;
import com.example.library.reader.ReaderRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DemoDataConfig {

    @Bean
    CommandLineRunner loadDemoData(BookRepository bookRepository, ReaderRepository readerRepository) {
        return args -> {
            if (bookRepository.count() == 0) {
                bookRepository.saveAll(List.of(
                        new Book("978-7-111-54275-9", "Java核心技术 卷I", "Cay S. Horstmann", "计算机", 3),
                        new Book("978-7-115-42802-8", "深入理解计算机系统", "Randal E. Bryant", "计算机", 2),
                        new Book("978-7-5442-9087-0", "百年孤独", "加西亚·马尔克斯", "文学", 4)
                ));
            }
            if (readerRepository.count() == 0) {
                readerRepository.saveAll(List.of(
                        new Reader("张三", "zhangsan@example.com", "13800000001"),
                        new Reader("李四", "lisi@example.com", "13800000002")
                ));
            }
        };
    }
}
