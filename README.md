# simresUI - User interface for SIMRES

`simresUI` is a Java based user interface for the Monte Carlo neutron ray-tracing progrem SIMRES - https://github.com/saroun/simres. It includes three packages:

- `JSTools`: Common library of classes representing various data entities, property editors, file manipulation tools and xml parsers used by simresUI. This library is not designed specifically for SIMRES and can be used to build Java GUI's for other applications as well.  
- `simresCON`: Console user interface for SIMRES providing all functions to run and communicate with SIMRES core application, simulation project managment, scripting and top level control of the simulaiton process. The SIMRES core application is executed as a process, which communicates with `simresCON` through the console IO. This package encapsulates all non-graphical functions of simres UI.  
- `simresGUI`: An extension of `simresCON`, which adds graphical user interface, including property editors, script editor, 3D instrumet viewer (using Java3D library), project editor, simulation control and command editors etc. 

*See also*:  
SIMRES repository: https://github.com/saroun/simres  
SIMRES homepage: http://neutron.ujf.cas.cz/restrax  
SIMRES user guide: http://neutron.ujf.cas.cz/restrax/download/simres-guide.pdf

---------------------------

## How to build the package

Use Apache Ant builder: 
`ant -Ddest=[destination directory]`

from the package root directory. This command will compile and create jar files and copy dependences (Java3D) to the [*destionation directory*]. The argument is optional, default destination directory is `./dest`. 

Run `ant -p` to see other available targets.

When deploying SIMRES, contents of the destination directory should be copied to the ./GUI directory of the SIMRES distribution. 

