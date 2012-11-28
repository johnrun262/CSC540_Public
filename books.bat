@echo off
set RESTVAR=

REM Use Shift to get more than 9 parameters

REM Parameter 1
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 2
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 3
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 4
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 5
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 6
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 7
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 8
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 9
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 10
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 11
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 12
set RESTVAR=%RESTVAR% %1
shift
REM Parameter 13
set RESTVAR=%RESTVAR% %1
shift

:after_loop
echo %RESTVAR%

java -classpath "lib\classes12.zip";"lib\BooksAThousand.jar" BooksCmd %RESTVAR%