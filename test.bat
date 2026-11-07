@echo off
title Run Test Suite
echo Running Online Reservation System Automated Tests...
java --enable-native-access=ALL-UNNAMED -cp "lib/*;build;." TestSystem
pause

