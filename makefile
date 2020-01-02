DEST?=./dist
LIB?=.
MCDIR?=.
MCIMP?=libmcplio.a
MCLIB?=libmcplio.so
MCOPT?= -std=c99 -fPIC -I$(MCDIR)/mcpl -I$(MCDIR)
default: build.xml build.os.xml build.con.xml build.gui.xml build.jstools.xml
	ant -f build.xml -Ddest=$(DEST)
	
clean:
	rm -r *.so *.lib *.o
