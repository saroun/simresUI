package cz.restrax.view3D.geometry;

import javax.vecmath.Vector3d;

public class LamellaArrayAttE extends LamellaArrayAtt {

	public double focH;
    public double focV;
    public double tanH;
    public double tanV;
	public LamellaArrayAttE(int orientation, GuideAttributes att) throws Exception {
		super(orientation, att);
		if (!(att instanceof GuideAttributesE)) {
			throw new Exception("LamellaArrayAttE requires GuideAttributesE in constructor argument");
		}
	}

	@Override
	public void importFromGuide(GuideAttributes att) {
		   super.importFromGuide(att);
		   if (orientation == HORIZONTAL_ARRAY) {
			   focH=((GuideAttributesE) att).focH; 
			   focV=((GuideAttributesE) att).focV; 
			   tanH=((GuideAttributesE) att).tanH; 
			   tanV=((GuideAttributesE) att).tanV; 
		   } else if (orientation == VERTICAL_ARRAY) {
			   focH=((GuideAttributesE) att).focV; 
			   focV=((GuideAttributesE) att).focH;
			   tanH=((GuideAttributesE) att).tanV; 
			   tanV=((GuideAttributesE) att).tanH; 
		   }
	}
	
	@Override
	public Vector3d getLamellaAngle(int index,double z) {
		double ksi = index*1.0/nl-0.5;		
		Vector3d p = new Vector3d();
		if ( focH == 0 ) {
			p.y=ksi*dwidth;
		} else {
			double epsilon = GuideAttributesE.getEpsilon(widthIn,widthOut, length, tanH);
			double z0 = GuideAttributesE.getEllEntry(widthIn,widthOut, length, tanH);
			p.y=GuideAttributesE.getProfileAngle(2*ksi*epsilon,z0,length,focH,z);
		}					
		return p;
	}
		
	@Override
	public Vector3d getLamellaPos(int index,double z) {	
		double ksi = index*1.0/nl-0.5;		
		Vector3d p = new Vector3d();
		if ( focH == 0 ) {
			p.x=(widthIn+dwidth*z)*(index*1.0/nl-0.5);
		} else {
			double epsilon = GuideAttributesE.getEpsilon(widthIn,widthOut, length,tanH);
			double z0 = GuideAttributesE.getEllEntry(widthIn,widthOut, length, tanH);
			p.x=GuideAttributesE.getProfile(2*ksi*epsilon,z0,length,focH,z);
		}			
		p.z=z;
		return p;
	}
	
	@Override
	public double getHeight(double z) {
		if ( focV == 0 ) return super.getHeight(z);
		double epsilon = GuideAttributesE.getEpsilon(heightIn,heightOut, length,tanV);
		double z0 = GuideAttributesE.getEllEntry(heightIn,heightOut, length,tanV);
		double a = 2*GuideAttributesE.getProfile(epsilon,z0,length,focV,z);
		return a;
	}
	
	@Override
	public double getWidth(double z) {
		if ( focH == 0 ) return super.getWidth(z);		
		double epsilon = GuideAttributesE.getEpsilon(widthIn,widthOut, length,tanH);
		double z0 = GuideAttributesE.getEllEntry(widthIn,widthOut, length, tanH);
		double a = 2*GuideAttributesE.getProfile(epsilon,z0,length,focH,z);
		return a;
	}

}
