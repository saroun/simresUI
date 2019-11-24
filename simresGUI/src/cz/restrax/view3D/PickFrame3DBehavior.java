package cz.restrax.view3D;

import javax.media.j3d.Bounds;
import javax.media.j3d.Node;

import com.sun.j3d.utils.picking.PickResult;
import com.sun.j3d.utils.picking.PickTool;
import com.sun.j3d.utils.picking.behaviors.PickMouseBehavior;

import cz.restrax.view3D.components.Frame3D;
import cz.restrax.view3D.components.FrameShape;

public class PickFrame3DBehavior extends PickMouseBehavior {
	Lab3D owner=null;
	public PickFrame3DBehavior(Lab3D owner, Bounds bounds) {
		super(owner.getCanvas(), owner.getScene(), bounds);
		this.owner=owner;
		this.setSchedulingBounds(bounds);
		// owner.getScene().addChild(this);
	    pickCanvas.setMode(PickTool.GEOMETRY);
	}

	public void updateScene(int xpos, int ypos) {
		PickResult pickResult = null;
		Node node=null;
		pickCanvas.setShapeLocation(xpos, ypos);
		pickResult = pickCanvas.pickClosest();
		if (pickResult != null) {
		    node = pickResult.getNode(PickResult.SHAPE3D);
		    if (node instanceof FrameShape) {		    	
		    	Frame3D f = ((FrameShape) node).getFrame();
		    	if ( ! f.isSelected()) {
		    		owner.setFocusObject(f,pickResult.getLocalToVworld());
		    		//owner.setFocusObject(f);
		    		//System.out.printf("selected: %s \n",f.getId());
		    	}
		    	
		    	
		    }
		}		
	}

}
