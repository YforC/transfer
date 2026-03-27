# transfer

基于“携号转网”场景的双系统 Java Web 课程项目。

仓库内包含两个独立系统：

- `UnicomPro`：Spring + JSP + CXF + JDBC + MySQL
- `TelecomPro`：Spring Boot + Thymeleaf + CXF + JdbcTemplate + MySQL

两个系统通过 SOAP WebService 互相调用，模拟号码在不同运营商之间转入、转出的业务流程。

## 项目目标

本项目按照 `goat.txt` 的要求实现了以下内容：

- 两个独立 Web 系统
- 公用手机号套餐信息实体
- 双向 WSDL 服务发布
- 双向客户端请求转发
- 携号转网成功后级联修改 `mobile` / `unicom` 两个数据库中的 `cell_phone_info`

## 业务规则

`cell_phone_info.status` 的定义：

- `0`：当前运营商号码
- `1`：已转出号码

转网成功时：

1. 源运营商库中该号码更新为 `status = 1`
2. 目标运营商库中该号码：
   - 存在则更新
   - 不存在则新增
   - 最终状态设为 `status = 0`

## 仓库结构

```text
.
├─ TelecomPro/     # Spring Boot 侧，支持页面提交、调用联通服务、提供移动侧服务
├─ UnicomPro/      # Spring MVC/JSP 侧，支持页面提交、调用移动服务、提供联通侧服务
├─ mobile.sql      # mobile 数据库表结构和示例数据
├─ unicom.sql      # unicom 数据库表结构和示例数据
├─ goat.txt        # 题目要求
└─ docs/           # 本次设计文档与实现计划
```

## 环境要求

- JDK 8
- Maven 3.8+
- MySQL 8.x

## 数据库准备

先创建两个数据库：

```sql
create database mobile default character set utf8mb4;
create database unicom default character set utf8mb4;
```

然后分别导入脚本：

```bash
mysql -uroot -p mobile < mobile.sql
mysql -uroot -p unicom < unicom.sql
```

默认配置中使用的是：

- 用户名：`root`
- 密码：`123456`

如需修改：

- `TelecomPro/src/main/resources/application.properties`
- `UnicomPro/src/main/resources/db.properties`

## 系统端口与接口

### UnicomPro

- 运行端口：`8080`
- 页面入口：`http://localhost:8080/UnicomPro/home.jsp`
- WSDL：`http://localhost:8080/UnicomPro/services/TransferSupportService?wsdl`

### TelecomPro

- 运行端口：`8081`
- 页面入口：`http://localhost:8081/`
- WSDL：`http://localhost:8081/services/TransferSupportService?wsdl`

## 启动方式

建议先启动 `UnicomPro`，再启动 `TelecomPro`。

### 1. 启动 UnicomPro

在 `UnicomPro` 目录执行：

```bash
mvn tomcat7:run
```

### 2. 启动 TelecomPro

在 `TelecomPro` 目录执行：

```bash
mvn spring-boot:run
```

## 页面使用方式

### 转入联通

1. 打开 `TelecomPro` 首页
2. 目标运营商选择“转入联通”
3. 提交手机号、余额、套餐说明、当前状态
4. `TelecomPro` 调用 `UnicomPro` 的 SOAP 服务
5. `UnicomPro` 完成：
   - `mobile` 源库转出
   - `unicom` 目标库更新或新增

### 转入移动

1. 打开 `UnicomPro` 首页
2. 在“转入移动”表单中填写信息
3. `UnicomPro` 调用 `TelecomPro` 的 SOAP 服务
4. `TelecomPro` 完成：
   - `unicom` 源库转出
   - `mobile` 目标库更新或新增

## 测试

### UnicomPro

```bash
cd UnicomPro
mvn test
```

### TelecomPro

```bash
cd TelecomPro
mvn test
```

## 编译验证

### UnicomPro

```bash
cd UnicomPro
mvn -DskipTests compile
```

### TelecomPro

```bash
cd TelecomPro
mvn -DskipTests compile
```

## 主要实现点

- `UnicomPro` 服务端实现真实双库转网逻辑
- `TelecomPro` 从纯客户端扩展为“客户端 + 服务端”
- 双向 SOAP DTO、接口与调用网关
- 双数据源配置
- 仓储层按手机号查找、插入、更新
- 服务层测试与控制器测试

## 说明

本项目当前以教学作业实现为主，重点放在：

- 双系统互调
- WSDL 发布
- 双库状态级联修改

未引入分布式事务、消息队列或补偿机制。
