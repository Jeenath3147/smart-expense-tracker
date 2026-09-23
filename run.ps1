# Edit these two paths to match your machine, then run:  .\run.ps1
$FX = "D:\downloads\openjfx-21.0.12_windows-x64_bin-sdk\javafx-sdk-21.0.12\lib"
$MYSQL = "D:\downloads\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0\mysql-connector-j-9.7.0.jar"

Push-Location "$PSScriptRoot\src"
javac --module-path $FX --add-modules javafx.controls -cp $MYSQL -d ..\bin *.java
if ($LASTEXITCODE -eq 0) {
    java --module-path $FX --add-modules javafx.controls -cp "..\bin;$MYSQL" MainApp
}
Pop-Location
