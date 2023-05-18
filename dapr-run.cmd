@echo off

REM get properties from application.properties
For /F "tokens=1* delims==" %%A IN (src/main/resources/application.properties) DO (
    IF "%%A"=="dapr.appId" set app-id=%%B
    IF "%%A"=="dapr.port" set dapr-http-port=%%B
    If "%%A"=="server.port" set app-port=%%B
)

REM get command line arguments
set args=%*
set command=.\gradlew.bat bootRun --args="%args%"
IF "%args%"=="" set command=.\gradlew.bat bootRun

REM start dapr
dapr run --app-id %app-id% --app-port %app-port% --dapr-http-port %dapr-http-port% -- %command%
