# 携号转网双库级联修改设计

## 背景

当前项目包含两个独立系统：

- `UnicomPro`：Spring + JSP + CXF + JDBC，当前已发布 `TransferSupportService`
- `TelecomPro`：Spring Boot + Thymeleaf + CXF 客户端，当前只负责页面提交和调用 `UnicomPro`

现状与 `goat.txt` 的差异在于：现有实现只是把表单数据插入单个库，并没有体现“运营商携号转网后双库状态级联修改”的业务。当前数据库已调整为两个库：

- `mobile`
- `unicom`

两个库都包含 `cell_phone_info` 表。业务规则已经确认：

- `status = 0`：该号码当前属于本运营商
- `status = 1`：该号码已从本运营商转出
- 转网成功时：
  - 源运营商库对应号码更新为 `status = 1`
  - 目标运营商库对应号码“有则更新，无则新增”，并设置为 `status = 0`

## 目标

按 `goat.txt` 实现双向携号转网：

1. `TelecomPro -> UnicomPro`：表示号码转入联通
2. `UnicomPro -> TelecomPro`：表示号码转入移动

每个系统都需要：

- 提供自己的 CXF 服务端接口
- 提供调用对方服务的客户端入口
- 在服务端执行业务成功后的双库级联修改

## 推荐方案

采用“目标运营商服务端主导双库修改”的方案。

含义如下：

- 当号码转入联通时，由 `UnicomPro` 服务端完成整个转网业务，并同时修改 `mobile` 与 `unicom`
- 当号码转入移动时，由 `TelecomPro` 服务端完成整个转网业务，并同时修改 `unicom` 与 `mobile`

这样最符合 `goat.txt` 中“开发 A/B 系统的携号转网业务并发布 wsdl 地址”的要求。页面层只负责发起请求，数据库一致性由服务端业务层保证。

## 系统职责

### 1. UnicomPro

保留并增强现有 `TransferSupportService`，其语义改为“转入联通”。

请求进入后执行：

1. 校验手机号、余额、套餐说明
2. 查询 `mobile.cell_phone_info`
3. 如果源库不存在该号码，返回失败
4. 将 `mobile.cell_phone_info.status` 更新为 `1`
5. 查询 `unicom.cell_phone_info`
6. 如果目标库存在该号码，则更新余额、套餐说明、状态为 `0`
7. 如果目标库不存在该号码，则插入新记录，状态为 `0`
8. 返回转网成功结果

同时保留现有 JSP 页面作为本地入口，并新增或调整一个客户端入口，使其能调用 `TelecomPro` 的 wsdl 完成“转入移动”。

### 2. TelecomPro

当前 `TelecomPro` 只有页面和 CXF 客户端，需要补齐服务端能力。

需要新增：

1. 本地数据库访问层
2. 指向 `mobile` 与 `unicom` 的双数据源配置
3. 与 `UnicomPro` 对称的 CXF 服务端接口，语义为“转入移动”

请求进入后执行：

1. 校验手机号、余额、套餐说明
2. 查询 `unicom.cell_phone_info`
3. 如果源库不存在该号码，返回失败
4. 将 `unicom.cell_phone_info.status` 更新为 `1`
5. 查询 `mobile.cell_phone_info`
6. 如果目标库存在该号码，则更新余额、套餐说明、状态为 `0`
7. 如果目标库不存在该号码，则插入新记录，状态为 `0`
8. 返回转网成功结果

当前 Thymeleaf 页面保留为本地入口，并继续作为调用对方服务的客户端界面。

## 代码结构设计

### UnicomPro

预计修改：

- `src/main/resources/db.properties`
  - 从单库 `transfer` 改为显式配置 `mobile` 和 `unicom` 两个数据源
- `src/main/resources/spring/applicationContext-minimal.xml`
  - 注册两个 `DataSource`
  - 注册两个 `JdbcTemplate`
  - 为仓储层注入源库与目标库访问对象
- `src/main/java/com/unicom/repository/CellPhoneInfoRepository.java`
  - 从“单库单模板”改为可复用仓储，支持按号码查询、插入、更新
- `src/main/java/com/unicom/service/impl/TransferNumberServiceImpl.java`
  - 从“只插入一条记录”改为“源库转出 + 目标库 upsert”
- 新增一个面向 `TelecomPro` 的客户端调用类或控制入口
  - 实现“从联通转入移动”的请求转发

### TelecomPro

预计新增或修改：

- `pom.xml`
  - 增加 JDBC/MySQL 依赖
- `src/main/resources/application.properties`
  - 增加 `mobile` 与 `unicom` 数据源配置
  - 增加对方服务地址配置
- 新增数据访问配置类
  - 提供双数据源与双 `JdbcTemplate`
- 新增本地仓储类
  - 负责 `cell_phone_info` 的查询、更新、插入
- 新增 CXF 服务端接口与实现
  - 提供“转入移动”服务
- 修改现有页面控制器与网关服务
  - 区分“转入联通”和“转入移动”两个入口

## 数据流

### A. 转入联通

1. 用户在 `TelecomPro` 页面提交号码资料
2. `TelecomPro` 客户端调用 `UnicomPro` 的 `TransferSupportService`
3. `UnicomPro` 服务端校验请求并读取 `mobile` 源库
4. `UnicomPro` 更新 `mobile.status = 1`
5. `UnicomPro` 在 `unicom` 执行 upsert，目标状态写为 `0`
6. `UnicomPro` 返回结果
7. `TelecomPro` 展示结果

### B. 转入移动

1. 用户在 `UnicomPro` 页面提交号码资料
2. `UnicomPro` 客户端调用 `TelecomPro` 的新 wsdl 服务
3. `TelecomPro` 服务端校验请求并读取 `unicom` 源库
4. `TelecomPro` 更新 `unicom.status = 1`
5. `TelecomPro` 在 `mobile` 执行 upsert，目标状态写为 `0`
6. `TelecomPro` 返回结果
7. `UnicomPro` 展示结果

## 错误处理

失败场景统一返回明确业务消息，不抛给页面原始异常：

- 手机号格式不正确
- 余额为空或为负数
- 套餐说明为空
- 源运营商库不存在该号码
- 数据库更新失败
- 对方 wsdl 服务不可达

如果源库状态更新成功、目标库写入失败，会出现两库不一致风险。由于当前项目是教学作业规模，优先使用“同服务双数据源顺序更新”实现。实现时应尽量将两次数据库操作放在同一个服务方法内，并在异常消息中明确说明失败位置。若框架条件允许，再追加本地事务管理。

## 测试设计

测试至少覆盖以下行为：

1. 转入联通成功
   - `mobile` 源号码变为 `1`
   - `unicom` 目标号码存在且为 `0`
2. 转入联通时目标库无记录
   - 应新增
3. 转入联通时目标库已有记录
   - 应更新
4. 转入移动成功
   - `unicom` 源号码变为 `1`
   - `mobile` 目标号码存在且为 `0`
5. 源库号码不存在
   - 返回失败且不改目标库
6. 非法手机号/空套餐/非法余额
   - 返回失败

其中服务层测试优先，页面层只做最小回归验证。

## 范围边界

本次只处理 `cell_phone_info` 的携号转网业务，不扩展额外套餐表、审计表或异步补偿机制。

不做的内容：

- 不引入消息队列
- 不做分布式事务
- 不重构无关页面样式
- 不扩展多运营商之外的第三类运营商

## 实施顺序

1. 先改 `UnicomPro` 服务端为真实双库转网逻辑
2. 再给 `TelecomPro` 补双数据源和对称服务端
3. 最后补两端页面/客户端入口和测试
