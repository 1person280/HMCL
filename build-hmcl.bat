@echo off
chcp 65001 >nul
echo 正在使用 Windows 证书存储编译 HMCL...
set GRADLE_OPTS=-Dorg.gradle.jvmargs=-Djavax.net.ssl.trustStoreType=WINDOWS-ROOT
gradlew.bat --offline clean build -x test
if errorlevel 1 (
    echo 离线编译失败，尝试在线编译...
    gradlew.bat clean build -x test
)
pause
