# 代码阅读路线

不要按文件名从上到下硬读。跟着一次“读者借书”的请求走，最容易理解后端分层。

## 第一站：程序怎么启动

打开 `LibraryManagementApplication.java`。

- `@SpringBootApplication` 让 Spring 扫描并创建 Controller、Service、Repository 等对象。
- `main` 方法调用 `SpringApplication.run`，启动内嵌 Web 服务器。
- `application.yml` 配置端口、H2 数据库和 JPA 建表策略。

## 第二站：HTTP 请求怎么进来

打开 `loan/LoanController.java`，找到：

```java
@PostMapping
public ResponseEntity<LoanResponse> borrow(@Valid @RequestBody BorrowRequest request)
```

它把 `POST /api/loans` 的 JSON 转成 `BorrowRequest`。`@Valid` 会执行 DTO 上的参数校验。Controller 不写业务规则，只把请求交给 `LoanService`。

## 第三站：业务规则在哪里

打开 `loan/LoanService.java` 的 `borrow` 方法，按顺序观察：

1. 加锁读取图书，避免多人同时借走最后一本。
2. 查询读者是否存在。
3. 检查读者是否启用。
4. 检查是否有可借库存。
5. 检查同一读者是否已经借了这本书。
6. 创建 `Loan`，并将图书可借库存减一。
7. 在同一个 `@Transactional` 事务中写入数据库。

这是本项目最重要的代码。

## 第四站：对象如何变成数据库表

依次打开：

- `book/Book.java`
- `reader/Reader.java`
- `loan/Loan.java`

重点看：

- `@Entity`：这是持久化实体。
- `@Id` 和 `@GeneratedValue`：数据库主键自动生成。
- `@ManyToOne`：多条借阅记录可以指向同一本书或同一位读者。
- `@Enumerated(EnumType.STRING)`：把 `ACTIVE`、`RETURNED` 作为文字保存。
- 实体方法 `borrowCopy`、`returnCopy`、`markReturned`：让状态变化集中在对象内部。

数据库关系：

```text
Book 1 ─────< Loan >───── 1 Reader
```

一本书和一个读者都可以对应多条历史借阅记录。

## 第五站：Repository 为什么几乎没有实现代码

打开三个 `*Repository.java` 文件。它们继承 `JpaRepository`，Spring Data JPA 会根据方法名生成查询实现，例如：

```java
boolean existsByBookIdAndReaderIdAndStatus(...)
```

`findByIdForUpdate` 使用悲观写锁，解决库存并发竞争。

## 第六站：请求与响应为什么不用 Entity

查看 `BookRequest`、`BookResponse`、`BorrowRequest`、`LoanResponse`。

- Request DTO 控制客户端允许提交哪些字段，并承载参数校验。
- Response DTO 控制对外返回的结构。
- Entity 只负责领域状态和数据库映射，不直接成为 API 合同。

这种分离能避免客户端意外修改 `availableCopies` 等内部字段。

## 第七站：错误如何统一返回

打开 `common/GlobalExceptionHandler.java`。

- 找不到资源时返回 `404`。
- 违反借阅规则时返回 `409`。
- JSON 参数校验失败时返回 `400`，并列出字段错误。
- 数据库并发冲突时也返回 `409`，提示重试。

这样每个 Controller 无需重复写 `try/catch`。

## 第八站：测试如何隔离业务逻辑

打开：

- `BookServiceTest.java`
- `LoanServiceTest.java`

Mockito 创建假的 Repository，所以测试只关注 Service 的业务规则，不需要启动服务器和数据库。

建议给自己的练习：

1. 新增“每位读者最多借 5 本书”的规则。
2. 新增续借接口 `POST /api/loans/{id}/renew`。
3. 给逾期借阅增加每天 0.5 元的费用计算。
4. 为 ReaderService 补充单元测试。
