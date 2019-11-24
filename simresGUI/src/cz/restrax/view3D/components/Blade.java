package cz.restrax.view3D.components;

import cz.restrax.view3D.geometry.BladeAtt;
import cz.restrax.view3D.geometry.BladeGeometry;

public class Blade extends FrameShape {   

    protected BladeAtt att;
    
    
	public Blade(Frame3D frame) {
		super(frame);
	}

	public void setGeometry(BladeAtt att) {
		this.att=att;
		removeAllGeometries();
		addGeometry(new BladeGeometry(att));		
	}
}
