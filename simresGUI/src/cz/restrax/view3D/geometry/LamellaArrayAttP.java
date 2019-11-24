package cz.restrax.view3D.geometry;

import javax.vecmath.Vector3d;

/**
 * Defines properties of curved tapered array of lamellae
 */
public class LamellaArrayAttP extends LamellaArrayAtt {
    public double focH;
    public double focV;
	public LamellaArrayAttP(int orientation, GuideAttributes att) throws Exception {
		super(orientation, att);
		if (!(att instanceof GuideAttributesP)) {
			throw new Exception("LamellaArrayAttP requires GuideAttributesP in constructor argumentt");
		}
	}

	@Override
	public void importFromGuide(GuideAttributes att) {
		   super.importFromGuide(att);
		   if (orientation == HORIZONTAL_ARRAY) {
			   focH=((GuideAttributesP) att).focH; 
			   focV=((GuideAttributesP) att).focV; 
		   } else if (orientation == VERTICAL_ARRAY) {
			   focH=((GuideAttributesP) att).focV; 
			   focV=((GuideAttributesP) att).focH; 
		   }
	}
		
	@Override
	public Vector3d getLamellaAngle(int index,double z) {
		double ksi = index*1.0/nl-0.5;		
		Vector3d p = new Vector3d();
		if ( focH == 0 ) {
			p.y=ksi*dwidth;
		} else {
			p.y=GuideAttributesP.getProfileAngle(ksi*widthIn,length,focH,z);
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
			p.x=GuideAttributesP.getProfile(ksi*widthIn,length,focH,z);
		}			
		p.z=z;
		return p;
	}
	
	@Override
	public double getHeight(double z) {
		if ( focV == 0 ) return super.getHeight(z);
		double a = 2*GuideAttributesP.getProfile(heightIn/2,length,focV,z);
		return a;
	}
	
	@Override
	public double getWidth(double z) {
		if ( focH == 0 ) return super.getWidth(z);
		double a = 2*GuideAttributesP.getProfile(widthIn/2,length,focH,z);
		return a;
	}
			   
}
