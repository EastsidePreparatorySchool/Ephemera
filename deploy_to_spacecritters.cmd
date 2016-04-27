md ..\spacecritters
md ..\spacecritters\lib
md ..\spacecritters\aliens
md ..\spacecritters\aliens\examples


copy spacecritters\dist\spacecritters.jar ..\spacecritters
copy spacecritters\dist\lib\alieninterfaces.jar ..\spacecritters\lib
copy spacecritters\dist\lib\gameengine.jar ..\spacecritters\lib
copy spacecritters\dist\lib\stockaliens.jar ..\spacecritters\lib
copy customalien\dist\customalien.jar ..\spacecritters\aliens\examples
copy sc_config.csv ..\spacecritters
