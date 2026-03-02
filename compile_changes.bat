@echo off
set JAVA_HOME=C:\Program Files\Zulu\zulu-25
set SRCDIR=c:\Users\1person\HMCL\HMCL\src\main\java
set BUILDDIR=c:\Users\1person\HMCL\HMCL\build\classes\java\main
set COREJAR=c:\Users\1person\HMCL\HMCLCore\build\libs\HMCLCore.jar
set RESDIR=c:\Users\1person\HMCL\HMCL\build\resources\main

"%JAVA_HOME%\bin\javac.exe" -encoding UTF-8 -cp "%BUILDDIR%;%COREJAR%;%RESDIR%" -d "%BUILDDIR%" "%SRCDIR%\org\jackhuang\hmcl\setting\LauncherVisibility.java" "%SRCDIR%\org\jackhuang\hmcl\setting\CloseWindowBehavior.java" "%SRCDIR%\org\jackhuang\hmcl\ui\SystemTrayManager.java"
