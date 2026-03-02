$cp = "HMCL\build\classes\java\main;HMCLCore\build\classes\java\main;HMCLBoot\build\classes\java\main;HMCL\lib\*"
javac -cp $cp -encoding UTF-8 -d temp_classes HMCL\src\main\java\org\jackhuang\hmcl\ui\account\MicrosoftAccountLoginPane.java
if ($LASTEXITCODE -eq 0) {
    Write-Host "编译成功！"
    Copy-Item "temp_classes\org\jackhuang\hmcl\ui\account\MicrosoftAccountLoginPane.class" -Destination "HMCL\build\classes\java\main\org\jackhuang\hmcl\ui\account\" -Force
    Write-Host "已更新 class 文件到 HMCL\build\classes\java\main\org\jackhuang\hmcl\ui\account\"
} else {
    Write-Host "编译失败！"
}
