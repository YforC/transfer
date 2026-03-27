<%@ page language="java" pageEncoding="UTF-8" import="org.springframework.web.context.WebApplicationContext,org.springframework.web.context.support.WebApplicationContextUtils,com.unicom.service.TelecomTransferGatewayService,com.unicom.client.telecom.TransferResponse" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path;
TransferResponse telecomResult = null;
String submitError = null;
String submittedPhone = request.getParameter("cellPhoneNumber");

if ("POST".equalsIgnoreCase(request.getMethod()) && "telecom".equals(request.getParameter("action"))) {
    try {
        WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(application);
        TelecomTransferGatewayService gateway = (TelecomTransferGatewayService) context.getBean("telecomTransferGatewayService");
        telecomResult = gateway.submit(submittedPhone);
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
  <title>携号转网</title>
  <style>
    :root {
      --surface: #dfe5ec;
      --text: #324252;
      --muted: #6f7f90;
      --ok: #3f7f62;
      --error: #b05a61;
      --accent: #58738f;
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
      width: min(100%, 480px);
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
      color: var(--accent);
    }

    h1 {
      margin: 0;
      text-align: center;
      font-size: 30px;
      letter-spacing: 2px;
    }

    .sub {
      margin: 10px 0 24px;
      text-align: center;
      color: var(--muted);
      font-size: 14px;
    }

    .field label {
      display: block;
      margin: 0 0 10px 6px;
      color: var(--muted);
      font-size: 13px;
    }

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

    .field input:focus {
      box-shadow: var(--shadow-inset), 0 0 0 3px rgba(88,115,143,.18);
    }

    .action,
    .back {
      display: block;
      width: 100%;
      min-height: 56px;
      line-height: 56px;
      margin-top: 18px;
      border: 0;
      border-radius: 20px;
      text-align: center;
      text-decoration: none;
      background: linear-gradient(145deg, #6d88a3, #4f677f);
      color: #f7fbff;
      box-shadow: 10px 10px 22px rgba(145,157,175,.55), -10px -10px 22px rgba(255,255,255,.8);
      cursor: pointer;
      font-size: 16px;
    }

    .meta {
      margin-top: 18px;
      text-align: center;
      color: var(--muted);
      font-size: 12px;
    }

    .panel {
      margin-top: 20px;
      padding: 22px;
      border-radius: 28px;
      background: var(--surface);
      box-shadow: var(--shadow-high), var(--shadow-low);
    }

    .panel h2 {
      margin: 0 0 18px;
      text-align: center;
      font-size: 24px;
    }

    .status {
      padding: 18px;
      border-radius: 22px;
      text-align: center;
      font-size: 18px;
      background: var(--surface);
      box-shadow: var(--shadow-inset);
      color: var(--text);
    }

    .status.ok { color: var(--ok); }
    .status.fail { color: var(--error); }

    .detail {
      margin: 18px 0 22px;
      padding: 18px;
      border-radius: 22px;
      background: var(--surface);
      box-shadow: var(--shadow-inset);
    }

    .row {
      display: flex;
      justify-content: space-between;
      gap: 12px;
      padding: 8px 0;
      font-size: 14px;
    }

    .label { color: var(--muted); }

    .next {
      margin-top: 10px;
      color: var(--muted);
      font-size: 13px;
      text-align: center;
    }
  </style>
</head>
<body>
<div class="shell">
  <div class="brand">UNI</div>
  <h1>携号转网</h1>
  <div class="sub">转入移动</div>

  <form method="post">
    <input type="hidden" name="action" value="telecom" />
    <div class="field">
      <label for="cellPhoneNumber">手机号</label>
      <input id="cellPhoneNumber" name="cellPhoneNumber" type="text" maxlength="11" value="<%= submittedPhone == null ? "" : submittedPhone %>" required />
    </div>
    <button class="action" type="submit">立即转网</button>
  </form>

  <div class="meta">
    联通 WSDL：<%=basePath%>/services/TransferSupportService?wsdl
  </div>

  <% if (telecomResult != null || submitError != null) { %>
  <div class="panel">
    <h2>转网结果</h2>

    <% if (telecomResult != null) { %>
      <div class="status <%= telecomResult.isSuccess() ? "ok" : "fail" %>"><%= telecomResult.getMessage() %></div>
      <div class="detail">
        <div class="row">
          <span class="label">手机号</span>
          <span><%= submittedPhone == null ? "-" : submittedPhone %></span>
        </div>
        <div class="row">
          <span class="label">目标</span>
          <span>移动</span>
        </div>
      </div>
      <div class="next"><%= telecomResult.getNextStep() %></div>
    <% } else { %>
      <div class="status fail"><%= submitError %></div>
      <div class="detail">
        <div class="row">
          <span class="label">手机号</span>
          <span><%= submittedPhone == null ? "-" : submittedPhone %></span>
        </div>
        <div class="row">
          <span class="label">目标</span>
          <span>移动</span>
        </div>
      </div>
      <div class="next">请检查手机号或服务状态后重试。</div>
    <% } %>

    <a class="back" href="<%=basePath%>/home.jsp">返回</a>
  </div>
  <% } %>
</div>
</body>
</html>
