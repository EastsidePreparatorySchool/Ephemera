set e=%userprofile%\ephemera
md %e%
md %e%\lib
md %e%\aliens

if exist stockaliens\dist\stockaliens.jar copy /y stockaliens\dist\*.jar %e%\lib
if exist alieninterfaces\dist\alieninterfaces.jar copy /y alieninterfaces\dist\*.jar %e%
if exist gameengine\dist\gameengine.jar copy /y gameengine\dist\*.jar %e%
if exist consoleshell\dist\consoleshell.jar copy /y consoleshell\dist\*.jar %e%
if exist consoleshell\dist\guishell.jar copy /y guishell\dist\*.jar %e%
if exist ephemera_initial_setup.csv copy /y ephemera_initial_setup.csv %e%
