package cz.restrax.view3D;

        /* adapted from:
         * $RCSfile: ScreenScaleBehavior.java,v $
         *
         * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
         *
         * Redistribution and use in source and binary forms, with or without
         * modification, are permitted provided that the following conditions
         * are met:
         *
         * - Redistribution of source code must retain the above copyright
         *   notice, this list of conditions and the following disclaimer.
         *
         * - Redistribution in binary form must reproduce the above copyright
         *   notice, this list of conditions and the following disclaimer in
         *   the documentation and/or other materials provided with the
         *   distribution.
         *
         * Neither the name of Sun Microsystems, Inc. or the names of
         * contributors may be used to endorse or promote products derived
         * from this software without specific prior written permission.
         *
         * This software is provided "AS IS," without a warranty of any
         * kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
         * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
         * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
         * EXCLUDED. SUN MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL
         * NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF
         * USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
         * DERIVATIVES. IN NO EVENT WILL SUN OR ITS LICENSORS BE LIABLE FOR
         * ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL,
         * CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND
         * REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR
         * INABILITY TO USE THIS SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
         * POSSIBILITY OF SUCH DAMAGES.
         *
         */
import java.awt.event.MouseEvent;

import javax.media.j3d.Canvas3D;

import com.sun.j3d.internal.J3dUtilsI18N;
import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;

// public class ScreenScaleBehavior extends ViewPlatformAWTBehavior {
public class ScreenScaleBehavior extends OrbitBehavior {	
//	public static final int REVERSE_ZOOM = 0x040;
//	public static final int STOP_ZOOM = 0x100;
//	 public static final int DISABLE_ZOOM = 0x800;
	 private boolean zoomEnabled = true;
	 private boolean reverseZoom = false;

	 private double minScale = 0.001;
	 private double maxScale = 1000.0;
	 
	 private static final int ROTATE = 0;
	 private static final int TRANSLATE = 1;
	 private static final int ZOOM = 2;
	 private int leftButton = ROTATE;
	 private int rightButton = TRANSLATE;
	 private int middleButton = ZOOM;

	 private double zoomFactor = 1.0;
	 private static final double NOMINAL_ZOOM_FACTOR = .01;
	 private double zoomMul = NOMINAL_ZOOM_FACTOR * zoomFactor;
	 private float wheelZoomFactor = 50.0f;
	 private int mouseY = 0;

	
	 public ScreenScaleBehavior() {
		super ();
	 }		
	 public ScreenScaleBehavior(Canvas3D c) {
	    super(c);
	 }
		
	public ScreenScaleBehavior(Canvas3D c, int flags) {
		super(c,flags);
/*		
		super (c, MOUSE_LISTENER | MOUSE_MOTION_LISTENER
	                       | MOUSE_WHEEL_LISTENER | flags);		
		if ((flags & DISABLE_ZOOM) != 0) zoomEnabled = false;
		if ((flags & REVERSE_ZOOM) != 0) reverseZoom = true;	
		*/	                
	 }
	 
	
	
	 /*extraction of the zoom algorithms so that there is no code duplication or source 'uglyfication'.
	             */
		
	private void doZoomOperations1(int ychange, Canvas3D c) {		
		if (c != null) {
			double oldScale = c.getView().getScreenScale();		
			double delta=zoomMul*ychange;
			double fact;		
			if (delta>0) {
				fact=(1.0+delta);
			} else {
				fact=1.0/(1.0-delta);
			}
			if (reverseZoom) fact=1.0/fact;
			double newScale = Math.max(minScale,Math.min(maxScale,oldScale*fact));
			c.getView().setScreenScale(newScale);
		}
	}

/*
	protected synchronized void processAWTEvents(final AWTEvent[] events) {
		motion = false;
		for (int i = 0; i < events.length; i++)
			if (events[i] instanceof  MouseEvent) processMouseEvent((MouseEvent) events[i]);
    }
    */
	
	protected void processMouseEvent(final MouseEvent evt) {	
		
		if (! (zoom1(evt))) {
			super.processMouseEvent(evt);
			return;
		}
		
		Canvas3D c = null;
		if (evt.getSource() instanceof Canvas3D) {
			c = (Canvas3D) evt.getSource();
		}
		if (evt.getID() == MouseEvent.MOUSE_PRESSED) {
			mouseY = evt.getY();
			motion = true;
		} else if (evt.getID() == MouseEvent.MOUSE_DRAGGED) {
			int ychange = evt.getY() - mouseY;
		// zoom
			if (zoom1(evt)) {
		    	doZoomOperations1(ychange,c);
		    }
			mouseY = evt.getY();
		 	motion = true;
		} else if (evt.getID() == MouseEvent.MOUSE_RELEASED) {
		} else if (evt.getID() == MouseEvent.MOUSE_WHEEL) {
			if (zoom1(evt)) {
		                        // if zooming is done through mouse wheel, 
		                        // the amount of increments the wheel changed, 
		                        // multiplied with wheelZoomFactor is used, 
		                        // so that zooming speed looks natural compared to mouse movement zoom.
		    	if (evt instanceof  java.awt.event.MouseWheelEvent) {
		                            // I/O differenciation is made between 
		                            // java.awt.event.MouseWheelEvent.WHEEL_UNIT_SCROLL or 
		                            // java.awt.event.MouseWheelEvent.WHEEL_BLOCK_SCROLL so 
		                            // that behavior remains stable and not dependent on OS settings.
		                            // If getWheelRotation() was used for calculating the zoom, 
		                            // the zooming speed could act differently on different platforms, 
		                            // if, for example, the user sets his mouse wheel to jump 10 lines 
		                            // or a block.
		    		int zoom = ((int) (((java.awt.event.MouseWheelEvent) evt).getWheelRotation()*wheelZoomFactor));
		    		doZoomOperations1(zoom,c);
		    		motion = true;
		    	}
		  	}
		}
	}
   
	private boolean zoom1(MouseEvent evt) {
		if (zoomEnabled) {
			if (evt instanceof  java.awt.event.MouseWheelEvent) {
		    	return true;
		    	}
			if ((leftButton == ZOOM)
		      	&& (!evt.isAltDown() && !evt.isMetaDown())) {
		        	return true;
		    }
		    if ((middleButton == ZOOM)
		    	&& (evt.isAltDown() && !evt.isMetaDown())) {
		        	return true;
			}
		    if ((rightButton == ZOOM)
		    	&& (!evt.isAltDown() && evt.isMetaDown())) {
		       		return true;
			}
		}
		return false;
	}

	public synchronized void setZoomFactor(double zfactor) {
		zoomFactor = zfactor;
		zoomMul = NOMINAL_ZOOM_FACTOR * zfactor;
	}
	
	public void ZoomFactor(Object[] zFactor) {
		if (!(zFactor.length == 1 && zFactor[0] instanceof  Double))
			throw new IllegalArgumentException("ZoomFactor must be a Double");	
	 	setZoomFactor(((Double) zFactor[0]).doubleValue());
	}
	public synchronized void setMinScale(double r) {
		if (r <= 0.0) {
			throw new IllegalArgumentException(J3dUtilsI18N.getString("ScreenScaleBehavior1"));
		}
			 minScale = r;
	}

	public synchronized void setMaxScale(double r) {
		if (r <= 0.0) {
			throw new IllegalArgumentException(J3dUtilsI18N.getString("ScreenScaleBehavior1"));
		}
			maxScale = r;
	}
	public void MinScale(Object[] r) {
		if (!(r.length == 1 && r[0] instanceof  Double))
			throw new IllegalArgumentException("MinScale must be a Double");
		setMinScale(((Double) r[0]).doubleValue());
	}
	public void MaxScale(Object[] r) {
		if (!(r.length == 1 && r[0] instanceof  Double))
			throw new IllegalArgumentException("MaxScale must be a Double");
		setMaxScale(((Double) r[0]).doubleValue());
	}

}
