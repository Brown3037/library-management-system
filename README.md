# 图书管理系统后端

一个适合学习和继续扩展的 Spring Boot REST API。项目使用 Java 17、Spring Boot 4.1.1、Spring Data JPA、Bean Validation 和 H2 文件数据库。

## 已实现功能

- 图书新增、查询、搜索、修改、删除
- 读者新增、查询、搜索、修改、删除
- 借书、归还、借阅记录查询
- 自动维护总库存和可借库存
- 默认借期 14 天，可在 1～60 天内指定
- 识别逾期借阅
- 防止无库存借阅、重复借阅和重复归还
- 防止删除存在借阅历史的图书或读者，保证历史记录完整
- 统一参数校验和错误响应
- H2 文件数据库，重启后保留数据
- 示例数据、单元测试和 IntelliJ HTTP 请求文件

## 项目结构

```text
src/main/java/com/example/library
├── LibraryManagementApplication.java   # 程序入口
├── book/                               # 图书：Controller / Service / Repository / Entity / DTO
├── reader/                             # 读者：Controller / Service / Repository / Entity / DTO
├── loan/                               # 借阅：借书、归还和记录查询
├── common/                             # 分页、异常和统一错误响应
└── config/                             # 示例数据初始化
```

核心调用链：

```text
HTTP 请求 → Controller → Service → Repository → H2 数据库
                           ↓
                       业务规则与事务
```

## 启动

在项目根目录执行：

```powershell
mvn spring-boot:run
```

启动后访问：

- API 状态：<http://localhost:8080/api>
- 图书接口：<http://localhost:8080/api/books>
- H2 控制台：<http://localhost:8080/h2-console>

H2 控制台配置：

```text
JDBC URL: jdbc:h2:file:./data/librarydb
User Name: sa
Password: 留空
```

项目第一次启动会自动创建 3 本图书和 2 位读者。数据库文件位于 `data/`，已被 `.gitignore` 忽略。

## API 一览

| 方法 | 地址 | 说明 |
|---|---|---|
| `POST` | `/api/books` | 新增图书 |
| `GET` | `/api/books` | 分页查询图书；支持 `q` 搜索 |
| `GET` | `/api/books/{id}` | 查询单本图书 |
| `PUT` | `/api/books/{id}` | 修改图书 |
| `DELETE` | `/api/books/{id}` | 删除图书 |
| `POST` | `/api/readers` | 新增读者 |
| `GET` | `/api/readers` | 分页查询读者；支持 `q` 搜索 |
| `GET` | `/api/readers/{id}` | 查询单个读者 |
| `PUT` | `/api/readers/{id}` | 修改读者 |
| `DELETE` | `/api/readers/{id}` | 删除读者 |
| `POST` | `/api/loans` | 借书 |
| `POST` | `/api/loans/{id}/return` | 归还 |
| `GET` | `/api/loans` | 查询借阅；支持 `readerId`、`status` |
| `GET` | `/api/loans/{id}` | 查询单条借阅 |

分页接口支持 `page` 和 `size`，例如：

```text
GET /api/books?q=Java&page=0&size=10
GET /api/loans?readerId=1&status=ACTIVE
```

## 快速体验借阅流程

```powershell
curl.exe -X POST http://localhost:8080/api/loans `
  -H "Content-Type: application/json" `
  -d '{"bookId":1,"readerId":1,"loanDays":14}'
```

响应中的 `id` 是借阅 ID。使用它归还：

```powershell
curl.exe -X POST http://localhost:8080/api/loans/1/return
```

也可以在 IntelliJ IDEA 中打开 `api-tests.http`，逐个点击请求左侧的运行按钮。

## 测试和打包

```powershell
mvn test
mvn clean package
java -jar target/library-management-backend-0.0.1-SNAPSHOT.jar
```

## 错误响应示例

```json
{
  "timestamp": "2026-10-07T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "No copies of this book are currently available",
  "path": "/api/loans",
  "validationErrors": {}
}
```

## 下一步可扩展

- Spring Security + JWT 登录和管理员/读者角色
- MySQL 或 PostgreSQL
- 图书预约、续借、罚款
- Flyway 数据库版本管理
- OpenAPI/Swagger 文档
- 前端管理页面

代码学习顺序见 [`docs/CODE_READING_GUIDE.md`](docs/CODE_READING_GUIDE.md)。

