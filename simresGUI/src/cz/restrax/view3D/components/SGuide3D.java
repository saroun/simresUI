package cz.restrax.view3D.components;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.geometry.BladeSequenceAtt;
import cz.restrax.view3D.geometry.SGuideAttributes;

/** 
 * @author   Jan Šaroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.4 $</dt>
 *               <dt>$Date: 2013/04/06 23:15:09 $</dt></dl>
 */
public class SGuide3D extends Frame3D {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.1f, 0.9f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    protected static final Color3f reflectiveColour = new Color3f(0.9f, 0.9f, 0.9f);
    
	private SGuideAttributes Gatt;
	private BladeSequence[] walls;
	private TransformGroup[] grips;
		
	public SGuide3D(ClassData cls) {
		super(cls);
		//getScaling().setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		//getScaling().setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		//System.out.printf("SGuide3D created.\n");
	}
	
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {SGuide3D.wallColour, 
				SGuide3D.diffusiveColour,
				SGuide3D.emissiveColour, 
				SGuide3D.specularColour};
		return cc;
	}
	protected void setAppearance() {
		super.setAppearance();
		for (int i=0;i<4;i++) {
			if (getWalls()[i]!= null) getWalls()[i].setAppearance(appearance);
		}
	}	

	public TransformGroup[] getGrips() {
		if (grips==null) {
			grips = new TransformGroup[4];
			//System.out.printf("SGuide3D.getGrips()\n");
			for (int i=0;i<4;i++) {
				grips[i]=new TransformGroup();
				grips[i].setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
				grips[i].setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
				grips[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
				getScaling().addChild(grips[i]);
			}
		}
		return grips;		
	}		

	
	
	public BladeSequence[] getWalls() {
		if (walls==null) {
			walls = new BladeSequence[4];
		}
		return walls;
	}
	
	protected void readClassData(ClassData cls) {
        super.readClassData(cls);       
		
    // Only BOX shapes are allowed 
        SHAPE = FRAME_SHAPE_BOX;            
        if (Gatt == null ) {
        	int vertexFormat=QuadArray.COORDINATES | QuadArray.NORMALS | QuadArray.COLOR_3;      
			Gatt = new SGuideAttributes(cls,wallColour,vertexFormat);
        } else Gatt.importFromClass(cls);         
		rebuildLamellae();
	}
	
	
	protected Transform3D getExitTransform() {		
		Transform3D t =  Gatt.getExitTransform(createEulerYXY(AX));		
		exitTransform=t;
		return t;
	}

	protected Transform3D[] getGripTransforms() {
		Transform3D[] t=new Transform3D[4];
		int[] ori = {0,2,1,3};
		double [] x = {0.5*Gatt.widthIn,-0.5*Gatt.widthIn,0.0,0.0};
		double [] y = {0.0,0.0,0.5*Gatt.heightIn,-0.5*Gatt.heightIn};
		for (int i=0;i<t.length;i++) {
			Vector3d angles=new Vector3d(0.0,0.0,ori[i]*Math.PI/2);
			t[i] = new Transform3D();
			t[i].setEuler(angles);
			t[i].setTranslation(new Vector3d(x[i],y[i],0.0));			
		}
		return t;
	}
	
	public void setGeometry() {
		shape.removeAllGeometries();		
		BladeSequence[] w=getWalls();
		Transform3D[] tt = getGripTransforms();
		for (int i=0;i<w.length;i++) {
			if (w[i]!= null) {	
				w[i].getProperties().importFromSGuide(Gatt, i);
				w[i].setGeometry();
				grips[i].setTransform(tt[i]);
			}
		}
	}
	
	
	public void updateFromClass(ClassData cls) { 
		getGrips(); // make sure that grips are created prior to update 
    	super.updateFromClass(cls);
    }
	
	
	protected void removeBladeSequence(int i) {
		BladeSequence[] w=getWalls();		
		if (w[i] != null) {
			grips[i].removeChild(w[i]);
			w[i].detach();
			w[i].clearItems();
			w[i]=null;
			//System.out.printf("SGuide3D.removeBladeSequence(%d)\n",i);
		}
	}
	
	/**
	 * Rebuild BladeSequence objects when needed.
	 */
	public void rebuildLamellae() { 
		BladeSequence[] w=getWalls();	
		for (int i=0;i<w.length;i++) {		
			if (Gatt.active[i]>0) {
				if (w[i]==null) {
					w[i]=getBladeSequence(i);			
					w[i].compile();							
					grips[i].addChild(w[i]);
					//System.out.printf("SGuide3D.rebuildLamellae() add %d\n",i);
				} else if (w[i].needsRebuild(Gatt.nWalls)) {
					removeBladeSequence(i);				
					w[i]=getBladeSequence(i);			
					w[i].compile();
					grips[i].addChild(w[i]);	
					//System.out.printf("SGuide3D.rebuildLamellae() rebuild %d\n",i);
				}				
			} else {
				removeBladeSequence(i);				
			}
		}
	}
	
	protected BladeSequence getBladeSequence(int index) {		
		try {
			BladeSequenceAtt a = null;
			a = new BladeSequenceAtt(Gatt.color,reflectiveColour); 	
			a.importFromSGuide(Gatt, index);
			return new BladeSequence(this,a);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	
	/**
	 * Find the wall with the largest curvature
	 * or at least the 1st existing one. Return null if there is
	 * no wall visible.
	 */
	protected BladeSequence getReferenceWall() {
		BladeSequence w = null;
		int k=-1;
		int m=-1;
		double rmax=0.0;
		double r;
		BladeSequence[] ww=getWalls();
		for (int i=0;i<ww.length;i++) {
			if (ww[i]!=null) {
				if (m<0) m=i;
				r=Math.abs(ww[i].getProperties().rho[0]);
				if (r>rmax) {
					k=i;
				}
			}
		}
		if (k<0) k=m;
		if (k>=0) w=ww[k];
		return w;
	}
	

	public int getCentralBeamLength() {
		int n=2;
		BladeSequence w = getReferenceWall();
		if (w!=null) {
			n=w.getNodesNumber();			
		}
		return n;
	}
	
	public Point3f[] getCentralBeam() {
		if (Gatt != null ) {
			Transform3D t = new Transform3D();
			BladeSequence w = getReferenceWall();
			double[] z;
			if (w!=null) {
				z = w.getNodesPositions();
			} else {
				z = new double[2];
				z[0]=0.0;
				z[1]=Gatt.length;
			}			
			int n=z.length;
			Point3f[] b = new Point3f[n];
			for (int i=0;i<n;i++) {
				b[i] = new Point3f(Gatt.getPosition(z[i]));
				getStage().getLocalToVworld(t);
				t.transform(b[i]);
			}
			return b;
		} else return super.getCentralBeam();		
	}
	
	
}

