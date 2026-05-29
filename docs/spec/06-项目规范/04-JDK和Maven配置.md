# 后端 pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>personal-website</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>personal-website</name>
    <description>个人网站系统</description>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.5</version>
        <relativePath/>
    </parent>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis.version>3.5.16</mybatis.version>
        <mybatis-spring.version>3.0.3</mybatis-spring.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Web -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Spring Boot Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Spring Boot Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- PostgreSQL -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- MyBatis -->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>${mybatis.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>${mybatis-spring.version}</version>
        </dependency>

        <!-- JWT -->
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Spring Boot Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## JDK 本地配置

由于 JDK 未配置全局环境变量，需在项目中指定：

### 方式 1: Maven 配置文件 (推荐)

在 `backend/` 目录下创建 `.mvn/jvm.config`：

```bash
backend/.mvn/jvm.config
```

内容：
```
-Djava.home=D:/CodeProgram/jdk-21.0.1
```

### 方式 2: IDEA 配置

在 IDEA 中设置 Project SDK：
1. File → Project Structure → Project
2. SDK 选择 "Add SDK" → "JDK"
3. 选择路径 `D:\CodeProgram\jdk-21.0.1`

### 方式 3: 命令行指定

```bash
cd backend
set JAVA_HOME=D:\CodeProgram\jdk-21.0.1
mvn spring-boot:run
```

Windows PowerShell:
```powershell
$env:JAVA_HOME="D:\CodeProgram\jdk-21.0.1"
mvn spring-boot:run
```

---

## application.yml 配置示例

```yaml
spring:
  application:
    name: personal-website

  datasource:
    url: jdbc:postgresql://localhost:5432/personal_website
    username: postgres
    password: ${DB_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver

  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      upload-dir: ${user.dir}/uploads

server:
  port: 8080

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.personalwebsite.model.entity
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-change-in-production}
  access-token-expiration: 900000    # 15 分钟
  refresh-token-expiration: 604800000 # 7 天

logging:
  level:
    com.example.personalwebsite: INFO
```
