package cz.restrax.view3D.geometry;

import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

public class GeometryFactory {

	public static Geometry getCylinderWall(float R, SlitAttributes att, int nseg, Transform3D t) {
		TriangleStripArray tsa=null;
		double hRange=(att.widthIn/R);
		hRange=Math.min(2*Math.PI, hRange);
		double dphi=hRange/nseg;
	//	double vRange=2.0*Math.atan2(0.5*att.heightIn,R);
	//	vRange=Math.min(Math.PI, vRange);
		Transform3D tr = new Transform3D();
		Point3f base[] = new Point3f[4];
		Vector3f baseNormal[] = new Vector3f[4];
		int sides[] = {1,-1,1,-1};
		base[0]=new Point3f(0.0f,0.5f*att.heightIn,R+0.5f*att.length);
		base[1]=new Point3f(0.0f,-0.5f*att.heightIn,R+0.5f*att.length);
		base[2]=new Point3f(0.0f,0.5f*att.heightIn,R-0.5f*att.length);
		base[3]=new Point3f(0.0f,-0.5f*att.heightIn,R-0.5f*att.length);
		baseNormal[0]=new Vector3f(0.0f,0.0f,1.0f);
		baseNormal[1]=new Vector3f(0.0f,0.0f,-1.0f);
		baseNormal[2]=new Vector3f(0.0f,1.0f,0.0f);
		baseNormal[3]=new Vector3f(0.0f,-1.0f,0.0f);
		int idx[] = {0,1,2,3,2,0,3,1};
		int nstrips=4;		
		int stripVertexCounts[] = new int[nstrips+2];
		int nc=2*(nseg+1);
		int ncoord=nstrips*nc+8;
		for (int i=0;i<nstrips;i++) {
			stripVertexCounts[i]=nc;
		}
		for (int i=nstrips;i<nstrips+2;i++) {
			stripVertexCounts[i]=4;
		}
		
		Point3f coords[]=new Point3f[ncoord];
		Vector3f normals[]=new Vector3f[ncoord];							
		int nt=0;
		double phi;
	// front, rear, top and bottom walls
		for (int ns=0;ns<nstrips;ns++) {
			for (int i=0;i<=nseg;i++) {
				phi=sides[ns]*(-0.5f*hRange+i*dphi);
				coords[nt]=(Point3f) base[idx[2*ns]].clone();
				coords[nt+1]=(Point3f) base[idx[2*ns+1]].clone();
				tr.rotY(phi);
				tr.transform(coords[nt]);
				tr.transform(coords[nt+1]);
				normals[nt]=(Vector3f) baseNormal[ns].clone();
				normals[nt+1]=(Vector3f) baseNormal[ns].clone();
				tr.transform(normals[nt]);
				tr.transform(normals[nt+1]);
				nt += 2;
			}
			
		}
	// left side
		tr.rotY(0.5f*hRange);
		for (int i=0;i<4;i++) {
			coords[nt]=(Point3f) base[i].clone();
			tr.transform(coords[nt]);
			normals[nt]= new Vector3f(1.0f,0.0f,0.0f);
			tr.transform(normals[nt]);
			nt++;
		}
	// right side
		tr.rotY(-0.5f*hRange);
		int ix[]={2,3,0,1};
		for (int i=0;i<4;i++) {
			coords[nt]=(Point3f) base[ix[i]].clone();
			tr.transform(coords[nt]);
			normals[nt]= new Vector3f(1.0f,0.0f,0.0f);
			tr.transform(normals[nt]);
			nt++;
		}

		if (t!=null) {
	    	for (int i=0;i<ncoord;i++) {
	    	  t.transform(coords[i]);			    	  
	    	  t.transform(normals[i]);
	    	}
		}
		tsa=new TriangleStripArray(ncoord,att.vertexFormat,stripVertexCounts);
		tsa.setCoordinates(0, coords);
		tsa.setNormals(0, normals);
		return tsa;
	}
}
