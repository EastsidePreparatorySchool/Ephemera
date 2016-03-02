if exist ..\stockaliens\dist\stockaliens.jar xcopy /e /y ..\stockaliens\dist\*.* c:\users\public\ephemera\drop\
if exist ..\alieninterfaces\dist\alieninterfaces.jar xcopy /e /y ..\alieninterfaces\dist\*.* c:\users\public\ephemera\drop\
if exist ..\consoleshell\dist\consoleshell.jar xcopy /e /y ..\consoleshell\dist\*.* c:\users\public\ephemera\drop\
if exist ..\gameengine\dist\gameengine.jar xcopy /e /y ..\gameengine\dist\*.* c:\users\public\ephemera\drop\

if exist ..\stockaliens\dist\stockaliens.jar xcopy /y ..\stockaliens\dist\*.jar c:\users\public\ephemera\drop\lib
if exist ..\alieninterfaces\dist\alieninterfaces.jar xcopy /y ..\alieninterfaces\dist\*.jar c:\users\public\ephemera\drop\lib
if exist ..\consoleshell\dist\consoleshell.jar xcopy /y ..\consoleshell\dist\*.jar c:\users\public\ephemera\drop\lib
if exist ..\gameengine\dist\gameengine.jar xcopy /y ..\gameengine\dist\*.jar c:\users\public\ephemera\drop\lib
