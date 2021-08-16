## ProbeJ付加起動
java -agentpath:JISD/lib/ProbeJ_ex.dll=JISD/lib/options_none:39876 -cp JISD/bin jisd.demo.LoopN
java -agentlib:jdwp=transport=dt_socket,server=y,address=25432,suspend=n -cp JISD/bin jisd.demo.LoopN
## tomcat with probej port 39876
$env:PROBEJ_PORT=39876; cmd.exe /c %CATALINA_HOME%/bin/catalina.bat probej start
## tomcat with jppa port 25432
$env:JPDA_ADDRESS="localhost:25432"; cmd.exe /c %CATALINA_HOME%/bin/catalina.bat jpda start
