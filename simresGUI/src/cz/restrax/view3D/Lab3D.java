package cz.restrax.view3D;

import java.awt.GraphicsConfiguration;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.LineAttributes;
import org.jogamp.java3d.PointLight;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.View;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

//import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.behaviors.vp.ViewPlatformBehavior;
import org.jogamp.java3d.utils.universe.SimpleUniverse;

import cz.jstools.classes.ClassData;
import cz.restrax.sim.Instrument;
import cz.restrax.view3D.components.CoordAxes;
import cz.restrax.view3D.components.Axis;
import cz.restrax.view3D.components.Floor;
import cz.restrax.view3D.components.Frame3D;
import cz.restrax.view3D.components.FrameShape;
import cz.restrax.view3D.components.Trajectory;
import cz.restrax.view3D.OrbitBehavior;

public class Lab3D {
	public static final int STYLE_LAYOUT=1;
	public static final int STYLE_3D=0;
	public static final float AXIS_LENGTH=8.0f;
	private static final BoundingSphere BOUNDS = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 400.0);
    private SimpleUniverse universe = null;
    private OrbitBehavior orbit = null;  // for 3D navigation
    private OrbitBehavior travel = null; 
   // private ScreenScaleBehavior scaling = null; // for 2D layout
	private OrbitBehavior scaling = null; // for 2D layout
    private Canvas3D canvas = null;
    private BranchGroup scene = null;
    private BranchGroup axesBranch = null;
    private BranchGroup floorBranch = null;
    private BranchGroup beamBranch = null;
    private Trajectory beam = null;
	private Frame3DCollection  instrument3D  = null;
//	private Instrument spectrometer = null;
	private Frame3D focusObject = null;
	private int style = STYLE_3D;
	private Axis xaxis = null;
	private Axis yaxis = null;
	private Axis zaxis = null;
	private TransformGroup origin=null ;
	private Shape3D floor = null;
	private int faceStyle = FrameShape.VIEW_FACE;
    

	public Lab3D (Frame3DCollection  instrument3D) {
		createUniverse();
		this.instrument3D=instrument3D;
		if (instrument3D!=null) reset(instrument3D.getSpectrometer());
    }    
		
	private void createUniverse() {
    	// Get the preferred graphics configuration for the default screen
    	GraphicsConfiguration config =SimpleUniverse.getPreferredConfiguration();
    	// Create a Canvas3D using the preferred configuration
    	canvas = new Canvas3D(config);
    	// Create simple universe with view branch
    	universe = new SimpleUniverse(canvas);
    	universe.getViewingPlatform().getViewPlatform().setViewAttachPolicy(View.NOMINAL_HEAD);
    	universe.getViewingPlatform().setNominalViewingTransform();
    	universe.getViewingPlatform().setViewPlatformBehavior(getOrbit());
    	universe.getViewer().getView().setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
    	universe.getViewer().getView().setMinimumFrameCycleTime(10);  
    	universe.getViewer().getView().setFrontClipDistance(0.02);	
    // Coordinate axes    
    	showAxes(true);
    	showFloor(true);
    //	System.out.printf("Lab3D.createUniverse\n");
    }	

	
	private void showBranch(BranchGroup branch, boolean visible) {
		if (branch.getCapability(BranchGroup.ALLOW_DETACH)) {
			if (visible) {
				if (! branch.isLive()) {
					universe.addBranchGraph(branch);
				}
			} else {
				if (branch.isLive()) {
					universe.getLocale().removeBranchGraph(branch);
					branch.detach();
				}
			}
		}
	}
	
	public void showBeam(boolean visible) {
		updateBeam();
		if (beamBranch == null) {
			beamBranch=new BranchGroup();
	    	beamBranch.setCapability(BranchGroup.ALLOW_DETACH);
	    	beamBranch.addChild(beam);
	    	beamBranch.compile();
		}
		showBranch(beamBranch,visible);		
	}
	
	/**
	 * Calculate current central beam trajectory and update (or create new) <b>Trajectory beam</b>.
	 */
	public void updateBeam() {
		Point3f[] nodes;
		if (instrument3D != null) {
			nodes=instrument3D.getCentralBeam();
		} else {
			nodes = new Point3f[] {new Point3f(0.0f,0.0f,0.0f),new Point3f(0.0f,0.0f,0.1f)};
		}		  
		if (beam == null) {
			beam = new Trajectory(nodes);
		} else {
			beam.updateGeometry(nodes);
		}
	}
	
	public void showAxes(boolean visible) {		
		if (axesBranch == null) {
			axesBranch = new CoordAxes(AXIS_LENGTH,1.0f,LineAttributes.PATTERN_DASH);
		}
		showBranch(axesBranch,visible);	
		// printBranches();		
	}
	
	public void showFloor(boolean visible) {
		if (floorBranch == null) {
			floorBranch=new BranchGroup();
			floor=new Floor();	    	
			floorBranch.addChild(floor);
			floorBranch.setCapability(BranchGroup.ALLOW_DETACH);	
			floorBranch.compile();
		}
		showBranch(floorBranch,visible);	
	}
	
	
	
    private void createScene(TransformGroup objects) {
    	scene = new BranchGroup();
    	scene.setCapability(
    	//  BranchGroup.ALLOW_CHILDREN_WRITE |
    	  BranchGroup.ALLOW_DETACH);
  
  // Background
    	Color3f bgColor = new Color3f(0.05f, 0.05f, 0.05f);
    	Background bg = new Background(bgColor);
    	bg.setApplicationBounds(BOUNDS);
    	scene.addChild(bg);

  // Global lights
    // ambiente    
   	    Color3f alColor = new Color3f(0.3f, 0.3f, 0.3f);
    	AmbientLight aLgt = new AmbientLight(alColor);
    	aLgt.setInfluencingBounds(BOUNDS);
    	scene.addChild(aLgt);
    // directional
    	Color3f lColor1 = new Color3f(0.7f, 0.7f, 0.7f);
    	Vector3f lDir1  = new Vector3f(-1.0f, -1.0f, -1.0f);
    	DirectionalLight lgt1 = new DirectionalLight(lColor1, lDir1);
    	lgt1.setInfluencingBounds(BOUNDS);
    	scene.addChild(lgt1);
    // point	    	
    	Color3f lColor2 = new Color3f(1.0f, 1.0f, 1.0f);
    	Point3f lPos2  = new Point3f(0.0f, 5.0f, 0.0f);
    	Point3f lAtt2  = new Point3f(1.0f, -1.0f, 0.0f);
    	PointLight lgt2 = new PointLight(lColor2, lPos2,lAtt2);    	
    	lgt2.setInfluencingBounds(BOUNDS);
    	scene.addChild(lgt2);
    	
   // source origin (1 m above floor)	
    	Transform3D t = new Transform3D();
    	t.setTranslation(new Vector3d(0.0,1.0,0.0));
    	origin=new TransformGroup(t);
    //	origin.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    //	origin.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);

   // add view objects 	
    	origin.addChild(objects);
    	scene.addChild(origin);
   
   // add my pick behavior 	
    	PickFrame3DBehavior pickb = new PickFrame3DBehavior(this, BOUNDS);
    	scene.addChild(pickb);
    	
   // Have Java 3D perform optimizations on this scene graph.
    	scene.compile();
    //	System.out.printf("Lab3D.createScene \n");
        }
    
    public void reset(Instrument spectrometer) {
    	if (instrument3D != null) {
    		showBeam(false);
    	//	this.spectrometer=spectrometer;
    	//	instrument3D = new Frame3DCollection(spectrometer.getCfgTitle());
		//	instrument3D.importClassCollection(spectrometer.getPrimarySpec());
		//	instrument3D.importClassCollection(spectrometer.getSpecimen());
		//	instrument3D.importClassCollection(spectrometer.getSecondarySpec());	
		//	instrument3D.createGroup();
			if (scene != null) {
				universe.getLocale().removeBranchGraph(scene);
				scene.detach();
			}
    		instrument3D.reset(spectrometer);
			createScene(instrument3D.getTopGroup());
			universe.addBranchGraph(scene);
			showBeam(true);
			focusAt(instrument3D.getDefaultFocusObject());	
    	}
    //	System.out.printf("Lab3D.reset spectrometer=%s\n",spectrometer);
    }
        
    
    public void update() {
    	if (instrument3D != null) {
    		instrument3D.update();    		
			updateBeam();
			centerAt(focusObject);			
    	}
    }
    
    /**
     * Focus viewer at given component
     */
    protected void focusAt(Frame3D f) {
    	TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
    	Transform3D tloc = new Transform3D();    		
    	if (f != null) f.getStage().getLocalToVworld(tloc);
    	Transform3D tshift = getDefaultViewerTransform();
    	tloc.mul(tshift);
    	vpTrans.setTransform(tloc);
    	// mark the object as selected and set viewer rotation axis at the object's stage
    	setFocusObject(f);    	    	
    }
    
    /**
     * Focus 3D viewer at given ClassData object
     */
    public void focusAtClass(ClassData obj) {
    	if (instrument3D != null) {
    		Frame3D f = instrument3D.getFrame3D(obj);
    		focusAt(f);
    	}    		    	
    }
    
    /**
     * Center viewer rotation at given component
     */
    protected void centerAt(Frame3D f) {
    	ViewPlatformBehavior b = universe.getViewingPlatform().getViewPlatformBehavior();
    	if (b != null) {
    		OrbitBehavior o = (OrbitBehavior) b;
        	Point3d center = new Point3d(0.0,0.0,0.0);
        	if (f != null) {
        		Transform3D t = new Transform3D();
        		f.getStage().getLocalToVworld(t);
        		t.transform(center);
        	}
        	o.setRotationCenter(center);
    	}    		
    }
    
    protected void centerAtTrans(Transform3D trans) {
    	if (trans != null) {
    		Point3d center = new Point3d(0.0,0.0,0.0);
        	trans.transform(center);
        	getOrbit().setRotationCenter(center); 
    	}
    }
    
    
    public void resetViewer() {
    	focusAt(focusObject);
    }
    
/*                        ACCESS METHODS */	
	public SimpleUniverse getUniverse() {
		return universe;
	}

	protected OrbitBehavior getOrbit() {
		if (orbit == null) {
			orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
	    	orbit.setRotYFactor(0.2);
	    	orbit.setProportionalZoom(false);
	    	orbit.setZoomFactor(0.2);	    	
	    	orbit.setRotationCenter(new Point3d(0.0, 0.0, 0.0));	    
	    	orbit.setSchedulingBounds(BOUNDS);
		}
		return orbit;
	}
	
	protected OrbitBehavior getTravel() {
		if (travel == null) {
			travel  = new OrbitBehavior(canvas, 
					OrbitBehavior.REVERSE_ALL | 
					OrbitBehavior.DISABLE_ROTATE); // | 
					//OrbitBehavior.DISABLE_ZOOM);
			travel .setProportionalZoom(true);
			travel .setZoomFactor(0.2);	    	
			travel .setRotationCenter(new Point3d(0.0, 0.0, 0.0));	    	
	    	travel .setSchedulingBounds(BOUNDS);
		}
		return travel ;
	}

	protected OrbitBehavior getScaling() {
		if (scaling == null) {
			scaling  = new OrbitBehavior(canvas,
					OrbitBehavior.REVERSE_ALL | 
					OrbitBehavior.DISABLE_ROTATE);
			scaling .setZoomFactor(0.2);	    	
	    	scaling .setSchedulingBounds(BOUNDS);
		}
		return scaling ;
	}
	
	public Canvas3D getCanvas() {
		return canvas;
	}

	public BranchGroup getScene() {
		return scene;
	}

	public void setFocusObject(Frame3D focusObject) {
		if (this.focusObject != focusObject) {
			if (this.focusObject != null) this.focusObject.setSelected(false);
			this.focusObject = focusObject;
			centerAt(this.focusObject);
			if (this.focusObject != null) this.focusObject.setSelected(true);
		} else if (focusObject == null) centerAt(null);
	}
	
	/**
	 * Like setFocusObject(Frame3D focusObject), but centers at the position given by trans
	 * instead of the focusObject origin
	 * @param focusObject
	 * @param trans
	 */
	public void setFocusObject(Frame3D focusObject, Transform3D trans) {
		if (this.focusObject != focusObject) {
			if (this.focusObject != null) this.focusObject.setSelected(false);
			this.focusObject = focusObject;
			centerAtTrans(trans);
			if (this.focusObject != null) this.focusObject.setSelected(true);
		} else if (focusObject == null) centerAt(null);
	}

	protected Vector3d getDefaultViewerPosition() {
		if (style == STYLE_LAYOUT) {    		
    		return new Vector3d(0.0,2.0,0.0);
		} else {
			return new Vector3d(0.0,0.2,-0.6);
		}
	}
	
	protected Vector3d getDefaultViewerAngle(Vector3d position) {
		if (style == STYLE_LAYOUT) {    		
    		return new Vector3d(-Math.PI/2,0.0,0.0);
		} else {
			return new Vector3d(Math.atan2(-position.y,-position.z),Math.PI,0.0);
		}
	}
	
	protected Transform3D getDefaultViewerTransform() {
		Transform3D tshift = new Transform3D();
		Vector3d position = getDefaultViewerPosition();
		tshift.setEuler(getDefaultViewerAngle(position));		
		tshift.setTranslation(position);
		return tshift;
	}
	
	public void setStyle(int style) {
		this.style=style;
		if (style == STYLE_LAYOUT) {			
			universe.getViewer().getView().setProjectionPolicy(View.PARALLEL_PROJECTION);
			universe.getViewer().getView().setScreenScalePolicy(View.SCALE_EXPLICIT);
			universe.getViewer().getView().setScreenScale(1.0);
		//	universe.getViewingPlatform().setViewPlatformBehavior(getTravel());	
			universe.getViewingPlatform().setViewPlatformBehavior(getScaling());	
		//	MouseScale ms = new MouseScale(universe.getViewingPlatform().getViewPlatformTransform());
		//	universe.getViewer().getViewingPlatform().addViewPlatformBehavior(ms);
		} else {
			universe.getViewer().getView().setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
			universe.getViewer().getView().setScreenScalePolicy(View.SCALE_SCREEN_SIZE);
			universe.getViewingPlatform().setViewPlatformBehavior(getOrbit());
		}
		focusAt(instrument3D.getDefaultFocusObject());
	/*	
		TransformGroup vpTrans = universe.getViewingPlatform().getViewPlatformTransform();
		Transform3D tloc=new Transform3D();
		vpTrans.getTransform(tloc);
    	Point3d v = new Point3d(0.0,0.0,0.0);
    	Vector3d d = new Vector3d(0.0,0.0,1.0);
    	tloc.transform(v);
    	tloc.transform(d);
    	System.out.printf("viewer at %s\n", v);
    	System.out.printf("looking at %s\n", d);
    	*/
	}

	public Frame3D getFocusObject() {
		return focusObject;
	}

	/**
	 * Set style of Frame objects (face or wireframe).
	 * @param faceStyle either Frame3D.VIEW_WIRE or Frame3D.VIEW_FACE
	 */
	public void setFaceStyle(int faceStyle) {
		if (this.faceStyle != faceStyle) {
			this.faceStyle = faceStyle;
			instrument3D.setFaceStyle(faceStyle);
		}
	}
	
}
