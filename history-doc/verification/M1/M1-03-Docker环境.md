# M1-03 - Docker Compose 环境

> 对应任务: [01-迭代计划.md](../../plan/01-迭代计划.md#m1-03-docker-compose-环境)
> 涉及 Spec: [03-数据库设计.md](../../spec/03-数据库设计.md), [07-部署配置.md](../../spec/07-部署配置.md)

## 验证目标

Docker Compose 环境（PostgreSQL + Redis）可正常启动。

## 测试用例

### TC-01: docker-compose.yml 验证

**前置条件：**
- Docker 已安装
- Docker Compose 已安装

**测试步骤：**
1. 检查 `backend/docker-compose.yml` 或项目根目录 `docker-compose.yml` 存在
2. 检查配置包含 PostgreSQL 和 Redis 服务

**预期结果：**
- 文件存在
- 包含 postgres 和 redis 服务定义

### TC-02: 环境启动验证

**前置条件：**
- TC-01 已通过

**测试步骤：**
1. 在 docker-compose.yml 目录执行 `docker-compose up -d`
2. 等待服务启动
3. 执行 `docker-compose ps`

**预期结果：**
- PostgreSQL 容器运行中
- Redis 容器运行中

### TC-03: PostgreSQL 连接验证

**前置条件：**
- TC-02 已通过

**测试步骤：**
1. 执行 `docker-compose exec postgres psql -U postgres -c "SELECT version();"`

**预期结果：**
- 返回 PostgreSQL 版本信息

### TC-04: Redis 连接验证

**前置条件：**
- TC-02 已通过

**测试步骤：**
1. 执行 `docker-compose exec redis redis-cli ping`

**预期结果：**
- 返回 PONG

### TC-05: 数据持久化验证

**前置条件：**
- TC-02 已通过

**测试步骤：**
1. 创建测试数据库 `docker-compose exec postgres psql -U postgres -c "CREATE DATABASE test_db;"`
2. 执行 `docker-compose down`
3. 执行 `docker-compose up -d`
4. 验证数据库存在

**预期结果：**
- 数据持久化正常
- 数据库保留

## 验收标准

- [ ] TC-01: docker-compose.yml 验证通过
- [ ] TC-02: 环境启动验证通过
- [ ] TC-03: PostgreSQL 连接验证通过
- [ ] TC-04: Redis 连接验证通过
- [ ] TC-05: 数据持久化验证通过
- [ ] 所有测试用例通过