package cz.restrax.view3D.components;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;

import cz.restrax.view3D.geometry.BladeAtt;
import cz.restrax.view3D.geometry.BladeSequenceAtt;


/**
 * Represents a sequence of blades, like one wall of a segmented guide. 
 *
 */

public class BladeSequence extends BranchGroup {
    
    
	   private BladeSequenceAtt properties;
	   private Blade[] items=null;
	   private Frame3D frame=null;
	   	   
//////////////////////////////////////////	   
	   public BladeSequence(Frame3D frame, BladeSequenceAtt att) {
		   super();
		   properties=att;
		   this.frame=frame;
		   setCapability(BranchGroup.ALLOW_DETACH);
		   createLamellae();
	   }
	   	
	public boolean needsRebuild(int nWalls) {		
		   boolean b=false;
		   b=b || (properties.nBlades != nWalls );		   
		   return b;		   
		//return true;
	   }

	   
	private void createLamellae() {
		   items=new Blade[properties.nBlades];
		   if (this.isLive()) {
				System.out.printf("Cannot build live BladeSequence: %s\n",this.getName());			   
		   } else {
			   for (int i=0;i<properties.nBlades;i++) {
				   BladeAtt batt=properties.getBladeAtt(i);
				   createLamella(batt,i);
				   this.addChild(getLamella(i));
			   } 
			   setAppearance(null);
		   }
	   }	   
	   
	
	
	void setAppearance(Appearance ap) {	
		   if (items != null && items.length>0) {				
			   Blade L;			    
			   for (int i=0;i<items.length;i++) {
					L = getLamella(i);
					if (L != null) {
						L.setAppearance(ap);
					}
			   }
		   }	
	   }
	   
	   public void setGeometry() {		
		   if (items != null) {
			   for (int i=0;i<items.length;i++) {
					setItemGeometry(i);
			   }
		   }	
	   }	   	  	   
	   
	   void clearItems() {		
		   if (items != null) {				
				for (int i=0;i<items.length;i++) {
					items[i]=null;
				}
		   }
		   items=null;
	   }
	   
	   private void setItemGeometry(int index) {
		   Blade L = getLamella(index);
			if (L != null) {
				BladeAtt att=properties.getBladeAtt(index);			
				L.setGeometry(att);	
			} 
	   }
	   
	   private void createLamella(BladeAtt batt,int index) {
		   if (index >=0 && index<properties.nBlades ) {				
			   Blade L = new Blade (frame);
			   L.setGeometry(batt);	
			   items[index]=L;
			}			
	   }
		
	   private Blade getLamella(int index) {		
		   Blade L = null;
			if (index >=0 && index<=properties.nBlades ) {
				if (index < items.length) L = items[index]; 
			}
			return L;
	   }

		/**
		 * Add the number of segments + 1 for each blade, so that
		 * n=suma_i[(1+nseg(i)] 
		 * where nseg(i) is the number of segments for i-th blade
		 */
	   public int getNodesNumber() {
			int n=0;
			Blade[] items=getItems();
			n=0;
			for (int j=0;j<items.length;j++) {
				n += items[j].att.getNseg()+1;
			}
			return n;
		}
		
		/**
		 * Return positions of the nodes for all blades, including 
		 * entry and exit z-positions and borders between segments for all nodes.
		 * The length of the resulting array must be the same as the number 
		 * returned by getNodesNumber(); 
		 */
	   public double[] getNodesPositions() {
			int nn=getNodesNumber();
			double[] z = new double[nn];			
			Blade[] items=getItems();
			int n=-1;
			z[0]=0.0;
			for (int j=0;j<items.length;j++) {
				int nk=items[j].att.getNseg();
				double len=items[j].att.length;
				double dlen=len/nk;
				n++;
				z[n]=properties.zL[j];
				for (int k=1;k<=nk;k++) {
					n++;
					z[n]=z[n-1]+dlen;
				}
			}
			return z;
		}
		
	public BladeSequenceAtt getProperties() {
		return properties;
	}

	public Blade[] getItems() {
		return items;
	}
	   
}
