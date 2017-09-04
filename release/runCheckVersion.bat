@echo off

set CLASSPATH=%CLASSPATH%;mail.jar;activation.jar;commons-codec-1.9.jar;commons-logging-1.2.jar;fluent-hc-4.5.jar;httpclient-4.5.jar;httpclient-cache-4.5.jar;httpclient-win-4.5.jar;httpcore-4.4.1.jar;httpmime-4.5.jar;jna-4.1.0.jar;jna-platform-4.1.0.jar

java CheckVersion
pause