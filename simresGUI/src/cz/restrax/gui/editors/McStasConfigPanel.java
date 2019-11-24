package cz.restrax.gui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.restrax.gui.SimresGUI;
import cz.restrax.sim.mcstas.McStas;

/**
 * Panel with McStas configuration editors
 * @author   Jan Saroun
 * @version  <dl><<dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2018/12/31 17:15:04 $</dt></dl>
 */
public class McStasConfigPanel extends JPanel {
	private static final int TXTH=20;
	private static final int LBLW=100;
	private final SimresGUI program;
	private McStas mcstas;
	private  Map<String,String> envars;
	
	public McStasConfigPanel(SimresGUI program) {
		super();
		this.program=program;
		this.mcstas = program.getMcStas();
		
		//setPreferredSize(new Dimension(400,150));
		//initialize();
	}
/*	
	private JPanel createItemPanel(String label, String labelTip, JTextField text, JButton btn) {
		JPanel pnl = new JPanel();
		pnl.setLayout(new BoxLayout(pnl,BoxLayout.X_AXIS));
		pnl.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		JLabel lbl = new JLabel();	
		lbl.setText(label);
		lbl.setToolTipText(labelTip);
		lbl.setPreferredSize(new Dimension(LBLW,TXTH));
		pnl.add(lbl);
		//text.setPreferredSize(new Dimension(WIDTH-LBLW-BTN_SIZE.width,BTN_SIZE.height));
		pnl.add(text);
		if (btn!=null) pnl.add(btn);
		return pnl;
	}
	
	private JTextField getTxtField(String name) {
		if (txtOutPath == null) {
			txtOutPath = new JTextField();
			txtOutPath.setName(name);
		}
		return txtOutPath;
	}
	
	private void initialize() {
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));	
		setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

		this.add(createItemPanel("MCSTAS", 
				"Environment variable, McStas installation path.",
				mcstas.getEnvar("MCSTAS"), null));
		
		this.add(createItemPanel("MCSTAS_CC", 
				"Environment variable, McStas C compiler.",
				mcstas.getEnvar("MCSTAS_CC"), null));
		
		this.add(createItemPanel("MCSTAS_TOOLS", 
				"Environment variable, path to McStas tools.",
				mcstas.getEnvar("MCSTAS_TOOLS"), null));
	
		this.add(createItemPanel("Run command", 
				"McStas run command",
				getTxtDescription(),null));
		JPanel p = new JPanel(new BorderLayout());
		p.add(this.getLblMessage(),BorderLayout.WEST);
		this.add(p,BorderLayout.CENTER);
		this.setPreferredSize(PANEL_SIZE);
	}.
*/
}
