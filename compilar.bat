@echo off
setlocal
cd /d "%~dp0"

echo Limpiando carpeta out...
if exist out rmdir /s /q out
mkdir out

echo Buscando archivos .java...
dir /s /b src\*.java > sources.txt

echo Compilando...
javac -d out @sources.txt
if errorlevel 1 (
    echo.
    echo ERROR: la compilacion fallo.
    del sources.txt
    pause
    exit /b 1
)

del sources.txt
echo.
echo Compilacion correcta. Clases en la carpeta out.
echo Para ejecutar el cliente:  java -cp out cliente.Main
echo Para ejecutar el servidor: java -cp out servidor.Main
pause
endlocal
