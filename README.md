# AI Demo 项目

一个基于 Spring Boot 的 AI 问答系统，集成了 DeepSeek API，支持知识库管理、实时问答和流式响应。

## 🚀 技术栈

### 后端框架
- **Spring Boot 3.2.0** - 现代 Java 后端框架
- **Java 21** - 使用最新的 LTS 版本
- **Maven** - 项目构建和依赖管理

### 数据库
- **MySQL 8.0** - 关系型数据库，存储知识库数据
- **Redis 7** - 缓存层，提高响应速度
- **Spring Data JPA** - 数据持久化
- **Spring Data Redis** - Redis 集成

### AI 集成
- **DeepSeek API** - 大语言模型服务
- **SSE (Server-Sent Events)** - 流式响应传输
- **LangChain4j 0.34.0** - AI 应用开发框架

### Web 技术
- **Thymeleaf** - 服务器端模板引擎
- **Spring Security** - 安全认证和授权
- **Spring Web** - RESTful API 支持

### 开发工具
- **Lombok** - 减少样板代码
- **Spring Boot DevTools** - 热部署
- **Jackson** - JSON 序列化/反序列化

### 容器化
- **Docker** - 应用容器化
- **Docker Compose** - 多容器编排

## 📁 项目结构

```
src/main/java/org/lcr/aidemo/
├── AiDemoApplication.java     # 应用启动类
├── config/                    # 配置类
├── controller/               # 控制器层
├── entity/                   # 实体类
├── repository/               # 数据访问层
└── service/                  # 业务逻辑层
```

## 🛠️ 环境要求

- **Java 21** 或更高版本
- **Maven 3.6+**
- **Docker 20.10+** 和 **Docker Compose 2.0+** (可选)
- **MySQL 8.0+**
- **Redis 7+**
- **DeepSeek API Key** (从 [DeepSeek 官网](https://platform.deepseek.com/) 获取)

## 🚀 快速启动

### 方式一：使用 Docker Compose (推荐)

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd AIDemo
   ```

2. **配置环境变量**
   复制 `.env.example` 文件并配置：
   ```bash
   cp .env.example .env
   ```
   编辑 `.env` 文件，设置以下变量：
   ```env
   # DeepSeek API Key (必需)
   DEEPSEEK_API_KEY=your_deepseek_api_key_here
   
   # Redis 密码 (可选)
   REDIS_PASSWORD=your_redis_password
   ```

3. **构建并启动**
   ```bash
   docker-compose up -d
   ```

4. **访问应用**
   - 应用地址: http://localhost:8080
   - MySQL: localhost:3306 (默认用户: root, 密码: root)
   - Redis: localhost:6379

### 方式二：本地开发运行

1. **安装依赖**
   ```bash
   mvn clean install
   ```

2. **配置数据库**
   - 启动 MySQL 和 Redis 服务
   - 创建数据库: `aidemo`

3. **配置环境变量**
   ```bash
   # Linux/macOS
   export DEEPSEEK_API_KEY=your_api_key
   export DB_PASSWORD=your_mysql_password
   export REDIS_PASSWORD=your_redis_password
   
   # Windows (PowerShell)
   $env:DEEPSEEK_API_KEY="your_api_key"
   $env:DB_PASSWORD="your_mysql_password"
   $env:REDIS_PASSWORD="your_redis_password"
   ```

4. **运行应用**
   ```bash
   mvn spring-boot:run
   ```
   或使用 IDE 直接运行 `AiDemoApplication.java`

5. **访问应用**
   - http://localhost:8080

### 方式三：生产部署

1. **构建 JAR 包**
   ```bash
   mvn clean package -DskipTests
   ```

2. **运行 JAR**
   ```bash
   java -jar target/AIDemo-0.0.1-SNAPSHOT.jar
   ```

## ⚙️ 配置说明

### 主要配置文件

1. **`application.properties`** - 主配置文件
   ```properties
   # DeepSeek API 配置
   deepseek.api-key=${DEEPSEEK_API_KEY}
   deepseek.base-url=https://api.deepseek.com/v1
   deepseek.model=deepseek-chat
   
   # 数据库配置
   spring.datasource.url=jdbc:mysql://${DB_HOST}:${DB_PORT:3306}/aidemo
   spring.datasource.username=${DB_USERNAME:root}
   spring.datasource.password=${DB_PASSWORD:root}
   
   # Redis 配置
   spring.data.redis.host=${REDIS_HOST}
   spring.data.redis.port=${REDIS_PORT:6379}
   spring.data.redis.password=${REDIS_PASSWORD:}
   ```

2. **`application-local.properties`** - 本地开发配置
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/aidemo
   spring.jpa.show-sql=true
   logging.level.org.lcr.aidemo=DEBUG
   ```

### Docker Compose 配置

- **MySQL**: 端口 3306，数据持久化到 `mysql-data` 卷
- **Redis**: 端口 6379，数据持久化到 `redis-data` 卷
- **应用**: 端口 8080，依赖 MySQL 和 Redis

## 🔧 功能特性

### 1. 知识库管理
- 支持问答对的增删改查
- 知识库数据持久化到 MySQL
- 缓存机制提高访问速度

### 2. AI 问答
- 集成 DeepSeek API
- 支持流式响应 (SSE)
- 智能缓存已回答的问题

### 3. 用户界面
- 简洁的管理后台
- 实时问答界面
- 响应式设计

### 4. 性能优化
- Redis 缓存层
- 连接池配置
- 异步处理

## 🔒 安全配置

- Spring Security 基础认证
- 环境变量存储敏感信息
- 数据库连接加密配置

## 📊 API 接口

### 问答接口
- `POST /api/answer` - 获取 AI 回答 (SSE 流式响应)
- `GET /api/kb` - 获取知识库列表
- `POST /api/kb` - 添加知识库条目
- `DELETE /api/kb/{id}` - 删除知识库条目

## 🐛 常见问题

### Q1: 如何获取 DeepSeek API Key?
A: 访问 [DeepSeek 平台](https://platform.deepseek.com/)，注册账号并创建 API Key。

### Q2: 数据库连接失败怎么办?
A: 检查：
1. MySQL 服务是否运行
2. 数据库 `aidemo` 是否存在
3. 用户名密码是否正确
4. 防火墙是否开放 3306 端口

### Q3: Redis 连接失败怎么办?
A: 检查：
1. Redis 服务是否运行
2. 密码是否正确配置
3. 防火墙是否开放 6379 端口

### Q4: 如何修改端口?
A: 在 `application.properties` 中修改：
```properties
server.port=9090
```

## 📈 性能监控

应用内置了以下监控和日志：
- 请求日志记录
- 错误日志追踪
- 数据库查询日志 (开发环境)
- Redis 操作日志

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 📞 支持

如有问题，请：
1. 查看 [LINUX_STARTUP_GUIDE.md](LINUX_STARTUP_GUIDE.md) 获取详细启动指南
2. 检查日志文件获取错误信息
3. 提交 Issue 描述问题

---

**提示**: 首次启动时，应用会自动创建数据库表结构。确保 MySQL 用户有足够的权限。