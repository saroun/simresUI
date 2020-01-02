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
## Requirements

`Java3D` is required to run simnres GUI with 3D visualization of the instrument model. Java3D is system dependent. It is not part of the SIMRES source distribution, but should be found in the ./GUI/j3d-jre subdirectory of the binary distribution for given platform. Recent Java3D packages can be obtained at https://jogamp.org/deployment/java3d/. 

The path to the java3D distribution for given platform has to be defined in `build.properties`. Both 32 and 64 bit Windows and Linux systems are supported by the simresUI build scripts. Note that the current version assumes that the Java3D jar files are placed in `[j3d]/lib/ext`, where `[j3d]` is the path to Java3D defined in `build.properties`. The system dependent shared librarties should then be placed in `[j3d]/bin` for Windows (both 32 and 64 bit), or in `[j3d]/lib/amd64` and `[j3d]/lib/i386` for Linux 64 and 32 bit, respectively.

## How to build the package

Use Apache Ant builder: 
`ant -Ddest=[destination directory]`

from the package root directory. This command will compile and create jar files and copy dependences (Java3D) to the [*destionation directory*]. The argument is optional, default destination directory is `./dest`. 

Run `ant -p` to see other available targets.

When deploying SIMRES, contents of the destination directory should be copied to the ./GUI directory of the SIMRES distribution. 

