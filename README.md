# transfer

一个基于 SOAP WebService 的双系统携号转网示例项目。

仓库包含两个独立的 Java Web 系统，分别模拟移动和联通两侧业务。两个系统既能发布 WSDL 服务，也能作为客户端调用对方服务，完成“携号转网”场景下的双向业务协作和双库状态同步。

## 项目概览

- `TelecomPro`
  - 技术栈：Spring Boot、Spring MVC、Thymeleaf、CXF、JdbcTemplate、MySQL
  - 页面角色：只能发起“转入联通”
  - 服务角色：对外提供“转入移动”SOAP 服务
- `UnicomPro`
  - 技术栈：Spring、Struts2、Hibernate、JSP、CXF、MySQL
  - 页面角色：只能发起“转入移动”
  - 服务角色：对外提供“转入联通”SOAP 服务

## 系统调用关系

### 转入联通

1. 用户访问 `TelecomPro`
2. 页面只输入手机号
3. `TelecomPro` 作为客户端调用 `UnicomPro` 的 SOAP 服务
4. `UnicomPro` 在服务端执行转网逻辑
5. 转网成功后同时更新 `mobile` 和 `unicom` 数据库

### 转入移动

1. 用户访问 `UnicomPro`
2. 页面只输入手机号
3. `UnicomPro` 作为客户端调用 `TelecomPro` 的 SOAP 服务
4. `TelecomPro` 在服务端执行转网逻辑
5. 转网成功后同时更新 `unicom` 和 `mobile` 数据库

## 业务规则

两个数据库 `mobile`、`unicom` 中都包含 `cell_phone_info` 表。

`cell_phone_info.status` 定义如下：

- `0`：当前属于本运营商
- `1`：已从本运营商转出

转网成功时：

1. 源运营商库中的该号码更新为 `status = 1`
2. 目标运营商库中的该号码执行 upsert
3. 目标运营商库最终状态写为 `status = 0`
4. 页面端不输入余额和套餐，服务端按手机号从源库读取并同步

## 仓库结构

```text
.
├─ TelecomPro/   # 移动侧系统
├─ UnicomPro/    # 联通侧系统
├─ mobile.sql    # mobile 库初始化脚本
├─ unicom.sql    # unicom 库初始化脚本
└─ docs/         # 设计文档与实现计划
```

## 环境要求

- JDK 8
- Maven 3.8+
- MySQL 8.x

## 快速开始

### 1. 创建数据库

```sql
create database mobile default character set utf8mb4;
create database unicom default character set utf8mb4;
```

### 2. 导入初始化脚本

```bash
mysql -uroot -p mobile < mobile.sql
mysql -uroot -p unicom < unicom.sql
```

默认数据库配置：

- 用户名：`root`
- 密码：`123456`

如需修改，请调整：

- `TelecomPro/src/main/resources/application.properties`
- `UnicomPro/src/main/resources/db.properties`

### 3. 启动 `UnicomPro`

```bash
cd UnicomPro
mvn tomcat7:run
```

默认地址：

- 页面：`http://localhost:8080/UnicomPro/home.jsp`
- WSDL：`http://localhost:8080/UnicomPro/services/TransferSupportService?wsdl`

### 4. 启动 `TelecomPro`

```bash
cd TelecomPro
mvn spring-boot:run
```

默认地址：

- 页面：`http://localhost:8081/`
- WSDL：`http://localhost:8081/services/TransferSupportService?wsdl`

## 页面使用

### `TelecomPro`

- 访问首页后输入手机号
- 点击提交后固定发起“转入联通”
- 结果页展示成功或失败状态、手机号和下一步提示

### `UnicomPro`

- 访问首页后输入手机号
- 点击提交后固定发起“转入移动”
- 页面下方展示统一风格的结果卡片

## 测试与验证

### `UnicomPro`

```bash
cd UnicomPro
mvn test
mvn -DskipTests compile
```

### `TelecomPro`

```bash
cd TelecomPro
mvn test
mvn -DskipTests compile
```

## 主要实现点

- 双向 SOAP 服务发布与调用
- 双系统客户端 / 服务端双角色结构
- 双数据库级联修改
- 手机号单字段输入，服务端自动同步套餐资料
- `TelecomPro` 双数据源配置修复
- 双侧服务层与控制层测试

## 常见说明

- 本项目以课程作业实现为主，重点是 SOAP 调用链路和双库状态同步
- 当前没有引入分布式事务、消息补偿或消息队列
- 如果只启动一个系统，对应的跨系统调用会失败，这是预期行为
