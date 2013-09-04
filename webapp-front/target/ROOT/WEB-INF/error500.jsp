<%@page isErrorPage="true" import="java.io.*, com.polopoly.html.CharConv" %>

<html>
  <head>
    <title>500 - Server error</title>
  </head>

  <body>
    <div style="font-family: sans-serif; color: darkred; font-size: 12px; font-weight: bold; margin-top: 100px; margin-left: 20px; text-align: left;">An unexcepted error occurred: <pre><%= exception != null ? CharConv.CC.toHTML(exception.toString()) : "" %></pre></div>
<%
    if (exception != null) {
        out.println("<!-- Full stack trace:");
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.close();
        out.print(CharConv.CC.toHTML(sw.toString()));
        out.print(" -->");
    }
%>
  </body>
</html>
