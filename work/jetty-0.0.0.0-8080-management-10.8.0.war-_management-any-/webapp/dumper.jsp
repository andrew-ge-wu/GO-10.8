<%@ page language="java"
         contentType="text/plain"
         import="com.polopoly.management.troubleshooting.thread.StackTraceCollector"
%><%= new StackTraceCollector(6, true).getFullDump() %>