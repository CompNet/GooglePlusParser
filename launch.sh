# define path variables
	bin="./bin"
	jerseyclient="./lib/jersey-client-1.8.jar"
	jerseycore="./lib/jersey-core-1.8.jar"
	jerseyjson="./lib/jersey-json-1.8.jar"
	jettison="./lib/jettison-1.3.jar"
	h2="./lib/h2-1.3.158.jar"
	log4j="./lib/log4j-1.2.16.jar"
	cp="${bin}:${jerseyclient}:${jerseycore}:${jerseyjson}:${jettison}:${h2}:${log4j}"
	launcher="tr.edu.gsu.googleplus.Launcher"	
	
# launch the program
#	java -Xmx256 -classpath $cp $launcher
	java -classpath $cp $launcher
