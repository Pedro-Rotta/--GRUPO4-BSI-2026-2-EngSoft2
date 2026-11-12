@echo off
rem ============================================================
rem  CateSys - inicia o servidor e abre a tela no navegador.
rem  Uso: duplo-clique neste arquivo. Para parar: feche a janela
rem  ou aperte Ctrl+C.
rem ============================================================
setlocal
cd /d "%~dp0"
title CateSys
set "TELA=http://localhost:8080/VIEW/catequizandos.html"

rem --- 1. Ja esta rodando? So abre o navegador. ---------------
netstat -ano | findstr /r /c:":8080 .*LISTENING" >nul
if not errorlevel 1 (
    echo O servidor ja esta rodando na porta 8080. Abrindo a tela...
    start "" "%TELA%"
    "%SystemRoot%\System32\timeout.exe" /t 3 >nul
    exit /b 0
)

rem --- 2. Procura um JDK 17 ------------------------------------
set "JDK="
for /d %%D in (
    "%USERPROFILE%\.jdks\*-17*"
    "C:\Program Files\Java\jdk-17*"
    "C:\Program Files\Java\jdk17*"
    "C:\Program Files\Eclipse Adoptium\jdk-17*"
    "C:\Program Files\Microsoft\jdk-17*"
    "C:\Program Files\Zulu\zulu-17*"
    "C:\Program Files\Amazon Corretto\jdk17*"
    "C:\Program Files\BellSoft\LibericaJDK-17*"
) do (
    if exist "%%~D\bin\java.exe" set "JDK=%%~D"
)

if defined JDK (
    set "JAVA_HOME=%JDK%"
) else if defined JAVA_HOME (
    echo Aviso: nao achei um JDK 17; tentando com JAVA_HOME=%JAVA_HOME%
) else (
    where java >nul 2>nul
    if errorlevel 1 (
        echo.
        echo ERRO: nenhum Java encontrado neste computador.
        echo Instale o JDK 17 em https://adoptium.net/temurin/releases/?version=17
        echo ou abra o projeto no IntelliJ e rode a classe CateSysApplication.
        echo.
        pause
        exit /b 1
    )
    echo Aviso: nao achei um JDK 17; tentando com o java do PATH.
)
echo Usando Java: %JAVA_HOME%

rem --- 3. Abre o navegador quando o servidor responder --------
start "" /b powershell -NoProfile -WindowStyle Hidden -Command ^
  "for($i=0;$i -lt 120;$i++){try{Invoke-WebRequest -UseBasicParsing '%TELA%' -TimeoutSec 2 | Out-Null; Start-Process '%TELA%'; break}catch{Start-Sleep 2}}"

rem --- 4. Sobe o servidor --------------------------------------
echo.
echo Iniciando o CateSys... a tela abre sozinha quando estiver pronto.
echo Endereco: %TELA%
echo Para parar: feche esta janela ou aperte Ctrl+C.
echo.
call "%~dp0gradlew.bat" bootRun

echo.
echo O servidor parou. Se apareceu erro acima, leia a mensagem antes de fechar.
pause
