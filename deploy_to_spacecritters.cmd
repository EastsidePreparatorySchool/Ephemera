md ..\spacecritters
md ..\spacecritters\lib
md ..\spacecritters\aliens
md ..\spacecritters\examples
md ..\spacecritters\examples\customalien
md ..\spacecritters\alieninterfaces


copy spacecritters\dist\spacecritters.jar ..\spacecritters
copy spacecritters\dist\lib\alieninterfaces.jar ..\spacecritters\lib
copy spacecritters\dist\lib\alieninterfaces.jar ..\spacecritters\alieninterfaces
copy spacecritters\dist\lib\gameengine.jar ..\spacecritters\lib
copy spacecritters\dist\lib\stockaliens.jar ..\spacecritters\lib
xcopy customalien\* ..\spacecritters\examples\customalien /s /y
copy alieninterfaces\src\alieninterfaces\* ..\spacecritters\alieninterfaces
copy sc_config.csv ..\spacecritters
