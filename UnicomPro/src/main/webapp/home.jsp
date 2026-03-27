<%@ page language="java" pageEncoding="UTF-8" import="org.springframework.web.context.WebApplicationContext,org.springframework.web.context.support.WebApplicationContextUtils,com.unicom.service.TelecomTransferGatewayService,com.unicom.client.telecom.TransferResponse" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
TransferResponse telecomResult = null;
String submitError = null;
if ("POST".equalsIgnoreCase(request.getMethod()) && "telecom".equals(request.getParameter("action"))) {
    try {
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(application);
        TelecomTransferGatewayService gateway = (TelecomTransferGatewayService) context.getBean("telecomTransferGatewayService");
        telecomResult = gateway.submit(
                request.getParameter("cellPhoneNumber"),
                request.getParameter("remainMoney"),
                request.getParameter("orderDesc"),
                request.getParameter("status"));
        request.setAttribute("telecomServiceAddress", gateway.getServiceAddress());
    } catch (Exception ex) {
        submitError = ex.getMessage();
    }
}
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>联通携号转网服务</title>
  <style>
    body { font-family: "Microsoft YaHei", Arial, sans-serif; background: #f5f7fb; margin: 0; padding: 40px 20px; }
    .card { max-width: 760px; margin: 0 auto; background: #ffffff; border-radius: 12px; box-shadow: 0 8px 24px rgba(15, 23, 42, 0.08); padding: 32px; }
    h1 { margin-top: 0; color: #1f3c88; }
    .block { margin-top: 18px; line-height: 1.7; }
    code { background: #eef2ff; padding: 2px 6px; border-radius: 4px; }
    .form-group { margin-bottom: 16px; }
    label { display: block; font-weight: 600; margin-bottom: 6px; }
    input, select { width: 100%; padding: 10px 12px; box-sizing: border-box; border: 1px solid #d0d8e7; border-radius: 8px; }
    button { width: 100%; padding: 12px; border: none; border-radius: 8px; background: #1368ce; color: white; cursor: pointer; }
    .result { margin-top: 20px; padding: 14px; border-radius: 10px; }
    .success { background: #e8f7ec; color: #166534; }
    .error { background: #fef2f2; color: #991b1b; }
  </style>
</head>
<body>
<div class="card">
  <h1>联通侧携号转网服务</h1>
  <div class="block">
    联通服务 WSDL：<code><%=basePath%>/services/TransferSupportService?wsdl</code>
  </div>
  <div class="block">
    移动服务 WSDL：<code>http://localhost:8081/services/TransferSupportService?wsdl</code>
  </div>

  <div class="block">
    <h2>转入移动</h2>
    <form method="post">
      <input type="hidden" name="action" value="telecom" />
      <div class="form-group">
        <label for="cellPhoneNumber">手机号</label>
        <input id="cellPhoneNumber" name="cellPhoneNumber" type="text" maxlength="11" required />
      </div>
      <div class="form-group">
        <label for="remainMoney">余额</label>
        <input id="remainMoney" name="remainMoney" type="number" min="0" step="0.01" required />
      </div>
      <div class="form-group">
        <label for="orderDesc">套餐说明</label>
        <input id="orderDesc" name="orderDesc" type="text" required />
      </div>
      <div class="form-group">
        <label for="status">当前状态</label>
        <select id="status" name="status" required>
          <option value="0">当前运营商号码</option>
          <option value="1">已转出号码</option>
        </select>
      </div>
      <button type="submit">提交到 TelecomPro</button>
    </form>
  </div>

  <% if (telecomResult != null) { %>
    <div class="result <%= telecomResult.isSuccess() ? "success" : "error" %>">
      <div><strong>结果：</strong><%= telecomResult.getMessage() %></div>
      <div><strong>下一步：</strong><%= telecomResult.getNextStep() %></div>
    </div>
  <% } %>
  <% if (submitError != null) { %>
    <div class="result error">
      <div><strong>调用失败：</strong><%= submitError %></div>
    </div>
  <% } %>
</div>
</body>
</html>
