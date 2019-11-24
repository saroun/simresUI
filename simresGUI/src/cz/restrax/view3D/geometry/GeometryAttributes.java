package cz.restrax.view3D.geometry;

import javax.media.j3d.QuadArray;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;


public abstract class GeometryAttributes {
	   public Color3f color;
	   public int vertexFormat;

	   public GeometryAttributes(Color3f color, int vertexFormat) {
		   this.color=color;
		   this.vertexFormat=vertexFormat;
		   setDefault();
	   }	   
	   public GeometryAttributes() {
			super();			
			this.color=new Color3f(0.5f, 0.5f, 0.5f);
			this.vertexFormat=QuadArray.COORDINATES | QuadArray.NORMALS;
			setDefault();
	   }
	   protected abstract void setDefault();
	   public abstract Vector3d getAngle(double z);	   
	   public abstract Vector3d getPosition(double z);
}
