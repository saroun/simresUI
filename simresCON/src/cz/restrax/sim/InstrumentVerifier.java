package cz.restrax.sim;

import java.awt.Component;

import javax.swing.JOptionPane;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.jstools.classes.ClassDef;
import cz.jstools.classes.ClassesCollection;

public class InstrumentVerifier {
	private ClassesCollection classes;
	private ClassDataCollection primary; 
	private ClassDataCollection specimen;
	private ClassDataCollection secondary;
	private String msg="";
	public InstrumentVerifier(
			ClassesCollection classes,
			ClassDataCollection primary, 
			ClassDataCollection specimen,
			ClassDataCollection secondary
			) {
		this.classes=classes;
		this.primary=primary;
		this.specimen=specimen;
		this.secondary=secondary;
	}
	
	private static int getNumberOfInstances(String cid,ClassDataCollection group,boolean withParents) {
		ClassData cd;
		int n=0;
		if (group != null) {
		for (int i=0;i<group.size();i++) {
			cd=group.get(i);
			if (withParents) {
				if (cd.getClassDef().isInstanceOf(cid)) n++;
			} else {
				if (cd.getClassDef().cid.equals(cid)) n++;
			}
		}
		}
		return n;
	}
	
	public boolean isNumberOfInstancesOk() {
		boolean b=true;
		ClassDef cd;
		int n;
		for (int ic=0;ic<classes.size();ic++) {
			cd=classes.get(ic);
			n=getNumberOfInstances(cd.cid, primary,false);
			n=n+getNumberOfInstances(cd.cid, specimen,false);
			n=n+getNumberOfInstances(cd.cid, secondary,false);
			if (n > cd.maxInstances) {
				msg += "\t Max. number of instances exceeded for "+cd.cid+", limit="+cd.maxInstances+"\n";
				b=false;
			}
		}
		return b;
	}
	/**
	 * Check that configuration is consistent
	 * (it has what is expected)
	 */
	public boolean verify(Component msgParent) {
		//	System.err.print("verify\n");
		boolean b=true;
		msg="";
		b = b && isNumberOfInstancesOk();		
		if ((primary == null) || (primary.size() < 1)) {
			msg += "\t no primary beamline is defined\n";
			b=false;
		} else {
			String cid=primary.get(0).getClassDef().cid;
			if (! cid.equals("SOURCE")) {		
				msg += "\t the SOURCE component is missing or it is not the 1st component on the PRIMARY beamline\n";
				b=false;
			}
		}
		if (specimen != null) {
			if (specimen.size()>1) {
				msg += "\t At most one specimen component is allowed \n";
				b=false;
			}
		}
		if (! b) {
			msg = "There are problems with the instrument setup:\n" + msg;
			JOptionPane.showMessageDialog(msgParent,msg,"Error", JOptionPane.ERROR_MESSAGE);
		}
		return b;
	}
}
