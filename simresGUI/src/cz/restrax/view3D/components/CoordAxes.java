package cz.restrax.view3D.components;

import org.jogamp.java3d.BranchGroup;


public class CoordAxes extends BranchGroup {
	private Axis A,B,C;
	private float length,width;
	private int style;
	
	public CoordAxes(float len, float width, int style) {
		super();
		this.length=len;
		this.width=width;
		this.style=style;
		A=new Axis(0,len,width,style);
    	B=new Axis(1,len,width,style);
    	C=new Axis(2,len,width,style);    	
    	this.addChild(A);
    	this.addChild(B);
    	this.addChild(C);
    	setName("CoordAxes");
    	setCapability(BranchGroup.ALLOW_DETACH);
    	compile();
	}

	public Axis getA() {
		return A;
	}

	public Axis getB() {
		return B;
	}

	public Axis getC() {
		return C;
	}

	public float getLength() {
		return length;
	}
}
