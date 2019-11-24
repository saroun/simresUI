# simresUI - User interface for SIMRES

`simresUI` is a Java based user interface for the Monte Carlo neutron ray-tracing progrem SIMRES - https://github.com/saroun/simres. It includes three packages:

- `JSUtils`: Common library of classes representing various data entities, property editors, file manipulation tools and xml parsers used by simresUI. This library is not designed specifically for SIMRES and can be used to build Java GUI's for other applications as well.  
- `simresCON`: Console user interface for SIMRES providing all functions to run and communicate with SIMRES core application, simulation project managment, scripting and top level control of the simulaiton process. The SIMRES core application is executed as a process, which communicates with `simresCON` through the console IO. This package encapsulates all non-graphical functions of simres UI.  
- `simresGUI`: An extension of `simresCON`, which adds graphical user interface, including property editors, script editor, 3D instrumet viewer (using Java3D library), project editor, simulation control and command editors etc. 

*See also*:  
SIMRES repository: https://github.com/saroun/simres  
SIMRES homepage: http://neutron.ujf.cas.cz/restrax  
SIMRES user guide: http://neutron.ujf.cas.cz/restrax/download/simres-guide.pdf
