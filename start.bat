@echo off
title Compilador OfflineTime - Java 17

echo Procurando Java 17 instalado...

set JDK_PATH=

rem Procura JDK 17 em locais comuns
for /d %%i in ("C:\Program Files\Java\jdk-17*") do set JDK_PATH=%%i
for /d %%i in ("C:\Program Files\Eclipse Adoptium\jdk-17*") do set JDK_PATH=%%i
for /d %%i in ("C:\Program Files\AdoptOpenJDK\jdk-17*") do set JDK_PATH=%%i

if "%JDK_PATH%"=="" (
    echo ============================================
    echo ERRO: JDK 17 nao encontrado no computador!
    echo Instale o Java 17 para compilar plugins.
    echo ============================================
    pause
    exit
)

echo Usando Java 17 em: %JDK_PATH%

set JAVAC="%JDK_PATH%\bin\javac.exe"
set JAR="%JDK_PATH%\bin\jar.exe"

echo.
echo Limpando pasta out...
rmdir /s /q out >nul 2>&1
mkdir out

echo.
echo Compilando OfflineTime...

%JAVAC% --release 17 -d out ^
-classpath "spigot-api-1.20.1-R0.1-SNAPSHOT.jar;Vault.jar" ^
src/com/foxsrv/offlinetime/OfflineTime.java

if %errorlevel% neq 0 (
    echo ============================================
    echo ERRO AO COMPILAR O PLUGIN!
    echo Verifique os erros acima.
    echo ============================================
    pause
    exit
)

echo.
echo Copiando plugin.yml...
copy plugin.yml out\ >nul

echo.
echo Criando JAR final...

cd out
%JAR% cf OfflineTime.jar *
cd ..

echo.
echo ============================================
echo Plugin compilado com SUCESSO!
echo Arquivo gerado em:
echo out\OfflineTime.jar
echo ============================================

pause
