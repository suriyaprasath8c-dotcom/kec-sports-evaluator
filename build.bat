@echo off
title Build KEC Sports Evaluator
cls
echo =======================================================================
echo     BUILDING KEC SPORTS ATHLETIC PERFORMANCE & SCHOLARSHIP EVALUATOR
echo =======================================================================
echo.

cd /d "%~dp0"
call mvn clean package

echo.
echo Build process finished. The WAR file is located in target\kec-sports-evaluator.war
pause
