package cz.restrax.view3D;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2013/04/06 23:15:09 $</dt></dl>
 */
import java.util.HashMap;

import org.jogamp.java3d.TransformGroup;
import org.jogamp.vecmath.Point3f;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDataCollection;
import cz.restrax.sim.Instrument;
import cz.restrax.view3D.components.Chopper3D;
import cz.restrax.view3D.components.Crystal3D;
import cz.restrax.view3D.components.Detector3D;
import cz.restrax.view3D.components.Frame3D;
import cz.restrax.view3D.components.FrameShape;
import cz.restrax.view3D.components.Guide3D;
import cz.restrax.view3D.components.SGuide3D;
import cz.restrax.view3D.components.Sample3D;
import cz.restrax.view3D.components.Slit3D;
import cz.restrax.view3D.components.Source3D;
import cz.restrax.view3D.components.Xtal3D;


public class Frame3DCollection {
    private String 	name=null;
	private HashMap<Integer,Frame3D> items=null;
	private TransformGroup topGroup=null;
	private Frame3D sample=null;
	private int faceStyle = FrameShape.VIEW_FACE;
	private Instrument spectrometer;
	/*
	public Frame3DCollection(String name) {
		this.name=name;
		clearAll();
		topGroup = new TransformGroup();
		topGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		topGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
    }
	*/
	public Frame3DCollection(Instrument spectrometer) {
    	this.spectrometer=spectrometer;
    	name="";
		if (spectrometer != null) name=spectrometer.getCfgTitle();
		items = new HashMap<Integer,Frame3D>();
		reset(spectrometer);
    }
	
	public void update() {
    	if (spectrometer != null) {
    		name=spectrometer.getCfgTitle();
    		updateFromClassCollection(spectrometer.getPrimarySpec());
			updateFromClassCollection(spectrometer.getSpecimen());
			updateFromClassCollection(spectrometer.getSecondarySpec());		
    	}
    }
	
	public void reset(Instrument spectrometer) {
    	clearAll();
		if (spectrometer != null) {
			this.name=spectrometer.getCfgTitle();
    		this.spectrometer=spectrometer;
			importClassCollection(spectrometer.getPrimarySpec());
			importClassCollection(spectrometer.getSpecimen());
			importClassCollection(spectrometer.getSecondarySpec());	
			createGroup();
    	}
  //  	System.out.printf("Frame3DCollection.reset spectrometer=%s\n",spectrometer);
    }
	
	protected void clearAll() {
		sample=null;
		// if (topGroup != null) topGroup.removeAllChildren();
	//	if (topGroup!=null) topGroup.removeAllChildren();
    	topGroup = new TransformGroup();
    	topGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		topGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		items = new HashMap<Integer,Frame3D>();
	}
	
	protected int size() {
		return items.size();
	}
	
	protected void addNew(Frame3D item) {
		if (item != null) items.put((Integer)(items.size()), item);
	}
    
	protected void updateItemFromClass(ClassData cls) { 
    	Frame3D comp=getClassByID(cls.getId());    	
    	if (comp != null) {
    		comp.updateFromClass(cls);    		
    	}
    }
    
    
    /**
     * re-creates TransformGroup (tGroup) from the existing collection of Frame3D objects (items)
     */
    protected void createGroup() {
    	for (int i=0;i<items.size();i++) {
    		Frame3D f = items.get(i);
    		if (i == 0) {
    			topGroup.addChild(f.getDistance());
    		} else {
    			f.appendTo(items.get(i-1));
    		}    		
    	}    	
    }
        
    protected void importClassData(ClassData cls) {    	
    	String cid = cls.getClassDef().cid;
    	Frame3D f=null;
    	if (cid.equals("CRYSTAL")) {
    		f = new Crystal3D(cls);    	
    	} else if (cid.equals("XTAL")) {
        		f = new Xtal3D(cls); 
    	} else if (cid.equals("DCHOPPER")) {
    		f = new Chopper3D(cls);         		
    	} else if (cid.equals("SGUIDE")) {
    		f = new SGuide3D(cls);         		
    	} else if (cid.equals("GUIDE")) {
    		f = new Guide3D(cls);
    	} else if (cid.equals("SOURCE")) {
    		f = new Source3D(cls);
    	} else if (cid.equals("SAMPLE") | cid.equals("PCRYST") | cid.equals("SCRYST")) {
    		f = new Sample3D(cls);
    		sample=f;
    	} else if (cid.equals("DETECTOR")) {
    		f = new Detector3D(cls);
    	} else if (cid.equals("FRAME")) {
    		f = new Slit3D(cls);
    	} else {
    		f = new Frame3D(cls);    		
    	}
    	if (f != null) addNew(f);    	
    }

    /**
     * Return Frame3D object representing the given ClassData object.
     * Return null if such 3D object does not exist.
     */
    protected Frame3D getFrame3D(ClassData cls) { 
    	Frame3D f = null;
    	for (int i=0;i<items.size();i++) {
    		f = items.get(i);
    		if (items.get(i).getCls() == cls ) {
    			f=items.get(i);
    			break;
    		}
    	}
    	return f;
    }
    
    protected void importClassCollection(ClassDataCollection c) {    	
    	if (c != null) {
    		for (int i=0;i<c.size();i++) {
    			importClassData(c.get(i));
    		} 
    	}
    }

    protected void updateFromClassCollection(ClassDataCollection c) {    	
    	if (c != null) {
    		for (int i=0;i<c.size();i++) {
    			updateItemFromClass(c.get(i));
    		} 
    	}
    }

    protected Frame3D getClassByID(String id) {
		for (int i=0;i<items.size();i++) {
			Frame3D cd=items.get(i);
			if (cd.getId().equals(id)) {
				return items.get(i);
			}
		}
		return null;		
	}

	
	public int getCentralBeamLength() {
		int n=0;
		for (int i=0;i<items.size();i++) {
			Frame3D cd=items.get(i);
			n += cd.getCentralBeamLength();			
		}
		return n;
	}
	
	public Point3f[] getCentralBeam() {
		Point3f[] c;
		int n=0;
		Point3f[] b = new Point3f[getCentralBeamLength()];
		for (int i=0;i<items.size();i++) {
			Frame3D cd=items.get(i);
			c=cd.getCentralBeam();
			for (int j=0;j<c.length;j++) {
				b[n+j] = new Point3f(c[j]);
			}
			n += c.length;
		}
		return b;
	}
	
	
	
//****************   ACCESS METHODS  ********************
	
	public String getName() {
		return name;
	}
	
	protected TransformGroup getTopGroup() {
		return topGroup;
	}		
	
	public Frame3D getDefaultFocusObject() {
		if (sample != null) {
			return sample;
		} else if (items.size() > 0) {
			return items.get(0);
		} else return null;
	}
	
	public int getFaceStyle() {
		return faceStyle;
	}
	
	/**
	 * Set style of Frame objects (face or wireframe).
	 * @param faceStyle either Frame3D.VIEW_WIRE or Frame3D.VIEW_FACE
	 */
	public void setFaceStyle(int faceStyle) {
		if (this.faceStyle != faceStyle) {
			this.faceStyle = faceStyle;
			for (int i=0;i<items.size();i++) {
				items.get(i).setFaceStyle(faceStyle);
			}
		}
	}

	public Instrument getSpectrometer() {
		return spectrometer;
	}
}
