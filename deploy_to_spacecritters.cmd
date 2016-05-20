md ..\spacecritters
md ..\spacecritters\lib
rd /s /y ..\spacecritters\aliens
md ..\spacecritters\aliens
md ..\spacecritters\examples
md ..\spacecritters\alieninterfaces
rd /s /y ..\spacecritters\logs

copy spacecritters\dist\spacecritters.jar ..\spacecritters
copy spacecritters\dist\lib\*.jar ..\spacecritters\lib
copy stockaliens\src\stockaliens\aggressivealien.java ..\spacecritters\examples\
copy alieninterfaces\src\alieninterfaces\* ..\spacecritters\alieninterfaces
copy *.json ..\spacecritters
