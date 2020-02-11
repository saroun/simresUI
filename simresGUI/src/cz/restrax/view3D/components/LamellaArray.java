package cz.restrax.view3D.components;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransparencyAttributes;
import org.jogamp.vecmath.Color3f;

import cz.restrax.view3D.geometry.GuideAttributes;
import cz.restrax.view3D.geometry.LamellaArrayAtt;
import cz.restrax.view3D.geometry.LamellaArrayAttE;
import cz.restrax.view3D.geometry.LamellaArrayAttP;
import cz.restrax.view3D.geometry.LamellaGeometry;


/**
 * Represents a set of lamelae, e.g. inside Soller collimators or benders.
 * It is defined as a BranchGroup and handles correct detach procedure when 
 * lamellae need to rebuild. All lamellae share the same appearance.
 *
 */
public class LamellaArray extends BranchGroup{
	protected static final Color3f wallColour = new Color3f(0.5f, 0.5f, 0.5f);
	protected static final Color3f diffusiveColour = new Color3f(0.5f, 0.5f, 0.5f);
	protected static final Color3f emissiveColour = new Color3f(0.2f, 0.2f, 0.2f);
	protected static final Color3f specularColour = new Color3f(0.7f, 0.7f, 1.0f);
	private LamellaArrayAtt properties;
	private FrameShape[] items=null;
	private Frame3D frame=null;
	private int type = GuideAttributes.GUIDE_TYPE_SOLLER;
	private int nseg;
	private Appearance appearance;
	private Material material;

	   	   
//////////////////////////////////////////	   
	   public LamellaArray(Frame3D frame, int orientation, GuideAttributes att) {
		   super();
		   try {
		   switch (att.type) {
   			case GuideAttributes.GUIDE_TYPE_PARA:   				
				properties=new LamellaArrayAttP(orientation,att);				
   				break;
   			case GuideAttributes.GUIDE_TYPE_PARA2:
   				properties=new LamellaArrayAttP(orientation,att);
   				break;
   			case GuideAttributes.GUIDE_TYPE_ELL:
   				properties=new LamellaArrayAttE(orientation,att);
   				break;
   			default:
   				properties=new LamellaArrayAtt(orientation,att);      		
		   }  
		   } catch (Exception e) {
				e.printStackTrace();
			}
		   type = att.type;
		   this.frame=frame;
		   setCapability(BranchGroup.ALLOW_DETACH);
		   importFromGuide(att);
		   createLamellae();
	   }
	   
	   
	   public boolean needsRebuild(GuideAttributes att) {
		   boolean b=false;
		   if (properties.orientation == LamellaArrayAtt.HORIZONTAL_ARRAY) {
			   b = b || (att.nlH != properties.nl);
		   } else {
			   b = b || (att.nlV != properties.nl);
		   }		
		   b = b || (type != att.type);
		   return b;
	   }

	   
	   protected void importFromGuide(GuideAttributes att) {
		   nseg = att.getOptimumSegments();
		   if (properties!=null) properties.importFromGuide(att);
	   }
	   
	   protected void createLamellae() {
		   items=new FrameShape[properties.nl+1];
		   if (this.isLive()) {
				System.out.printf("Cannot build live LamellaArray: %s\n",this.getName());			   
		   } else {
			   for (int i=0;i<=properties.nl;i++) {
				   createLamella(nseg,i);
				   this.addChild(getLamella(i));
			   } 
			   setAppearance();
		   }
	   }	     	   	

	   protected Appearance createAppearance() {
		   Appearance app = new Appearance();
		   TransparencyAttributes ta=null;	
		   ta = new TransparencyAttributes(TransparencyAttributes.NONE,0.0f);
		   ta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
		   app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		   app.setCapability(Appearance.ALLOW_MATERIAL_READ);
		   //app.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
		   app.setCapability(Appearance.ALLOW_TRANSPARENCY_ATTRIBUTES_WRITE);
		   app.setTransparencyAttributes(ta);
		   app.setMaterial(getMaterial());	   			
		   app.setPolygonAttributes(FrameShape.getPolygonAttributes(frame.getFaceStyle()));
		   return app;
	   }

	   protected Appearance getAppearance() {
		   if (appearance==null) {
			   appearance = createAppearance();			   
		   }
		   // adjust transparency
		   TransparencyAttributes ta=appearance.getTransparencyAttributes();
		   if (ta!=null) {
			   float tr=ta.getTransparency();
			   boolean needT=(tr!=properties.transparency);
			   if (needT) {
				   int tm=TransparencyAttributes.NONE;				   
				   if (properties.transparency > 0.0f ) tm=TransparencyAttributes.FASTEST;
				   ta = new TransparencyAttributes(tm,properties.transparency);
				   ta.setCapability(TransparencyAttributes.ALLOW_VALUE_READ);
				   appearance.setTransparencyAttributes(ta);
			   }	
		   }
		   // adjust view style
		   appearance.setPolygonAttributes(FrameShape.getPolygonAttributes(frame.getFaceStyle()));
		   getMaterial();
		   return appearance;
	   }

	   protected Material createMaterial() {
		   Material m = new Material(wallColour, 
				   FrameShape.BLACK, 
				   diffusiveColour,
				   FrameShape.BLACK,
				   FrameShape.SHINE_DARK);			
		   m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		   m.setCapability(Material.ALLOW_COMPONENT_READ);
		   m.setColorTarget(Material.AMBIENT_AND_DIFFUSE);
		   return m;
	   }
	   
		protected Material getMaterial() {
			if (material==null) {
				material = createMaterial();		
			}	
			// adjust shininess according to the reflectivity
			if (properties.shine != material.getShininess()) {
				material.setShininess(properties.shine);
				if (properties.shine==FrameShape.SHINE_REFLECTING) {
					material.setSpecularColor(specularColour);
				} else {
					material.setSpecularColor(FrameShape.BLACK);
				} 
			}
			return material;
		}

	   public void setAppearance() {	
		   if (items != null) {				
			   FrameShape L;
			   Appearance ap = getAppearance();
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
					setItemGeometry(nseg,i);
				}
		   }	
	   }	   	  	   
	   
	   public void clearItems() {		
		   if (items != null) {				
				for (int i=0;i<items.length;i++) {
					items[i]=null;
				}
		   }
		   items=null;
	   }
	   
	   protected void setItemGeometry(int nseg,int index) {
		   FrameShape L = getLamella(index);
			if (L != null) {
				Transform3D t = getLamellaTransform(index);
				LamellaGeometry lg=new LamellaGeometry(nseg,index,properties,t);
				L.setGeometry(lg);	
			} 
	   }
	   
	   protected void createLamella(int nseg,int index) {
		   if (index >=0 && index<=properties.nl ) {				
			   Transform3D t=getLamellaTransform(index);
			   FrameShape L = new FrameShape(frame);
			   LamellaGeometry lg=new LamellaGeometry(nseg,index,properties,t);			   
			   L.setGeometry(lg);	
			   items[index]=L;
			}
	   }
		
	   protected FrameShape getLamella(int index) {		
		   FrameShape L = null;
			if (index >=0 && index<=properties.nl ) {
				if (index < items.length) L = items[index]; 
			}
			return L;
	   }
	   
	   protected Transform3D getLamellaTransform(int index) {
		   Transform3D t = null;
			if (properties.orientation == LamellaArrayAtt.VERTICAL_ARRAY) {
				t=new Transform3D();
				t.rotZ(-Math.PI/2);
			}
			return t;
	   }


	public LamellaArrayAtt getProperties() {
		return properties;
	}	   	  

	   
}
