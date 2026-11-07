@echo off
title Online Reservation System
if not exist "lib\sqlite-jdbc.jar" (
    echo Downloading SQLite JDBC drivers for first-time setup...
    powershell -Command "New-Item -ItemType Directory -Force -Path lib; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar' -OutFile 'lib/sqlite-jdbc.jar'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-api/2.0.12/slf4j-api-2.0.12.jar' -OutFile 'lib/slf4j-api.jar'; Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/2.0.12/slf4j-nop-2.0.12.jar' -OutFile 'lib/slf4j-nop.jar'"
)
if not exist "build" (
    call build.bat
)
echo Starting Online Reservation System...
java --enable-native-access=ALL-UNNAMED -cp "ReservationSystem.jar;lib/*;build" com.reservation.Main
pause
