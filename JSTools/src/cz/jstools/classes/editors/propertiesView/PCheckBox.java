package cz.jstools.classes.editors.propertiesView;

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;


/**
 * JCheckBox used as property item check box, i.e. when
 * item is section it shows '[+]' or '[-]' images if the
 * section is collapsed or uncollpsed respectively. When
 * item is ordinary protperty it shows empty image in order
 * to correctly align all properties.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:52 $</dt></dl>
 */
class PCheckBox extends JCheckBox {
	private static final long serialVersionUID = -7779597058472247119L;

	public PCheckBox(boolean isSection, boolean isValueChanged) {
		super();
		
		if (isSection) {
			setFont(getFont().deriveFont(Font.BOLD));
			setIcon(new ImageIcon(this.getClass().getResource("images/uncollapsed.png")));
			setPressedIcon(new ImageIcon(this.getClass().getResource("images/empty.png")));
			setSelectedIcon(new ImageIcon(this.getClass().getResource("images/collapsed.png")));
		} else {
			setFont(getFont().deriveFont(Font.PLAIN));
			setIcon(new ImageIcon(this.getClass().getResource("images/null.png")));
		}
		
		if (isValueChanged) {
			setForeground(Color.RED);
		}
	}
}