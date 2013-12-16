<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
    <title>Maps Agenda Admin Console</title>
    <link href="admin.css" rel="stylesheet" type="text/css"></link>
    <script src="admin.js"></script>
  </head>
  <body onLoad="onLoadAdmin()">
    <div id="title">
      <div class="left">Maps Agenda admin page</div>
      <div class="right">
        <%
          UserService userService = UserServiceFactory.getUserService();
          User user = userService.getCurrentUser();
          out.println(user.getNickname() + " ");
          out.println("(<a href=\"" 
                      + userService.createLogoutURL("/admin") 
                      + "\">log out</a>)");
        %>
      </div>
    </div>
    <div id="main">
      <div id="menu">
        <div id="menu_selector"></div>
        <div class="menu_item" id="translators">Translators</div>
        <div class="menu_item" id="subscribers">Newsletter subscribers</div>
        <div class="menu_item" id="languages">Supported languages</div>
        <div class="menu_item" id="phrases">Translations</div>
        <div class="menu_item" id="events">Events</div>
        <div class="menu_item" id="generate">Generate XML</div>
        <div class="menu_item" id="send_newsletter">Send newsletter</div>
      </div>
      <div id="content">
        <iframe id="content-frame" name="content-frame" src=""></iframe>
      </div>
    </div>
  </body>
</html>
