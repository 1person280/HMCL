# Find cached Gradle
$gradleExe = Get-ChildItem -Path "$env:USERPROFILE\.gradle\wrapper\dists" -Recurse -Filter "gradle.bat" -File | Select-Object -First 1 -ExpandProperty FullName

if ([string]::IsNullOrEmpty($gradleExe)) {
    Write-Host "No Gradle found"
    exit 1
}

Write-Host "Using Gradle: $gradleExe"
Write-Host "Compiling HMCL..."

# Execute compilation
& $gradleExe --offline :HMCL:compileJava :HMCL:processResources :HMCL:classes :HMCL:shadowJar -x test

if ($LASTEXITCODE -eq 0) {
    Write-Host "Build successful!"
    Write-Host "JAR file: HMCL\build\libs\HMCL-*.jar"
} else {
    Write-Host "Build failed, exit code: $LASTEXITCODE"
}
