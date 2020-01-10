package cz.restrax.view3D.components;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.5 $</dt>
 *               <dt>$Date: 2013/04/06 23:15:09 $</dt></dl>
 */
import javax.media.j3d.QuadArray;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.geometry.GuideAttributes;
import cz.restrax.view3D.geometry.GuideAttributesE;
import cz.restrax.view3D.geometry.GuideAttributesP;
import cz.restrax.view3D.geometry.LamellaArrayAtt;
import cz.restrax.view3D.geometry.OFaceGeometry;
import cz.restrax.view3D.geometry.OWallGeometry;
import cz.restrax.view3D.geometry.RFaceGeometry;
import cz.restrax.view3D.geometry.RWallGeometry;

public class Guide3D extends Frame3D {
	protected static final Color3f wallColour = new Color3f(0.0f, 0.0f, 0.9f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
	private GuideAttributes Gatt;
	// private LamellaArray LHatt,LVatt;
	
	private LamellaArray lamH;
	private LamellaArray lamV;
	
	public Guide3D(ClassData cls) {
		super(cls);
		getScaling().setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		getScaling().setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
	}
	
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Guide3D.wallColour, 
				Guide3D.diffusiveColour,
				Guide3D.emissiveColour, 
				Guide3D.specularColour};
		return cc;
	}
	protected void setAppearance() {
		super.setAppearance();	
		if (lamH!= null) lamH.setAppearance();
		if (lamV!= null) lamV.setAppearance();
	}

	protected void readClassData(ClassData cls) {
        super.readClassData(cls);
        int type=GuideAttributes.GUIDE_TYPE_SOLLER;
		try {
			type = (Integer) cls.getField("TYPE").getValue();
		} catch (Exception e) {
			e.printStackTrace();
		}
    // Only BOX and DISC shapes are allowed 
        if (SHAPE != FRAME_SHAPE_DISC) SHAPE = FRAME_SHAPE_BOX;  
        int vertexFormat=QuadArray.COORDINATES | QuadArray.NORMALS;
        if (Gatt == null || Gatt.type != type) {
        	switch (type) {
        		case GuideAttributes.GUIDE_TYPE_PARA:
        			Gatt = new GuideAttributesP(cls,null,vertexFormat);
        			break;
        		case GuideAttributes.GUIDE_TYPE_PARA2:
        			Gatt = new GuideAttributesP(cls,null,vertexFormat);
        			break;
        		case GuideAttributes.GUIDE_TYPE_ELL:
        			Gatt = new GuideAttributesE(cls,null,vertexFormat);
        			break;
        		default:
        			Gatt = new GuideAttributes(cls,null,vertexFormat);        		
        	}        	
        } else Gatt.importFromClass(cls);  
		rebuildLamellae();
        if (lamH!= null) lamH.getProperties().importFromGuide(Gatt);
		if (lamV!= null) lamV.getProperties().importFromGuide(Gatt);
	}
	
	
	protected Transform3D getExitTransform() {		
		Transform3D t =  Gatt.getExitTransform(createEulerYXY(AX));		
		exitTransform=t;
		return t;
	}

	public void setGeometry() {
		shape.removeAllGeometries();
		int nseg=Gatt.getOptimumSegments();
	// round
		if (SHAPE == FRAME_SHAPE_DISC) {
		//	shape.addGeometry(new OChannelGeometry(SIZE,EX,thick));			
			shape.addGeometry(new OWallGeometry(nseg, RWallGeometry.OUTER_FACE, Gatt, null));
			shape.addGeometry(new OWallGeometry(nseg, RWallGeometry.INNER_FACE, Gatt, null));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.ENTRY,Gatt,null));
			shape.addGeometry(new OFaceGeometry(RFaceGeometry.EXIT,Gatt,null));
	// rectangular			
		} else {
			shape.addGeometry(new RWallGeometry(nseg, RWallGeometry.OUTER_FACE, Gatt, null));
			shape.addGeometry(new RWallGeometry(nseg, RWallGeometry.INNER_FACE, Gatt, null));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.ENTRY,Gatt,null));
			shape.addGeometry(new RFaceGeometry(RFaceGeometry.EXIT,Gatt,null));
			if (lamH!= null) lamH.setGeometry();
			if (lamV!= null) lamV.setGeometry();
		}
	}
	
	
	public void updateFromClass(ClassData cls) { 
    	super.updateFromClass(cls);
    }
	
	/**
	 * Rebuild LamellaArray objects when needed.
	 * @param cls
	 */
	public void rebuildLamellae() { 
   // update horizontal lamellae	
	if (SHAPE==FRAME_SHAPE_DISC) {
		if (lamH != null) {
			getScaling().removeChild(lamH);
		    lamH.detach();
		    lamH.clearItems();
		    lamH=null;
		}
		if (lamV != null) {
			getScaling().removeChild(lamV);
		    lamV.detach();
		    lamV.clearItems();
		    lamV=null;
		}
	} else {
		if (lamH == null) {
			lamH = new LamellaArray(this,LamellaArrayAtt.HORIZONTAL_ARRAY, Gatt);
		    lamH.compile();
			getScaling().addChild(lamH);
		} else if (lamH.needsRebuild(Gatt)) {
			getScaling().removeChild(lamH);
		    lamH.detach();
		    lamH.clearItems();
		    lamH=null;
			lamH = new LamellaArray(this,LamellaArrayAtt.HORIZONTAL_ARRAY, Gatt);
		   	lamH.compile();
		   	getScaling().addChild(lamH);
		} 
   // update vertical lamellae 	
		if (lamV == null) {
			lamV = new LamellaArray(this,LamellaArrayAtt.VERTICAL_ARRAY, Gatt);
			lamV.compile();
			getScaling().addChild(lamV);
		} else if (lamV.needsRebuild(Gatt)) {
			getScaling().removeChild(lamV);
			lamV.detach();
			lamV.clearItems();
			lamV=null;			
			lamV = new LamellaArray(this,LamellaArrayAtt.VERTICAL_ARRAY, Gatt);
		  	lamV.compile();
		  	getScaling().addChild(lamV);
		} 
	}
	}
	
	public int getCentralBeamLength() {
		if (Gatt != null ) {
			return Gatt.getOptimumSegments()+1;
		}	else return super.getCentralBeamLength();
	}
	
	public Point3f[] getCentralBeam() {
		if (Gatt != null ) {
			Transform3D t = new Transform3D();
			int n=getCentralBeamLength();
			Point3f[] b = new Point3f[n];
			double z;
			for (int i=0;i<n;i++) {
				z=i*Gatt.length/(n-1);
				b[i] = new Point3f(Gatt.getPosition(z));
				getStage().getLocalToVworld(t);
				t.transform(b[i]);
			}
			return b;
		} else return super.getCentralBeam();		
	}
	
	public LamellaArray getLamH() {
		return lamH;
	}
	public LamellaArray getLamV() {
		return lamV;
	}
	
}
// DHL 471 2479 310	
