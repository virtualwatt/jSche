:: Run HTTP requests from console; usage: httpReplay.bat <HTTP request file> [<host> <port>]
:: Put this file to the same folder where jsche-event-httpreplay-X.X.X.jar resides (jSche\lib)
:: If you give to request snapshot files same extension (e.g. .hr) setup windows association to this batch file
::  to run your requests using double click in the Explorer or specifying request file name only in the command line
:: Required files: jsche-core-1.2.1.jar, jsche-event-httpreplay-1.0.0.jar, log4j-1.2.17.jar

java -classpath "%~dp0*" vvat.jsche.event.httpreplay.HttpReplay %*
