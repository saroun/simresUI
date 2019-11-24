package cz.restrax.graphs.primitives;

import java.awt.BasicStroke;
import java.awt.Color;

public class Utils {
  public static final Color[] COLORS = {
	  new Color(255,0,0),
	  new Color(0,255,0),
	  new Color(0,0,255),
	  };
  
  public static final float dash1[] = {10.0f};
  public static final BasicStroke dashed = new BasicStroke(1.0f, 
                                        BasicStroke.CAP_BUTT, 
                                        BasicStroke.JOIN_MITER, 
                                        10.0f, dash1, 0.0f);

  
}
