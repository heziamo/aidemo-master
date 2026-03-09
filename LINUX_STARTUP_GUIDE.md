# AIDemo 项目 Linux 启动指南

## 前置条件

在 Linux 上启动项目前，需要安装以下工具：

1. **Docker** (>= 20.10)
2. **Docker Compose** (>= 1.29)

### 安装 Docker 和 Docker Compose

```bash
# 更新系统包
sudo apt update && sudo apt upgrade -y

# 安装 Docker
sudo apt install -y docker.io

# 启动 Docker 服务
sudo systemctl start docker
sudo systemctl enable docker

# 将当前用户添加到 docker 组（避免每次都用 sudo）
sudo usermod -aG docker $USER

# 重新登录或运行以下命令激活新组
newgrp docker

# 安装 Docker Compose
sudo apt install -y docker-compose

# 验证安装
docker --version
docker-compose --version
```

---

## 启动方式

### 方式一：使用 Docker Compose（推荐）

这种方式会同时启动应用、MySQL 和 Redis。

```bash
# 1. 进入项目根目录
cd /path/to/aidemo-master

# 2. 构建和启动容器
docker-compose up -d

# 3. 查看容器日志
docker-compose logs -f aidemo

# 4. 停止容器
docker-compose down

# 5. 删除所有数据（包括数据库数据）
docker-compose down -v
```

### 方式二：使用独立 Docker 镜像

如果你已经有 MySQL 和 Redis 服务，可以只启动应用。

```bash
# 1. 进入项目根目录
cd /path/to/aidemo-master

# 2. 构建镜像
docker build -t aidemo:latest .

# 3. 运行容器
docker run -d \
  --name aidemo \
  -p 8080:8080 \
  -e DB_HOST=your-db-host \
  -e DB_PORT=3306 \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=root \
  -e REDIS_HOST=your-redis-host \
  -e REDIS_PORT=6379 \
  -e DASHSCOPE_API_KEY=your-api-key \
  aidemo:latest

# 4. 查看日志
docker logs -f aidemo

# 5. 停止容器
docker stop aidemo
docker rm aidemo
```

### 方式三：直接在 Linux 上构建和运行

如果不想使用 Docker，也可以直接在 Linux 上运行。

```bash
# 1. 前置条件：安装 JDK 21 和 Maven
sudo apt install -y openjdk-21-jdk maven

# 2. 进入项目根目录
cd /path/to/aidemo-master

# 3. 构建项目
mvn clean package

# 4. 运行 JAR 文件
java -jar target/AIDemo-0.0.1-SNAPSHOT.jar

# 或者使用环境变量指定数据库和 Redis
java -jar \
  -Dspring.datasource.url=jdbc:mysql://localhost:3306/AIDemo \
  -Dspring.datasource.username=root \
  -Dspring.datasource.password=root \
  -Dspring.redis.host=localhost \
  -Ddashscope.api-key=your-api-key \
  target/AIDemo-0.0.1-SNAPSHOT.jar
```

---

## 常用 Docker 命令

```bash
# 查看所有正在运行的容器
docker ps

# 查看所有容器（包括已停止的）
docker ps -a

# 查看容器详细信息
docker inspect container-name

# 进入容器终端
docker exec -it container-name /bin/bash

# 查看容器日志（实时）
docker logs -f container-name

# 查看最后 100 行日志
docker logs --tail 100 container-name

# 停止容器
docker stop container-name

# 删除容器
docker rm container-name

# 删除镜像
docker rmi image-name

# 清理未使用的资源
docker system prune -a
```

---

## 访问应用

启动成功后，可以访问以下地址：

- **应用首页**: http://localhost:8080/home
- **登录页**: http://localhost:8080/login

---

## 环境变量配置

### 使用 .env 文件

在项目根目录创建 `.env` 文件，Docker Compose 会自动加载：

```bash
# .env
DASHSCOPE_API_KEY=sk-your-api-key-here
DB_PASSWORD=your-mysql-password
REDIS_PASSWORD=your-redis-password
```

然后运行：
```bash
docker-compose up -d
```

---

## 故障排查

### 1. 容器无法启动

```bash
# 查看详细错误日志
docker logs aidemo

# 检查容器状态
docker inspect aidemo | grep -A 5 "State"
```

### 2. 数据库连接失败

```bash
# 检查 MySQL 容器是否运行
docker ps | grep mysql

# 检查网络连接
docker exec aidemo ping mysql
```

### 3. 清理并重新启动

```bash
# 停止所有容器
docker-compose down

# 删除所有数据
docker-compose down -v

# 重新启动
docker-compose up -d
```

---

## 生产环境建议

1. **使用外部数据库**: 不要在容器中运行数据库，使用托管数据库服务
2. **安全密钥**: 不要在代码中硬编码密钥，使用环境变量或 Docker Secrets
3. **资源限制**: 在 docker-compose.yml 中设置内存和 CPU 限制
4. **日志管理**: 配置日志驱动程序处理日志，防止日志文件过大
5. **监控**: 使用 Prometheus + Grafana 监控应用性能

---

## 相关文件

- `Dockerfile`: 定义应用容器的构建过程
- `docker-compose.yml`: 定义多容器服务编排
- `.dockerignore`: 排除不需要打包的文件
- `application.properties`: 应用配置文件
