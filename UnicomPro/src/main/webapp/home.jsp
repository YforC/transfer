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
        telecomResult = gateway.submit(request.getParameter("cellPhoneNumber"));
    } catch (Exception ex) {
        submitError = ex.getMessage();
    }
}
%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>联通携号转网</title>
  <style>
    :root {
      --surface: #dfe5ec;
      --text: #324252;
      --muted: #6f7f90;
      --shadow-high: -14px -14px 28px rgba(255,255,255,.88);
      --shadow-low: 14px 14px 28px rgba(163,177,198,.52);
      --shadow-inset: inset 8px 8px 16px rgba(163,177,198,.42), inset -8px -8px 16px rgba(255,255,255,.88);
    }
    * { box-sizing: border-box; }
    body {
      margin: 0;
      min-height: 100vh;
      display: grid;
      place-items: center;
      padding: 24px;
      background: radial-gradient(circle at top left, #edf1f6 0%, var(--surface) 50%, #d4dbe3 100%);
      font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
      color: var(--text);
    }
    .shell {
      width: min(100%, 460px);
      padding: 30px;
      border-radius: 32px;
      background: var(--surface);
      box-shadow: var(--shadow-high), var(--shadow-low);
    }
    .brand {
      width: 72px;
      height: 72px;
      margin: 0 auto 18px;
      border-radius: 24px;
      display: grid;
      place-items: center;
      background: var(--surface);
      box-shadow: var(--shadow-inset);
      font-size: 24px;
      font-weight: 700;
      color: #58738f;
    }
    h1 { margin: 0; text-align: center; font-size: 30px; letter-spacing: 2px; }
    .sub { margin: 10px 0 24px; text-align: center; color: var(--muted); font-size: 14px; }
    .field label { display: block; margin: 0 0 10px 6px; color: var(--muted); font-size: 13px; }
    .field input {
      width: 100%;
      min-height: 54px;
      border: 0;
      border-radius: 20px;
      padding: 0 18px;
      font-size: 18px;
      color: var(--text);
      background: var(--surface);
      box-shadow: var(--shadow-inset);
      outline: none;
    }
    .field input:focus { box-shadow: var(--shadow-inset), 0 0 0 3px rgba(88,115,143,.18); }
    .action {
      width: 100%;
      min-height: 56px;
      margin-top: 18px;
      border: 0;
      border-radius: 20px;
      background: linear-gradient(145deg, #6d88a3, #4f677f);
      color: #f7fbff;
      box-shadow: 10px 10px 22px rgba(145,157,175,.55), -10px -10px 22px rgba(255,255,255,.8);
      cursor: pointer;
      font-size: 16px;
    }
    .meta {
      margin-top: 18px;
      color: var(--muted);
      font-size: 12px;
      text-align: center;
    }
    .result {
      margin-top: 18px;
      padding: 16px;
      border-radius: 20px;
      background: var(--surface);
      box-shadow: var(--shadow-inset);
      font-size: 14px;
      line-height: 1.7;
    }
  </style>
</head>
<body>
<div class="shell">
  <div class="brand">UNI</div>
  <h1>携号转网</h1>
  <div class="sub">输入手机号即可</div>

  <form method="post">
    <input type="hidden" name="action" value="telecom" />
    <div class="field">
      <label for="cellPhoneNumber">手机号</label>
      <input id="cellPhoneNumber" name="cellPhoneNumber" type="text" maxlength="11" required />
    </div>
    <button class="action" type="submit">转入移动</button>
  </form>

  <div class="meta">
    联通 WSDL：<%=basePath%>/services/TransferSupportService?wsdl
  </div>

  <% if (telecomResult != null) { %>
  <div class="result">
    <div><strong>结果：</strong><%= telecomResult.getMessage() %></div>
    <div><strong>下一步：</strong><%= telecomResult.getNextStep() %></div>
  </div>
  <% } %>

  <% if (submitError != null) { %>
  <div class="result">
    <div><strong>失败：</strong><%= submitError %></div>
  </div>
  <% } %>
</div>
</body>
</html>
