md ..\spacecritters
md ..\spacecritters\lib
md ..\spacecritters\aliens
md ..\spacecritters\aliens\examples


copy guishell\dist\guishell.jar ..\spacecritters
copy guishell\dist\lib\*.* ..\spacecritters\lib
copy customalien\dist\customalien.jar ..\spacecritters\aliens\examples
copy ephemera_initial_setup.csv ..\spacecritters
