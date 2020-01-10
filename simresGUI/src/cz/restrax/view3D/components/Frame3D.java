package cz.restrax.view3D.components;

import java.util.Enumeration;

import javax.media.j3d.Appearance;
import javax.media.j3d.Geometry;
import javax.media.j3d.Material;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Sphere;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ClassDef;
import cz.restrax.view3D.Utils;
import cz.restrax.view3D.geometry.BoxGeometry;
import cz.restrax.view3D.geometry.DiscFaceGeometry;
import cz.restrax.view3D.geometry.DiscWallGeometry;
import cz.restrax.view3D.geometry.OFaceGeometry;
import cz.restrax.view3D.geometry.OWallGeometry;
import cz.restrax.view3D.geometry.RFaceGeometry;
import cz.restrax.view3D.geometry.RWallGeometry;
import cz.restrax.view3D.geometry.SlitAttributes;

/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.13 $</dt>
 *               <dt>$Date: 2019/06/22 19:53:47 $</dt></dl>
 */
public class Frame3D {	
	protected static final Color3f wallColour = new Color3f(0.5f, 0.5f, 0.5f);
	protected static final Color3f diffusiveColour = new Color3f(0.5f, 0.5f, 0.5f);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    protected static final float shine = FrameShape.SHINE_DARK;
	protected static final float deg = (float)Math.PI/180;
	public static final int FRAME_SHAPE_ELLIPSOID = 0;
	public static final int FRAME_SHAPE_CYLINDER = 1;  // cylinder, axis // y
	public static final int FRAME_SHAPE_DISC = 2;      // cylinder, axis // z
	public static final int FRAME_SHAPE_BOX = 3;
   
	protected String id = "undefined";
	protected String cid = "undefined";	
	protected FrameShape shape = null;
    private TransformGroup gonio=new TransformGroup();
	private TransformGroup exitAxis=new TransformGroup();
	private TransformGroup stage=new TransformGroup();
	private TransformGroup stage2=new TransformGroup();
	private TransformGroup distance=new TransformGroup();
	private TransformGroup scaling=new TransformGroup();
    
   // object parameters, imported from ClassData
    protected Vector3f AX;
    protected Vector3f GON;
    protected Vector3f STA;
    protected Vector3f SIZE;
    protected Transform3D exitTransform = new Transform3D();
    protected float DIST;
    protected int SHAPE;
    protected int ORDER=0;
    //protected float thick=0.002f;
    protected boolean selected=false;
    private ClassData cls=null;
    private int faceStyle = FrameShape.VIEW_FACE;
    protected Appearance appearance;
    protected Material material;

	public Frame3D(ClassData cls) {   	
		this.cls = cls;
    	ClassDef cd = cls.getClassDef().lastParent();
    	if (cd.cid.equals("FRAME")) {    		
    		int cap=TransformGroup.ALLOW_TRANSFORM_WRITE;
    	// set capabilities
    		gonio.setCapability(cap);
    		stage.setCapability(cap);
    		stage2.setCapability(cap);
    		distance.setCapability(cap);
    		exitAxis.setCapability(cap);
    		scaling.setCapability(cap);    		    	
    		shape=new FrameShape(this);
    		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
    		shape.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
    		shape.setCapability(Shape3D.ENABLE_PICK_REPORTING);
    		shape.setPickable(true);
   		// link transformations
    		distance.addChild(stage);
    		stage.addChild(gonio);
    		gonio.addChild(stage2);
    		stage2.addChild(scaling);
    		scaling.addChild(shape);
    		distance.addChild(exitAxis);    				
    	// define shape and appearance	
    		updateFromClass(cls);    		
    	}
    }
    
	protected void readClassData(ClassData cls) {
		cid=cls.getClassDef().cid;
		id=cls.getId();
		try {
			AX = Utils.DoubleToVector3f((Double[]) cls.getField("AX").getValue());
			GON= Utils.DoubleToVector3f((Double[]) cls.getField("GON").getValue());
			SIZE= Utils.DoubleToVector3f((Double[]) cls.getField("SIZE").getValue());
			STA= Utils.DoubleToVector3f((Double[]) cls.getField("STA").getValue());
			DIST = ((Double) cls.getField("DIST").getValue()).floatValue();
			//thick = ((Double) cls.getField("THICK").getValue()).floatValue();
			SHAPE = (Integer) cls.getField("SHAPE").getValue();
			ORDER = (Integer) cls.getField("ORDER").getValue();			
		} catch (Exception e) {
			e.printStackTrace();
		}			
// convert SIMRES units (mm, deg) -> (m,rad)		
		AX.scale(deg);
		SIZE.scale(0.001f);
		STA.scale(0.001f);
		DIST=DIST*0.001f;
		//thick=thick*0.001f;
		GON.scale(deg);		
	}
	
    /**
     * Reads values from given ClassData object. Then updates geometry, appearance and all TransformGroup objects.
     * @param cls ClassData object with component parameters.
     * @see cz.jstools.classes.ClassData
     */
    public void updateFromClass(ClassData cls) { 
    	readClassData(cls);
    	setGeometry();
    	setAppearance();
    	setExitAxis();
    	setDistance(); 
    	setMotionControl();
    	// setStage(); 
    	// setGonio();
    }

	public void appendTo(Frame3D parent) {
		parent.exitAxis.addChild(distance);
	}
		
	protected void setAppearance() {
		appearance = getAppearance();		
		if (selected) {
			Color3f[] cc = getDefaultColors();
			appearance.getMaterial().setEmissiveColor(cc[2]);			
		} else {
			appearance.getMaterial().setEmissiveColor(FrameShape.BLACK);
		}
		appearance.setPolygonAttributes(FrameShape.getPolygonAttributes(faceStyle));
	    shape.setAppearance(appearance);	    
	}
	
	
	protected Appearance createAppearance() {
		Appearance app = new Appearance();
		TransparencyAttributes ta=null;	
		ta = new TransparencyAttributes();
		app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		app.setCapability(Appearance.ALLOW_MATERIAL_READ);
		app.setTransparencyAttributes(ta);
		app.setMaterial(getMaterial());	   			
		app.setPolygonAttributes(FrameShape.getPolygonAttributes(faceStyle));
		return app;
	}
	
	
	protected Material cretaeMaterial() {
		Color3f[] cc = getDefaultColors();
		Material m = new Material(cc[0],FrameShape.BLACK,cc[1],FrameShape.BLACK,shine);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		return m;
	}

	public Appearance getAppearance() {
		if (appearance==null) {
			appearance = createAppearance();			
		}
		return appearance;
	}
	
	public Material getMaterial() {
		if (material==null) {
			material = cretaeMaterial();			
		}		
		return material;
	}

	/**
	 * Replace old geometry with a new one using current object parameters
	 */
	public void setGeometry() {
		shape.removeAllGeometries();
		addGeometry(null);				
	}
	
	/**
	 * Return ambient, diffusive, emissive and specular colors
	 */
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Frame3D.wallColour, 
				Frame3D.diffusiveColour,
				Frame3D.emissiveColour, 
				Frame3D.specularColour};
		return cc;
	}
	
	/**
	 * Replace old geometry with a new one using current object parameters.
	 * In addition, apply given translation.
	 * @param size
	 */
	public void addGeometry(Vector3f trans) {		
		Transform3D tr=null;
		if (trans!=null) {
			tr=new Transform3D();
			tr.setTranslation(trans);
		}		
		if (SHAPE==FRAME_SHAPE_ELLIPSOID) {
	// ellipsoid	
			if (SIZE.x <= 0.0) return;
			Sphere s = new Sphere(SIZE.x,Sphere.GEOMETRY_NOT_SHARED,15,shape.getAppearance());
			shape.removeAllGeometries();
			for (Enumeration e = s.getShape().getAllGeometries() ; e.hasMoreElements() ;) {
				 shape.addGeometry((Geometry) e.nextElement()); 
			}
			Transform3D t = new Transform3D();
			t.setScale(new Vector3d(1.0,SIZE.y/SIZE.x,SIZE.z/SIZE.x));
			scaling.setTransform(t);
		//	shape.addGeometry(new BoxGeometry(SIZE,tr));
		} else if (SHAPE==FRAME_SHAPE_CYLINDER) {
	// cylinder
			Transform3D t = new Transform3D();
			t.rotX(Math.PI/2);
			if (trans!=null) t.setTranslation(trans);
			Vector3f s = new Vector3f(SIZE.x,SIZE.z,SIZE.y);
			shape.addGeometry(new DiscWallGeometry(s,1,t));
			shape.addGeometry(new DiscFaceGeometry(s,1,t));
			shape.addGeometry(new DiscFaceGeometry(s,-1,t));
		} else if (SHAPE==FRAME_SHAPE_DISC) {
	// disc	
			shape.addGeometry(new DiscWallGeometry(SIZE,1,tr));
			shape.addGeometry(new DiscFaceGeometry(SIZE,1,tr));
			shape.addGeometry(new DiscFaceGeometry(SIZE,-1,tr));	
		} else {
	// box		
			shape.addGeometry(new BoxGeometry(SIZE,tr));
		}		
	}
	
	public void addSlitGeometry(SlitAttributes att,Transform3D t) {
		if (SHAPE==FRAME_SHAPE_ELLIPSOID | SHAPE==FRAME_SHAPE_DISC) {
	// disc, ellipse
			shape.addGeometry(new OWallGeometry(1, RWallGeometry.OUTER_FACE, att, t));
			shape.addGeometry(new OWallGeometry(1, RWallGeometry.INNER_FACE, att, t));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.ENTRY,att,t));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.EXIT,att,t));
			// shape.addGeometry(new OChannelGeometry(size, exwin,thick));	
		} else {		
	// cylinder, box
			shape.addGeometry(new RWallGeometry(1, RWallGeometry.OUTER_FACE, att, t));
			shape.addGeometry(new RWallGeometry(1, RWallGeometry.INNER_FACE, att, t));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.ENTRY,att,t));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.EXIT,att,t));
			// shape.addGeometry(new RChannelGeometry(size, exwin,thick));
			
		}
	}

	/* TODO
	public void addHollowGeometry(SlitAttributes att,Transform3D t) {
		if (SHAPE==FRAME_SHAPE_DISC) {
	// disc, ellipse
			shape.addGeometry(new OWallGeometry(1, RWallGeometry.OUTER_FACE, att, t));
			shape.addGeometry(new OWallGeometry(1, RWallGeometry.INNER_FACE, att, t));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.ENTRY,att,t));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.EXIT,att,t));	
		} else if (SHAPE==FRAME_SHAPE_BOX) {
	// cylinder, box
			shape.addGeometry(new RWallGeometry(1, RWallGeometry.OUTER_FACE, att, t));
			shape.addGeometry(new RWallGeometry(1, RWallGeometry.INNER_FACE, att, t));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.ENTRY,att,t));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.EXIT,att,t));
		}
	}
	*/
	
	public static Transform3D createEulerYXY(Vector3f angles) {		
		Transform3D t1 = new Transform3D();
		Transform3D t2 = new Transform3D();
		Transform3D t3 = new Transform3D();
		t1.rotY(angles.z);
		t2.rotX(angles.y);
		t3.rotY(angles.x);
		t2.mul(t1);
		t3.mul(t2);
	/*
	 * t1.rotY(angles.x);
		t2.rotX(angles.y);
		t3.rotY(angles.z);
		t2.mul(t1);
		t3.mul(t2);
		if (angles.z != 0.0) {
			Matrix3f m = new Matrix3f();
			System.out.printf("angles %s\n", angles.toString());
			t1.get(m);
			System.out.printf("%s\n", m.toString());
			t2.get(m);
			System.out.printf("%s\n", m.toString());
			t3.get(m);
			System.out.printf("%s\n", m.toString());
		}
	*/
		return t3;	
	}
	
	/**
	 * @return number of nodes for calculation of central trajectory
	 */
	public int getCentralBeamLength() {
		return 1;
	}
	
	/**
	 * @return Array of coordinates describing the central beam in world coordinates
	 */
	public Point3f[] getCentralBeam() {
		Transform3D t = new Transform3D();
		Point3f[] b = new Point3f[1];
		b[0] = new Point3f();
		getStage().getLocalToVworld(t);
		t.transform(b[0]);
		return b;
	}
	
//	Access methods 	
	private void setGonio() {
		Transform3D t = new Transform3D(createEulerYXY(GON));
		this.gonio.setTransform(t);
	}
	
	/**
	 * Get transformation between exit axis system and true exit coordinates.
	 * Used e.g. to account for beam deflection by curved guides 
	 */
	protected Transform3D getExitTransform() {
		Transform3D t = new Transform3D(createEulerYXY(AX));
		return t;
	}
	
	private void setExitAxis() {
		this.exitAxis.setTransform(getExitTransform());
	}
		
	protected Vector3f getSize() {
		return SIZE;
	}
	
	private void setSize(Vector3f size) {
		SIZE=size;
		setGeometry();
	}
	
	/**
	 * Set stage and gonio position in required order
	 */
	protected void setMotionControl() {
		Transform3D t;
		if (ORDER==0) {
			t = new Transform3D();
			t.setTranslation(STA);
			stage.setTransform(t);
			t = new Transform3D();
			stage2.setTransform(t);						
		} else {
			t = new Transform3D();			
			stage.setTransform(t);
			t = new Transform3D();
			t.setTranslation(STA);
			stage2.setTransform(t);					
		}
		t = new Transform3D(createEulerYXY(GON));
		gonio.setTransform(t);		
	}
	
	private void setDistance() {
		Transform3D t = new Transform3D();
		t.setTranslation(new Vector3f(0.0f,0.0f,DIST));
		this.distance.setTransform(t);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCid() {
		return cid;
	}

	public void setCid(String cid) {
		this.cid = cid;
	}

	public TransformGroup getDistance() {
		return distance;
	}

	public TransformGroup getStage() {
		return stage;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		if (selected != this.selected) {
			this.selected = selected;
			setAppearance();
		}
	}

	public ClassData getCls() {
		return cls;
	}

	/**
	 * Set style to surface or wireframe. Changes only field value, not appearance
	 * @param faceStyle VIEW_FACE or VIEW_WIRE
	 */
	public void setFaceStyle(int faceStyle) {		
		if (this.faceStyle != faceStyle) {
			this.faceStyle = faceStyle;
			setAppearance();
		}
	}
	
    public int getFaceStyle() {
			return faceStyle;
	}

	public TransformGroup getScaling() {
		return scaling;
	}

	public TransformGroup getGonio() {
		return gonio;
	}
	
	public TransformGroup getStage2() {
		return stage2;
	}
    
	public TransformGroup getExitAxis() {
		return exitAxis;
	}
	
}
