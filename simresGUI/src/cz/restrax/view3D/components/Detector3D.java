package cz.restrax.view3D.components;

import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.geometry.GeometryFactory;
import cz.restrax.view3D.geometry.SlitAttributes;


public class Detector3D extends Frame3D  {
	protected static final Color3f wallColour = new Color3f(0.7f, 0.7f, 0.2f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
	protected int TYP; // type
	protected int ND; // n number of segments
	protected double SPACE; // distance between adjacent tubes
	protected double RAD; // detection radius (true radius for curved geometries)
	private SlitAttributes att;
	public static final int AREA=0;
	public static final int ARRAY=1;
	public static final int PSD=2;
	public static final int CYLINDRIC=3;
	public static final int SPHERICAL=4;
	
	public Detector3D(ClassData cls) {
		super(cls);		
	}
		
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Detector3D.wallColour, 
				Detector3D.diffusiveColour,
				Detector3D.emissiveColour, 
				Detector3D.specularColour};
		return cc;
	}
	
	public void setTYP(int typ) throws Exception {
		if (typ>=AREA & typ <= SPHERICAL) {
			TYP = typ;
		} else {
			String msg=String.format("Unknown Detector3D type = %d\n", typ);
			throw new Exception(msg);
		}
		
	}

	protected void readClassData(ClassData cls) {
		 super.readClassData(cls);
			try {
				//TYP=(Integer) cls.getField("TYPE").getValue();
				int typ = (Integer) cls.getField("TYPE").getValue();
				setTYP(typ);
				ND=(Integer) cls.getField("ND").getValue();
				SPACE=(Double) cls.getField("SPACE").getValue();
				RAD=(Double) cls.getField("RAD").getValue();
				if (RAD<=0.0d) RAD=1000.0d;
			} catch (Exception e) {
				e.printStackTrace();
			}			
	      // convert SIMRES units (mm, deg) -> (m,rad)		
			SPACE=SPACE*0.001;
			RAD=RAD*0.001;
			if (att == null) {
				int vf=TriangleArray.COORDINATES | TriangleArray.NORMALS ;
	        	att = new SlitAttributes(cls,null,vf);
	        } else att.importFromClass(cls); 
	        att.thframe=0.01f;
	}
	
	public void setGeometry() {
		shape.removeAllGeometries();
		if (TYP==AREA) {
			super.setGeometry();
		} else if (TYP==ARRAY) {
			if (ND == 1) {
			  super.setGeometry();
			} else {
				shape.removeAllGeometries();
				double shift=0.0;
				for (int i=1;i<=ND;i++) {
					shift=(SIZE.x+SPACE)*(i-(ND+1)/2.0);
					addGeometry(new Vector3f((float) shift,0.0f,0.0f));
				}
			}
		} else if (TYP==PSD) {	
			super.setGeometry();	
		} else if (TYP==CYLINDRIC) { 
			shape.addGeometry(getCylinderDetector());
		}
	}
	
	protected Geometry getCylinderDetector() {
		Transform3D t = new Transform3D();
		t.setTranslation(new Vector3d(0.0d,0.0d,-RAD));
		int nopt=(int) Math.max(6.0, 72*SIZE.x/(2.0*Math.PI*RAD));
		return GeometryFactory.getCylinderWall((float) RAD, att, nopt, t);
	}
	
}
