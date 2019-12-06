package cz.restrax.view3D.components;

import javax.media.j3d.Geometry;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import cz.jstools.classes.ClassData;
import cz.restrax.view3D.Utils;
import cz.restrax.view3D.geometry.SlitAttributes;


public class Chopper3D extends Frame3D  {
	protected static final Color3f wallColour = new Color3f(0.8f, 0.8f, 0.5f);
	protected static final Color3f diffusiveColour = new Color3f(wallColour);
    protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
    protected static final Color3f specularColour = new Color3f(0.3f, 0.3f, 0.3f);
    
	protected int PROF; // profile
	protected int ORI; // orientation
	protected int NWIN; // number of windows
	protected float WIN; // window width
	protected float PHASE; // phase
	protected float RAD; // radius
	protected String timing;
	protected float[] phases;
	protected float[] widths;
	private SlitAttributes att;
	
	public Chopper3D(ClassData cls) {
		super(cls);		
	}
	public Color3f[] getDefaultColors() {
		Color3f[] cc =  {Chopper3D.wallColour, 
				Chopper3D.diffusiveColour,
				Chopper3D.emissiveColour, 
				Chopper3D.specularColour};
		return cc;
	}
	
	protected void readClassData(ClassData cls) {
		 super.readClassData(cls);
			try {
				int prof=(Integer) cls.getField("PROF").getValue();
				PROF=prof;
				ORI=(Integer) cls.getField("ORI").getValue();
				NWIN=(Integer) cls.getField("NWIN").getValue();
				PHASE=((Double) cls.getField("PHASE").getValue()).floatValue();
				WIN=((Double) cls.getField("WIN").getValue()).floatValue();
				RAD=((Double) cls.getField("RAD").getValue()).floatValue();
				timing=(String) cls.getField("TIMING").getValue();
				timing=timing.replaceAll("\n", "");
				timing=timing.replaceAll("\r", "");
				timing=timing.replaceAll("[ ]+","");
				calcPhasing();
			} catch (Exception e) {
				e.printStackTrace();
			}
			RAD=RAD*0.001f;
			if (att == null) {
				int vf=TriangleArray.COORDINATES | TriangleArray.NORMALS | TriangleArray.COLOR_3;
	        	att = new SlitAttributes(cls,new Color3f(0.5f, 0.5f, 0.2f),vf);
	        } else att.importFromClass(cls); 
	        att.thframe=0.01f;
	}
	
	/**
	 * Calculate phases and slit widths from timing string
	 */
	protected void calcPhasing() {
		String[] items=timing.split("[:]");
		int n = items.length/2;
		float ph=0.0f;
		float w=0.0f;
		NWIN=n;
		phases=new float[n+1];
		widths=new float[n+1];
		for (int i=0;i<n;i++) {
			try {
			ph=Float.parseFloat(items[2*i]);
			w=Float.parseFloat(items[2*i+1]);
			phases[i]=ph;
			widths[i]=w;
			} catch (Exception e) {
				System.err.printf("calcPhasing: i=%d, (%s,%s)\n",i,items[2*i],items[2*i+1]);
				break;
			}
		}
		phases[n]=phases[0];
		widths[n]=widths[0];
	}
	
	public void setGeometry() {
		shape.removeAllGeometries();
		// double phi=Math.PI*WIN;
		float R1=(float) ((RAD-SIZE.y/2.0f)*0.9);
		float R2=(float) (2.0*RAD-R1);
		Transform3D t = new Transform3D();	
		t.setTranslation(new Vector3d(0.0d,-(R1+R2)/2,0.0d));
	//	int nseg=Math.min(1,Utils.CSEG/NWIN-1);
	//shape.addGeometry(new DiscChopperGeometry(R1, R2, WIN,nseg, NWIN, 1,t));
	//	shape.addGeometry(new DiscChopperGeometry(R1, R2, WIN,nseg, NWIN, -1,t));
		shape.addGeometry(getChopperFace(R1, R2, WIN, NWIN, 1,t));
		shape.addGeometry(getChopperFace(R1, R2, WIN, NWIN, -1,t));
		t.setTranslation(new Vector3d(0.0d,0.0d,SIZE.z/2.0f));		
		addSlitGeometry(att,t);
		t.setTranslation(new Vector3d(0.0d,0.0d,-SIZE.z/2.0f));		
		addSlitGeometry(att,t);
	}
	
	private Geometry getChopperFace_old(float R1, float R2, float win, int nwin, int side,Transform3D t) {
		TriangleFanArray tfa;
		float sgn=Math.signum(side);
		int nseg=Math.max(1,Utils.CSEG/NWIN-1);
		// int nseg=Math.max(1,5/NWIN-1);
		double twopi=2.0d*Math.PI;
		double phi = sgn*twopi*win/2;
		double dphi = sgn*twopi/nwin;
		double dseg = sgn*twopi*(1.0d/nwin-WIN)/nseg;
		int stripCounts[] = new int[nwin*2];
		Point3f base[] = new Point3f[3];
		Vector3f baseNormal = new Vector3f(0.0f,0.0f,sgn);
		Transform3D tr = new Transform3D();
		int ncoord=0;
		float th=sgn*0.00001f;
		for (int i=0;i<nwin*2;i+=2) {
			stripCounts[i]=3;
			stripCounts[i+1]=nseg+2;
			// we need (n+2) vertices for a strip of n triangles
			ncoord += stripCounts[i]+stripCounts[i+1];
		}
		tfa=new TriangleFanArray(ncoord,att.vertexFormat,stripCounts);
		Point3f coords[] = new Point3f[ncoord];
		Vector3f normals[] = new Vector3f[ncoord];
		base[0]=new Point3f(0.0f,0.0f,th);
		base[1]=new Point3f(0.0f,R1,th);
		base[2]=new Point3f(0.0f,R2,th);
		int nt=0;
		double phase=0.0d;
		for (int nw=0;nw<nwin;nw++) {
			phase=nw*dphi;
	// triangle of the window
		// central point, axis
			coords[nt]=(Point3f) base[0].clone(); // can be shared by all without cloning ?		
			// left and right vertex
			for (int i=1;i<=2;i++) {
				coords[nt+i]=(Point3f) base[1].clone();
				tr.rotZ(phase+(2*i-3)*phi);
				tr.transform(coords[nt+i]);
			}
			for (int i=0;i<3;i++) {
				normals[nt+i]=(Vector3f) baseNormal.clone();
			}
			nt +=3;
	// triangles of the  full section
			// central point, axis
			coords[nt]=(Point3f) base[0].clone(); 
			normals[nt]=(Vector3f) baseNormal.clone();
			nt++;
			for (int ns=0;ns<=nseg;ns++) {
				// central point, axis
				coords[nt]=(Point3f) base[2].clone();
				tr.rotZ(phase+phi+ns*dseg);
				tr.transform(coords[nt]);
				normals[nt]=(Vector3f) baseNormal.clone();
				nt++;
			}			
		}
		if (t!=null) {
	    	for (int i=0;i<ncoord;i++) {
	    	  t.transform(coords[i]);			    	  
	    	  t.transform(normals[i]);
	    	}
		}
		tfa.setCoordinates(0, coords);
		tfa.setNormals(0, normals);
		return tfa;
	}
	
	
	private Geometry getChopperFace(float R1, float R2, float win, int nwin, int side,Transform3D t) {
		TriangleFanArray tfa;
		float sgn=Math.signum(side);
		int isgn = Math.round(sgn);
		int[] nseg=new int[nwin];
		double[] left = new double[nwin+1];
		double[] right = new double[nwin];
		double[] dphi = new double[nwin];
		
		// int nseg=Math.max(1,5/NWIN-1);
		double twopi=2.0d*Math.PI;
		//double phi = sgn*twopi*win/2;
		//double dphi = sgn*twopi/nwin;
		//double dseg = sgn*twopi*(1.0d/nwin-WIN)/nseg;
		int stripCounts[] = new int[nwin*2];
		Point3f base[] = new Point3f[3];
		Vector3f baseNormal = new Vector3f(0.0f,0.0f,sgn);
		Transform3D tr = new Transform3D();
		int ncoord=0;
		float th=sgn*0.0001f;
		for (int i=0;i<nwin;i++) {
			left[i]=phases[i]-widths[i]/2*win;
			right[i]=phases[i]+widths[i]/2*win;
		}
		left[nwin]=left[0]+1;
		for (int i=0;i<nwin;i++) {
			dphi[i]=Math.abs(left[i+1]-right[i]);
			nseg[i]=(int) Math.round(Utils.CSEG*dphi[i]);
			nseg[i]=Math.max(1,nseg[i]);
			stripCounts[2*i]=3;
			// we need (n+2) vertices for a strip of n triangles
			stripCounts[2*i+1]=nseg[i]+2;
			ncoord += stripCounts[2*i]+stripCounts[2*i+1];
		}
		tfa=new TriangleFanArray(ncoord,att.vertexFormat,stripCounts);
		Point3f coords[] = new Point3f[ncoord];
		Vector3f normals[] = new Vector3f[ncoord];
		Color3f colors[] = new Color3f[ncoord];
		base[0]=new Point3f(0.0f,0.0f,th);
		base[1]=new Point3f(0.0f,R1,th);
		base[2]=new Point3f(0.0f,R2,th);
		int nt=0;
		double phase;
		double phi;
		double dseg;
		for (int nw=0;nw<nwin;nw++) {
			phase=twopi*(right[nw]+left[nw])/2.0d;
			phi=twopi*(right[nw]-left[nw])/2.0d;
	// triangle of the window
		// central point, axis
			coords[nt]=(Point3f) base[0].clone(); // can be shared by all without cloning ?		
			// left and right vertex			
			for (int i=1;i<=2;i++) {
				coords[nt+i]=(Point3f) base[1].clone();
				tr.rotZ(phase+isgn*(2*i-3)*phi);
				tr.transform(coords[nt+i]);
			}
			for (int i=0;i<3;i++) {
				normals[nt+i]=(Vector3f) baseNormal.clone();
			}
			nt +=3;
	// triangles of the  full section
			// central point, axis
			coords[nt]=(Point3f) base[0].clone(); 
			normals[nt]=(Vector3f) baseNormal.clone();
			nt++;
			phase=twopi*right[nw];
			int imax=nseg[nw];
			int imin=0;
			// turn rotation for the opposite side
			if (isgn<0) {
				imin=imax;
				imax=0;
			}
			for (int ns=imin;ns*isgn<=imax;ns=ns+isgn) {
				dseg = twopi*(left[nw+1]-right[nw])/nseg[nw];
				coords[nt]=(Point3f) base[2].clone();
				tr.rotZ(phase+ns*dseg);
				tr.transform(coords[nt]);
				normals[nt]=(Vector3f) baseNormal.clone();
				nt++;
			}			
		}
		if (t!=null) {
	    	for (int i=0;i<ncoord;i++) {
	    	  t.transform(coords[i]);			    	  
	    	  t.transform(normals[i]);
	    	}
		}
		tfa.setCoordinates(0, coords);
		tfa.setNormals(0, normals);
		for (int i=0;i<ncoord;i++) {
			tfa.setColor(i,wallColour);		    	  
	    }
		return tfa;
	}
	
}
