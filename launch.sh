#
# Google+ Network Extractor
# Copyright 2011-2012 Vincent Labatut 
# 
# This file is part of Google+ Network Extractor.
# 
# Google+ Network Extractor is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 2 of the License, or
# (at your option) any later version.
# 
# Google+ Network Extractor is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Google+ Network Extractor.  If not, see <http://www.gnu.org/licenses/>.
# 
#
# NOTE: script not re-tested yet
#
#
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
