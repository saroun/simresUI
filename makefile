DEST?=./dist
default: build.xml build.os.xml build.con.xml build.gui.xml build.jstools.xml
	ant -f build.xml -Ddest=$(DEST)
	
cleandist:
	ant -f build.xml cleandist
