md ..\spacecritters
md ..\spacecritters\lib
rd /s ..\spacecritters\aliens
md ..\spacecritters\aliens
md ..\spacecritters\examples
md ..\spacecritters\alieninterfaces
rd /s ..\spacecritters\logs

copy spacecritters\dist\spacecritters.jar ..\spacecritters
copy spacecritters\dist\lib\alieninterfaces.jar ..\spacecritters\lib
copy spacecritters\dist\lib\alieninterfaces.jar ..\spacecritters\alieninterfaces
copy spacecritters\dist\lib\gameengine.jar ..\spacecritters\lib
copy spacecritters\dist\lib\stockaliens.jar ..\spacecritters\lib
copy customalien\src\customalien\customalien.java ..\spacecritters\examples\
copy stockaliens\src\stockaliens\aggressivealien.java ..\spacecritters\examples\
copy alieninterfaces\src\alieninterfaces\* ..\spacecritters\alieninterfaces
copy sc_config.csv ..\spacecritters
