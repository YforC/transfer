# 携号转网双库级联修改 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让 `TelecomPro` 和 `UnicomPro` 都具备双向携号转网能力，并在转网成功后级联修改 `mobile` 与 `unicom` 两个库中的 `cell_phone_info.status`。

**Architecture:** 采用“目标运营商服务端主导双库修改”的方案。`UnicomPro` 负责处理转入联通，读取 `mobile` 作为源库并写入 `unicom` 作为目标库；`TelecomPro` 负责处理转入移动，读取 `unicom` 作为源库并写入 `mobile` 作为目标库。页面层只做客户端请求转发，双库状态变更放在服务端方法中完成。

**Tech Stack:** Spring Boot 2.7、Spring 3.x XML、CXF JAX-WS、JdbcTemplate、MySQL、JUnit

---

### Task 1: 为 UnicomPro 补双库仓储能力

**Files:**
- Modify: `D:/Desktop/Yangcheng/UnicomPro/src/main/resources/db.properties`
- Modify: `D:/Desktop/Yangcheng/UnicomPro/src/main/resources/spring/applicationContext-minimal.xml`
- Modify: `D:/Desktop/Yangcheng/UnicomPro/src/main/java/com/unicom/repository/CellPhoneInfoRepository.java`
- Create: `D:/Desktop/Yangcheng/UnicomPro/src/test/java/com/unicom/repository/CellPhoneInfoRepositoryTest.java`

- [ ] **Step 1: 写失败测试，定义仓储需要支持查找、插入、更新**

```java
public void testShouldFindInsertAndUpdateByPhoneNumber() {
    JdbcTemplate jdbcTemplate = createJdbcTemplate();
    jdbcTemplate.execute("create table cell_phone_info (id bigint auto_increment primary key, cell_phone_number varchar(20), remain_money decimal(10,2), order_desc varchar(100), status tinyint)");

    CellPhoneInfoRepository repository = new CellPhoneInfoRepository(jdbcTemplate);

    assertNull(repository.findByPhoneNumber("13300000000"));

    CellPhoneInfo created = new CellPhoneInfo();
    created.setCellPhoneNumber("13300000000");
    created.setRemainMoney(new BigDecimal("10.00"));
    created.setOrderDesc("A");
    created.setStatus(0);
    repository.insert(created);

    CellPhoneInfo loaded = repository.findByPhoneNumber("13300000000");
    assertEquals("13300000000", loaded.getCellPhoneNumber());

    created.setRemainMoney(new BigDecimal("20.00"));
    created.setOrderDesc("B");
    created.setStatus(1);
    repository.updateByPhoneNumber(created);

    CellPhoneInfo updated = repository.findByPhoneNumber("13300000000");
    assertEquals(new BigDecimal("20.00"), updated.getRemainMoney());
    assertEquals("B", updated.getOrderDesc());
    assertEquals(Integer.valueOf(1), updated.getStatus());
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=CellPhoneInfoRepositoryTest test`
Expected: FAIL，因为 `updateByPhoneNumber` 尚不存在，且测试依赖/实现未补齐。

- [ ] **Step 3: 最小实现仓储 update 能力与双数据源配置**

```java
public int updateByPhoneNumber(CellPhoneInfo info) {
    String sql = "update cell_phone_info set remain_money = ?, order_desc = ?, status = ? where cell_phone_number = ?";
    return jdbcTemplate.update(sql,
            info.getRemainMoney(),
            info.getOrderDesc(),
            info.getStatus(),
            info.getCellPhoneNumber());
}
```

```xml
<bean id="mobileDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="${mobile.database.driverClassName}"/>
    <property name="url" value="${mobile.database.url}"/>
    <property name="username" value="${mobile.database.username}"/>
    <property name="password" value="${mobile.database.password}"/>
</bean>

<bean id="unicomDataSource" class="org.springframework.jdbc.datasource.DriverManagerDataSource">
    <property name="driverClassName" value="${unicom.database.driverClassName}"/>
    <property name="url" value="${unicom.database.url}"/>
    <property name="username" value="${unicom.database.username}"/>
    <property name="password" value="${unicom.database.password}"/>
</bean>
```

- [ ] **Step 4: 运行仓储测试确认通过**

Run: `mvn -q -Dtest=CellPhoneInfoRepositoryTest test`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add UnicomPro/src/main/resources/db.properties UnicomPro/src/main/resources/spring/applicationContext-minimal.xml UnicomPro/src/main/java/com/unicom/repository/CellPhoneInfoRepository.java UnicomPro/src/test/java/com/unicom/repository/CellPhoneInfoRepositoryTest.java
git commit -m "test: add unicom repository dual-db support"
```

### Task 2: 实现 UnicomPro 转入联通服务端双库逻辑

**Files:**
- Modify: `D:/Desktop/Yangcheng/UnicomPro/src/main/java/com/unicom/service/impl/TransferNumberServiceImpl.java`
- Create: `D:/Desktop/Yangcheng/UnicomPro/src/test/java/com/unicom/service/impl/TransferNumberServiceImplTest.java`

- [ ] **Step 1: 写失败测试，覆盖转入联通成功场景**

```java
public void testShouldTransferFromMobileToUnicom() {
    InMemoryCellPhoneStore mobileStore = new InMemoryCellPhoneStore();
    InMemoryCellPhoneStore unicomStore = new InMemoryCellPhoneStore();
    mobileStore.save(record("13311111111", "10.00", "old", 0));

    TransferNumberServiceImpl service = new TransferNumberServiceImpl(mobileStore, unicomStore);
    TransferResponse response = service.transferNumber(request("13311111111", "12.50", "new plan", 0));

    assertTrue(response.isSuccess());
    assertEquals(Integer.valueOf(1), mobileStore.find("13311111111").getStatus());
    assertEquals(Integer.valueOf(0), unicomStore.find("13311111111").getStatus());
    assertEquals("new plan", unicomStore.find("13311111111").getOrderDesc());
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=TransferNumberServiceImplTest test`
Expected: FAIL，因为当前实现只做单库插入，且不支持构造注入双仓储。

- [ ] **Step 3: 最小实现“源库转出 + 目标库 upsert”**

```java
CellPhoneInfo source = sourceRepository.findByPhoneNumber(mobile);
if (source == null) {
    return fail("源运营商中不存在该号码。", "请确认号码当前归属后重试。");
}

source.setStatus(1);
sourceRepository.updateByPhoneNumber(source);

CellPhoneInfo target = targetRepository.findByPhoneNumber(mobile);
if (target == null) {
    CellPhoneInfo created = new CellPhoneInfo();
    created.setCellPhoneNumber(mobile);
    created.setRemainMoney(remainMoney);
    created.setOrderDesc(orderDesc);
    created.setStatus(0);
    targetRepository.insert(created);
} else {
    target.setRemainMoney(remainMoney);
    target.setOrderDesc(orderDesc);
    target.setStatus(0);
    targetRepository.updateByPhoneNumber(target);
}
```

- [ ] **Step 4: 增加失败测试并实现源库不存在校验**

```java
public void testShouldFailWhenSourceNumberMissing() {
    TransferResponse response = service.transferNumber(request("13399999999", "12.50", "new plan", 0));
    assertFalse(response.isSuccess());
    assertEquals("源运营商中不存在该号码。", response.getMessage());
}
```

- [ ] **Step 5: 运行服务测试确认通过**

Run: `mvn -q -Dtest=TransferNumberServiceImplTest test`
Expected: PASS

- [ ] **Step 6: 提交**

```bash
git add UnicomPro/src/main/java/com/unicom/service/impl/TransferNumberServiceImpl.java UnicomPro/src/test/java/com/unicom/service/impl/TransferNumberServiceImplTest.java
git commit -m "feat: implement unicom inbound transfer service"
```

### Task 3: 为 TelecomPro 补数据库和服务端能力

**Files:**
- Modify: `D:/Desktop/Yangcheng/TelecomPro/pom.xml`
- Modify: `D:/Desktop/Yangcheng/TelecomPro/src/main/resources/application.properties`
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/config/DataSourceConfig.java`
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/repository/CellPhoneInfoRepository.java`
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/service/TransferNumberService.java`
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/service/impl/TransferNumberServiceImpl.java`
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/test/java/com/telecom/service/impl/TransferNumberServiceImplTest.java`

- [ ] **Step 1: 写失败测试，覆盖转入移动成功场景**

```java
@Test
void shouldTransferFromUnicomToMobile() {
    InMemoryCellPhoneStore unicomStore = new InMemoryCellPhoneStore();
    InMemoryCellPhoneStore mobileStore = new InMemoryCellPhoneStore();
    unicomStore.save(record("13312345678", "10.00", "old", 0));

    TransferNumberServiceImpl service = new TransferNumberServiceImpl(unicomStore, mobileStore);
    TransferResponse response = service.transferNumber(request("13312345678", "19.00", "mobile-plan", 0));

    assertThat(response.isSuccess()).isTrue();
    assertThat(unicomStore.find("13312345678").getStatus()).isEqualTo(1);
    assertThat(mobileStore.find("13312345678").getStatus()).isEqualTo(0);
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=TransferNumberServiceImplTest test`
Expected: FAIL，因为 `TelecomPro` 还没有本地服务端与仓储。

- [ ] **Step 3: 最小实现双数据源、仓储与对称服务端**

```java
@Bean("mobileJdbcTemplate")
public JdbcTemplate mobileJdbcTemplate(@Qualifier("mobileDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}

@Bean("unicomJdbcTemplate")
public JdbcTemplate unicomJdbcTemplate(@Qualifier("unicomDataSource") DataSource dataSource) {
    return new JdbcTemplate(dataSource);
}
```

```java
@WebService(
        serviceName = "TransferSupportService",
        endpointInterface = "com.telecom.service.TransferNumberService",
        targetNamespace = "http://service.telecom.com/")
public class TransferNumberServiceImpl implements TransferNumberService {
    // 对称实现：源库 unicom -> 目标库 mobile
}
```

- [ ] **Step 4: 运行服务测试确认通过**

Run: `mvn -q -Dtest=TransferNumberServiceImplTest test`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add TelecomPro/pom.xml TelecomPro/src/main/resources/application.properties TelecomPro/src/main/java/com/telecom/config/DataSourceConfig.java TelecomPro/src/main/java/com/telecom/repository/CellPhoneInfoRepository.java TelecomPro/src/main/java/com/telecom/service/TransferNumberService.java TelecomPro/src/main/java/com/telecom/service/impl/TransferNumberServiceImpl.java TelecomPro/src/test/java/com/telecom/service/impl/TransferNumberServiceImplTest.java
git commit -m "feat: add telecom inbound transfer service"
```

### Task 4: 补双向客户端入口和页面请求转发

**Files:**
- Modify: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/service/PhoneLookupGatewayService.java`
- Modify: `D:/Desktop/Yangcheng/TelecomPro/src/main/java/com/telecom/controller/TransferController.java`
- Modify: `D:/Desktop/Yangcheng/TelecomPro/src/main/resources/templates/phone-transfer-index.html`
- Create or Modify: `D:/Desktop/Yangcheng/UnicomPro/src/main/java/com/unicom/...` 与 `D:/Desktop/Yangcheng/UnicomPro/src/main/webapp/...` 中的本地入口文件
- Create: `D:/Desktop/Yangcheng/TelecomPro/src/test/java/com/telecom/controller/TransferControllerTest.java`

- [ ] **Step 1: 写失败测试，验证页面可区分转入联通与转入移动**

```java
@Test
void shouldCallUnicomGatewayWhenTargetCarrierIsUnicom() throws Exception {
    mockMvc.perform(post("/transfer/transferNumber")
            .param("cellPhoneNumber", "13311111111")
            .param("remainMoney", "10.00")
            .param("orderDesc", "plan")
            .param("targetCarrier", "UNICOM"))
            .andExpect(status().isOk());

    verify(phoneLookupGatewayService).submitToUnicom(any(CellPhoneInfo.class));
}
```

- [ ] **Step 2: 运行测试确认失败**

Run: `mvn -q -Dtest=TransferControllerTest test`
Expected: FAIL，因为当前控制器只有单一提交入口。

- [ ] **Step 3: 最小实现双向入口**

```java
if ("MOBILE".equalsIgnoreCase(cellPhoneInfo.getTargetCarrier())) {
    response = phoneLookupGatewayService.submitToMobile(cellPhoneInfo);
} else {
    response = phoneLookupGatewayService.submitToUnicom(cellPhoneInfo);
}
```

```html
<select id="targetCarrier" name="targetCarrier" th:field="*{targetCarrier}">
    <option value="UNICOM">转入联通</option>
    <option value="MOBILE">转入移动</option>
</select>
```

- [ ] **Step 4: 运行控制器测试确认通过**

Run: `mvn -q -Dtest=TransferControllerTest test`
Expected: PASS

- [ ] **Step 5: 提交**

```bash
git add TelecomPro/src/main/java/com/telecom/service/PhoneLookupGatewayService.java TelecomPro/src/main/java/com/telecom/controller/TransferController.java TelecomPro/src/main/resources/templates/phone-transfer-index.html TelecomPro/src/test/java/com/telecom/controller/TransferControllerTest.java
git commit -m "feat: add bidirectional transfer client flows"
```

### Task 5: 整体验证与清理

**Files:**
- Verify only: `D:/Desktop/Yangcheng/TelecomPro`
- Verify only: `D:/Desktop/Yangcheng/UnicomPro`

- [ ] **Step 1: 运行 UnicomPro 测试**

Run: `mvn -q test`
Expected: PASS

- [ ] **Step 2: 运行 TelecomPro 测试**

Run: `mvn -q test`
Expected: PASS

- [ ] **Step 3: 编译 UnicomPro**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS

- [ ] **Step 4: 编译 TelecomPro**

Run: `mvn -q -DskipTests compile`
Expected: BUILD SUCCESS

- [ ] **Step 5: 检查设计要求覆盖**

```text
- UnicomPro 已提供服务端与客户端入口
- TelecomPro 已提供服务端与客户端入口
- mobile/unicom 双库都参与更新
- status 规则符合：源库 1，目标库 0
- 目标库缺记录时新增，已有记录时更新
```

- [ ] **Step 6: 提交**

```bash
git add .
git commit -m "feat: complete carrier porting dual-database flow"
```
