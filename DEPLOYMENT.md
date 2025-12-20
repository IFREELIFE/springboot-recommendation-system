# 部署指南 (Deployment Guide)

本文档提供 Spring Boot 民宿推荐系统的完整部署指南。

---

## 系统要求 (System Requirements)

### 必需组件 (Required Components)
- **JDK**: 11 或更高版本
- **Maven**: 3.6+ 
- **MySQL**: 8.0+
- **Git**: 用于克隆代码库

### 可选组件 (Optional Components)
- **Redis**: 6.0+ (用于缓存)
- **Elasticsearch**: 7.x+ (用于全文搜索)

---

## 部署步骤 (Deployment Steps)

### 1. 克隆项目 (Clone Repository)

```bash
git clone https://github.com/IFREELIFE/springboot-recommendation-system.git
cd springboot-recommendation-system
```

---

### 2. 配置 MySQL 数据库 (Configure MySQL Database)

#### 2.1 创建数据库 (Create Database)

```bash
# 登录 MySQL
mysql -u root -p

# 执行 SQL 脚本
mysql> source sql/schema.sql
mysql> source sql/sample_data.sql
```

或者使用命令行直接执行:

```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/sample_data.sql
```

#### 2.2 验证数据库 (Verify Database)

```sql
USE homestay_recommendation;
SHOW TABLES;

-- 应该看到以下表:
-- users
-- properties
-- orders
-- user_property_interactions
```

---

### 3. 配置应用程序 (Configure Application)

编辑 `src/main/resources/application.properties`:

```properties
# MySQL 配置
spring.datasource.url=jdbc:mysql://localhost:3306/homestay_recommendation?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_mysql_password

# Redis 配置 (如果已安装)
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=your_redis_password

# Elasticsearch 配置 (如果已安装)
spring.elasticsearch.rest.uris=http://localhost:9200

# JWT 配置 (可以自定义密钥)
jwt.secret=your_custom_secret_key_here_minimum_256_bits
jwt.expiration=86400000
```

---

### 4. 构建项目 (Build Project)

```bash
# 清理并编译
mvn clean compile

# 运行测试 (可选)
mvn test

# 打包成 JAR
mvn clean package -DskipTests
```

构建完成后，会在 `target/` 目录下生成 `homestay-recommendation-1.0.0.jar` 文件。

---

### 5. 运行应用程序 (Run Application)

#### 方式 1: 使用 Maven 运行 (Development)

```bash
mvn spring-boot:run
```

#### 方式 2: 使用 JAR 文件运行 (Production)

```bash
java -jar target/homestay-recommendation-1.0.0.jar
```

#### 方式 3: 后台运行 (Background)

```bash
nohup java -jar target/homestay-recommendation-1.0.0.jar > application.log 2>&1 &
```

#### 方式 4: 使用自定义配置文件

```bash
java -jar target/homestay-recommendation-1.0.0.jar --spring.config.location=file:./config/application.properties
```

---

### 6. 验证部署 (Verify Deployment)

#### 6.1 检查应用程序状态

应用程序启动后，应该在日志中看到:

```
Started HomestayRecommendationApplication in X.XXX seconds
```

#### 6.2 测试 API 端点

```bash
# 测试注册接口
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123",
    "email": "test@example.com",
    "role": "USER"
  }'

# 测试登录接口
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 测试获取房源列表
curl http://localhost:8080/api/properties
```

---

## 生产环境部署 (Production Deployment)

### 1. 使用 Systemd 服务 (Linux)

创建服务文件 `/etc/systemd/system/homestay-recommendation.service`:

```ini
[Unit]
Description=Homestay Recommendation System
After=syslog.target network.target

[Service]
User=appuser
Group=appuser
ExecStart=/usr/bin/java -jar /opt/homestay-recommendation/homestay-recommendation-1.0.0.jar
SuccessExitStatus=143
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

启动服务:

```bash
sudo systemctl daemon-reload
sudo systemctl enable homestay-recommendation
sudo systemctl start homestay-recommendation
sudo systemctl status homestay-recommendation
```

---

### 2. 使用 Docker 部署

创建 `Dockerfile`:

```dockerfile
FROM openjdk:11-jre-slim
WORKDIR /app
COPY target/homestay-recommendation-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

构建并运行:

```bash
# 构建镜像
docker build -t homestay-recommendation:1.0.0 .

# 运行容器
docker run -d \
  --name homestay-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/homestay_recommendation \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=password \
  homestay-recommendation:1.0.0
```

---

### 3. 使用 Docker Compose

创建 `docker-compose.yml`:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: homestay_recommendation
    ports:
      - "3306:3306"
    volumes:
      - ./sql:/docker-entrypoint-initdb.d
      - mysql-data:/var/lib/mysql

  redis:
    image: redis:6.0
    ports:
      - "6379:6379"

  elasticsearch:
    image: elasticsearch:7.17.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  app:
    build: .
    ports:
      - "8080:8080"
    depends_on:
      - mysql
      - redis
      - elasticsearch
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/homestay_recommendation
      SPRING_DATASOURCE_USERNAME: root
      SPRING_DATASOURCE_PASSWORD: root
      SPRING_REDIS_HOST: redis
      SPRING_ELASTICSEARCH_REST_URIS: http://elasticsearch:9200

volumes:
  mysql-data:
```

运行:

```bash
docker-compose up -d
```

---

## 配置 Nginx 反向代理 (Optional)

创建 Nginx 配置 `/etc/nginx/sites-available/homestay-recommendation`:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用配置:

```bash
sudo ln -s /etc/nginx/sites-available/homestay-recommendation /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

---

## 性能优化建议 (Performance Optimization)

### 1. JVM 参数调优

```bash
java -jar \
  -Xms512m \
  -Xmx2048m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  target/homestay-recommendation-1.0.0.jar
```

### 2. 数据库连接池配置

在 `application.properties` 中添加:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000
```

### 3. Redis 缓存配置

```properties
spring.cache.redis.time-to-live=3600000
spring.redis.lettuce.pool.max-active=20
spring.redis.lettuce.pool.max-idle=10
spring.redis.lettuce.pool.min-idle=5
```

---

## 监控和日志 (Monitoring and Logging)

### 1. 日志配置

在 `application.properties` 中:

```properties
logging.level.root=INFO
logging.level.com.recommendation.homestay=DEBUG
logging.file.name=logs/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### 2. 查看日志

```bash
# 实时查看日志
tail -f logs/application.log

# 查看最后100行
tail -n 100 logs/application.log

# 搜索错误日志
grep ERROR logs/application.log
```

---

## 故障排除 (Troubleshooting)

### 问题 1: 应用启动失败

**症状**: 应用无法启动，抛出异常

**解决方法**:
1. 检查 MySQL 是否正在运行
2. 验证数据库连接配置
3. 确认端口 8080 未被占用
4. 查看详细错误日志

### 问题 2: 数据库连接失败

**症状**: `Could not connect to database`

**解决方法**:
```bash
# 测试 MySQL 连接
mysql -h localhost -u root -p

# 检查 MySQL 是否运行
sudo systemctl status mysql

# 验证防火墙规则
sudo ufw allow 3306
```

### 问题 3: Redis 连接失败

**症状**: `Cannot connect to Redis`

**解决方法**:
- 如果没有安装 Redis，在 `application.properties` 中禁用 Redis:
```properties
spring.cache.type=none
```

### 问题 4: JWT Token 验证失败

**症状**: `Invalid JWT token`

**解决方法**:
- 确保 JWT secret 长度足够（至少 256 位）
- 检查 token 是否过期
- 验证请求头格式: `Authorization: Bearer {token}`

---

## 安全建议 (Security Recommendations)

1. **更改默认密码**: 修改示例数据中的默认用户密码
2. **使用 HTTPS**: 在生产环境配置 SSL/TLS
3. **定期更新**: 保持依赖库为最新版本
4. **数据库备份**: 定期备份 MySQL 数据库
5. **访问控制**: 配置防火墙规则限制访问

---

## 备份和恢复 (Backup and Recovery)

### 备份数据库

```bash
# 备份数据库
mysqldump -u root -p homestay_recommendation > backup_$(date +%Y%m%d).sql

# 恢复数据库
mysql -u root -p homestay_recommendation < backup_20240101.sql
```

---

## 联系支持 (Support)

如有问题，请提交 GitHub Issue:
https://github.com/IFREELIFE/springboot-recommendation-system/issues
