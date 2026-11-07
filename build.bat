@echo off
title Build Online Reservation System
if not exist "lib\sqlite-jdbc.jar" (
    echo Downloading required dependencies...
    powershell -Command "New-Item -ItemType Directory -Force -Path lib; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar' -OutFile 'lib/sqlite-jdbc.jar'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar' -OutFile 'lib/slf4j-api.jar'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/2.0.12/slf4j-nop-2.0.12.jar' -OutFile 'lib/slf4j-nop.jar'"
)

echo Compiling source files...
if not exist "build" mkdir build

javac -cp "lib/*" -d build src\com\reservation\util\*.java src\com\reservation\model\*.java src\com\reservation\db\*.java src\com\reservation\dao\*.java src\com\reservation\ui\*.java src\com\reservation\Main.java
if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b %errorlevel%
)

echo Packaging ReservationSystem.jar...
if exist "C:\Program Files\Java\jdk-26.0.1\bin\jar.exe" (
    "C:\Program Files\Java\jdk-26.0.1\bin\jar.exe" cfm ReservationSystem.jar manifest.txt -C build .
) else (
    jar cfm ReservationSystem.jar manifest.txt -C build .
)

echo Build completed successfully!
