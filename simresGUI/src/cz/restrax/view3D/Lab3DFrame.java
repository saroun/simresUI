package cz.restrax.view3D;
/** 
 * @author   Jan Saroun
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.7 $</dt>
 *               <dt>$Date: 2012/01/23 22:18:15 $</dt></dl>
 */

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.media.j3d.Transform3D;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.vecmath.Matrix3f;

import cz.jstools.classes.ClassData;
import cz.jstools.classes.ieditors.InternalDialog;
import cz.restrax.gui.SimresGUI;
import cz.restrax.view3D.components.Frame3D;
import cz.restrax.view3D.components.FrameShape;





public class Lab3DFrame extends InternalDialog {
	private static final long serialVersionUID = 1L;
	private JPanel drawingPanel;
	private JPanel guiPanel;
	private JCheckBox styleCheck;
	private JCheckBox axesCheck;
	private JCheckBox wireCheck;
	private Lab3D lab3D=null;
	private final SimresGUI program; 
	
   // final float deg=(float)(Math.PI/180);

	public Lab3DFrame(SimresGUI program)  {
		super(program.getDesktop());
		this.program=program;
		setResizable(true);
		setTitle("View 3D");
		setClosable(true);
		setIconifiable(true);
		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		initComponents();
		lab3D = new Lab3D(program.getInstrument3D());
        drawingPanel.add(lab3D.getCanvas(), java.awt.BorderLayout.CENTER);
        lab3D.getCanvas().addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount()>1) editSelected();			
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}
			public void mousePressed(MouseEvent e) {
			}
			public void mouseReleased(MouseEvent e) {
			}
        });
	}

	private void editSelected() {
		Frame3D obj=lab3D.getFocusObject();
		/*	Transform3D t=new Transform3D();
		obj.getGonio().getTransform(t);
		Matrix3f m = new Matrix3f();
		t.get(m);
		System.out.printf("selected %s\n%s\n", obj.getId(),m.toString());*/		
		program.getConfigWindow().getInstrumentEditor().showClassDataDialog(obj.getCls());
	}
	
	public void focusObject(ClassData obj) {
		if ((lab3D != null) && (obj != null)) {
			lab3D.focusAtClass(obj);
		}
	}
	
    private void initComponents() {
    	java.awt.GridBagConstraints gridBagConstraints;
    	gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
        gridBagConstraints.anchor= java.awt.GridBagConstraints.WEST;
 // Control tools	
    	JButton focusButton = new JButton();
    	focusButton.setText("Focus");
    	focusButton.setToolTipText("Focus view at the selected component, along incident beam.");
    	focusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testButtonActionPerformed(evt);
            }
        });
    	
     	styleCheck=new JCheckBox("2D layout view");
    	styleCheck.setToolTipText("Parallel projection from top");
    	styleCheck.setSelected(false);
    	styleCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	styleCheckActionPerformed(evt);
            }
        });

    	axesCheck=new JCheckBox("show axes");
    	axesCheck.setToolTipText("Show coordinate axes");
    	axesCheck.setSelected(true);
    	axesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	axesCheckActionPerformed(evt);
            }
        });

    	wireCheck=new JCheckBox("wireframe");
    	wireCheck.setToolTipText("Show objects as wireframe");
    	wireCheck.setSelected(false);
    	wireCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	wireCheckActionPerformed(evt);
            }
        });


    // Info panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new java.awt.BorderLayout());
        infoPanel.setPreferredSize(new java.awt.Dimension(600, 20));
        JTextArea infoText = new JTextArea();
        infoText.setEditable(false);
        infoText.setText("Mouse navigation: LEFT-rotate, RIGHT-move, WHEEL-zoom, CLICK-select, DBLCLICK-edit");
        infoPanel.add(infoText, java.awt.BorderLayout.CENTER);
    
    // Panel with controls     
    	guiPanel = new JPanel();
    	guiPanel.setLayout(new java.awt.GridBagLayout());
    	guiPanel.setPreferredSize(new java.awt.Dimension(600, 40));
        guiPanel.add(focusButton, gridBagConstraints);
        guiPanel.add(styleCheck, gridBagConstraints);
        guiPanel.add(axesCheck, gridBagConstraints);
        guiPanel.add(wireCheck, gridBagConstraints);       
    
    // North panel with controls and info    
        JPanel northPanel = new JPanel();
        northPanel.setPreferredSize(new java.awt.Dimension(600, 60));
        northPanel.setMinimumSize(new java.awt.Dimension(200, 50));
        northPanel.setLayout(new java.awt.BorderLayout());
        northPanel.add(guiPanel, java.awt.BorderLayout.NORTH);
        northPanel.add(infoPanel, java.awt.BorderLayout.CENTER); 
        getContentPane().add(northPanel, java.awt.BorderLayout.NORTH);    
        
    // Central panel with 3D graphics           
        drawingPanel = new JPanel();
        drawingPanel.setLayout(new java.awt.BorderLayout());
        drawingPanel.setPreferredSize(new java.awt.Dimension(600, 600));
        drawingPanel.setMinimumSize(new java.awt.Dimension(200, 200));
        drawingPanel.setBackground(new Color(0.2f,0.2f,0.2f));
        getContentPane().add(drawingPanel, java.awt.BorderLayout.CENTER);   
        
       
      //  this.setAlwaysOnTop(false);
        pack();
    }

    private void testButtonActionPerformed(java.awt.event.ActionEvent evt) {
    	lab3D.resetViewer();
    //    System.out.printf("preferred size: %s \n", lab3D.getCanvas().getPreferredSize());
    //    System.out.printf("minimum size: %s \n", lab3D.getCanvas().getMinimumSize());
    }
    
    private void styleCheckActionPerformed(java.awt.event.ActionEvent evt) {
    	if (evt.getSource() instanceof JCheckBox) {
    		if (((JCheckBox) evt.getSource()).isSelected() ) {
    			lab3D.setStyle(Lab3D.STYLE_LAYOUT);    			
    		} else lab3D.setStyle(Lab3D.STYLE_3D); 
    	}
    }
    
    private void axesCheckActionPerformed(java.awt.event.ActionEvent evt) {
    	if (evt.getSource() instanceof JCheckBox) {
    		lab3D.showAxes(((JCheckBox) evt.getSource()).isSelected());    			
    	}
    }
    
    private void wireCheckActionPerformed(java.awt.event.ActionEvent evt) {
    	if (evt.getSource() instanceof JCheckBox) {
    		if (((JCheckBox) evt.getSource()).isSelected() ) {
    			lab3D.setFaceStyle(FrameShape.VIEW_WIRE);  			
    		} else lab3D.setFaceStyle(FrameShape.VIEW_FACE);     				
    	}
    }

	public Lab3D getLab3D() {
		return lab3D;
	}    

    
}
