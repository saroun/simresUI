# simresUI - the user interface for SIMRES

`simresUI` is a Java based user interface for the Monte Carlo neutron ray-tracing progrem SIMRES - https://github.com/saroun/simres. It includes three packages:

- `JSTools`: Common library of classes representing various data entities, property editors, file manipulation tools and xml parsers used by simresUI. This library is not designed specifically for SIMRES and can be used to build Java GUI's for other applications as well.  
- `simresCON`: Console user interface for SIMRES providing functions for launching and communicating with the SIMRES core application, setting up the simulation projects, scripting and top level control of the simulation process. The SIMRES core application is executed as a process which communicates with `simresCON` through the console IO. This package encapsulates all non-graphical functions of the Simres user interface.  
- `simresGUI`: An extension of `simresCON`, which adds graphical user interface, including property editors, script editor, 3D instrument viewer (using the Java3D library), project editor, simulation control and command editors, etc.

*See also*:  
SIMRES repository: https://github.com/saroun/simres  
SIMRES homepage: http://neutron.ujf.cas.cz/restrax  
SIMRES user guide: http://neutron.ujf.cas.cz/restrax/download/simres-guide.pdf

---------------------------

## Terms of use
See the license conditions in the attached file LICENSE.

## Requirements
### Java3D

`Java3D` is required to run simnres GUI with 3D visualization of the instrument model. Java3D is not part of the SIMRES source distribution. Recent Java3D packages can be obtained at  
https://jogamp.org/deployment/java3d/  
Required packages are `j3dcore.jar`, `vecmath.jar`, and `j3dutils.jar`. For the version 1.7, they are available at 
https://jogamp.org/deployment/java3d/1.7.0-final/jogamp-java3d1.7.0-final.7z.

Java3D is built on the `jogamp` package, which is also required. Get the `jogamp-fat.jar` package at https://jogamp.org/deployment/jogamp-current/archive/jogamp-fat-all.7z.

Extract all the four jar files so that they are all in the same subdirectory, `./Java3D/j3d-jogamp`.

This is the default path to Java3D defined in the file `build.properties` as the property `java3d=Java3D/j3d-jogamp`. If you decide to use another Java3D implementation, this property has to be changed so that it points to the folder with your Java3D jar files. 

## How to build the package

Use the Apache Ant builder: 
`ant -Ddest=[destination directory]`

from the package root directory. This command will compile and create jar files and copy dependences (Java3D) to the [*destionation directory*]. This argument is optional, the default destination directory is `./dist`. 

Run `ant -p` to see other available targets.

When deploying SIMRES, the contents of the destination directory should be copied to the ./GUI subdirectory of the SIMRES distribution. 

