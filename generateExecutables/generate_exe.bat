SET mypath=%~dp0
del "%mypath%Room-Model-Editor.exe"
rmdir "%mypath%installer"
MD "%mypath%installer\Windows"
MD "%mypath%installer\Mac"
MD "%mypath%installer\Linux"

"C:\Program Files (x86)\Launch4j\launch4jc.exe" "%mypath%launch4j-config.xml"
"C:\Program Files (x86)\Inno Setup 5\ISCC.exe" "%mypath%InnoSetupScript.iss"

REM call "mac\apache-ant\bin\ant.bat" -buildfile mac-ant-build.xml bundle-Room-Model-Editor

pause