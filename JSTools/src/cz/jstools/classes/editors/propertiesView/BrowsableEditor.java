package cz.jstools.classes.editors.propertiesView;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellEditor;



/**
 * Editor for values that can be edit directly or can be chosen by clicking on
 * '...' button used in PTable.
 * Pokud je isDirectInput, hodnoty value muzou byt jen PValue. Pokud neni direct
 * input, muze byt value jakykoli objekt, ktery rozumne implementuje toString() metodu.
 * Interni hodnota value je typu Object. Pokud je direct input, pri action performed v textfieldu
 * se vytvori lokalni kopie do jktere na naparsuji data (aby fungoval priznak valueChanged
 * nesmi se importovat do puvodniho objektu) a pokud parsing provehne bez problemu, priradi se zpatky
 * value tato nova hodnota. Pokud by value nebylo typu PValue, byla by class cast exception.
 * Pokud neni directaccess, nikdy se action performed nevola, takze chyba pretypovani nehrozi a oibjekt muze
 * zustat tak jak je. Browsable by ale mel vracet stejny typ objektu, coz se kontroluje.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2012/01/23 22:18:36 $</dt></dl>
 */
class BrowsableEditor extends AbstractCellEditor implements TableCellEditor {
	private static final long    serialVersionUID = 3002260132472234675L;
	private static final Border  BORDER = new LineBorder(Color.BLACK, 1);
	
	private JPanel                   panel             = null;
	private JTextField               textField         = null;
	private JButton                  browseButton      = null;
	private Browsable                objectToBrowse         = null;
	private int                      clickCountToStart = 1;
	private Class<? extends Object>  valueClass        = null;
	private Object                   value             = null;
	private boolean                  directInput       = true;


	public BrowsableEditor(Browsable objectToBrowse, boolean directInput) {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
	
		textField = new JTextField();
		textField.setEditable(directInput);
		
		this.directInput = directInput;
		this.objectToBrowse = objectToBrowse;
		// browseButton = new JButton("\u2026");
		browseButton = new JButton();
		browseButton.setMargin(new Insets(0,0,0,0));
		browseButton.setAlignmentX((float) 0.5);
		browseButton.setAlignmentY((float) 0.5);
		browseButton.setIcon(new ImageIcon(this.getClass().getResource("images/right.png")));
		// browseButton.setIcon(Resources.getIcon(Resources.ICON16x16, "right.png"));
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx=0;
		c.gridy=0;
		c.weightx=1.0;
		c.weighty=1.0;
		c.insets=new Insets(0,0,0,0);
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		panel.add(textField, c);
		
		c.gridx=1;
		c.weightx=0.0;
		c.weighty=0.0;
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.EAST;
		panel.add(browseButton, c);
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Object retVal = BrowsableEditor.this.objectToBrowse.browse(value);
				if (retVal != null) {
					if (valueClass.isAssignableFrom(retVal.getClass()) == false) {
						throw new ClassCastException("Browsable returns other object type (" + retVal.getClass() +
						                             ") than value is ("+ valueClass +").");
					}
					setValue(retVal);
				}
				//at uz kliknu ok nebo cancel nebo kdovi co, skonci
				stopCellEditing();
			}
		});
		
		textField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				PValue value2 = ((PValue)value).duplicate();
				try {
					value2.assignValue(textField.getText());
					value = value2;
				} catch (ParseException ex) {} // when error value is not assigned and remains the same

				stopCellEditing();
			}
		});

		// tahle opicarna souvisi s tim, abych sebral zhatil klik
		// dodany do editoru. Kdyz jsem totiz kliknul na konci radky
		// kde se nachazi button, zavolal se browsable editor, zobrazil se
		// button a predal se klik, takze uzivatel rovnou skocil do browsable objektu
		browseButton.setVisible(false);
		textField.setVisible(false);
		panel.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e){
				browseButton.setVisible(true);
				textField.setVisible(true);
				panel.removeMouseListener(this);
				if (BrowsableEditor.this.directInput) {
					textField.requestFocus();
				}
			}
		});
	}
	
	public void setValue(Object value) {
		this.value = value;
		textField.setText(value.toString());
	}

	public Object getCellEditorValue() {
		return value;
	}
	
	public boolean shouldSelectCell(EventObject anEvent) { 
		if (anEvent instanceof MouseEvent) { 
			MouseEvent e = (MouseEvent)anEvent;
			return e.getID() != MouseEvent.MOUSE_DRAGGED;
		}
		return true;
	}
	
	public boolean stopCellEditing() {
		fireEditingStopped(); 
		return true;
	}
	
	public void cancelCellEditing() { 
		fireEditingCanceled(); 
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		valueClass = value.getClass();
		setValue(value);
		
		if (directInput) {
			textField.setBorder(BORDER);
			textField.setForeground(table.getForeground());
			textField.setBackground(table.getBackground());
		} else {
			//textField.setBorder(new LineBorder(table.getBackground(), 1));
			Color bgColor = new Color(220,220,220);
			Color fgColor = new Color(128,128,128);
			textField.setBorder(new LineBorder(bgColor, 1));
			textField.setForeground(fgColor);
			textField.setBackground(bgColor);
		}


		return panel;
	}
	
	
	public boolean isCellEditable(EventObject anEvent) {
		if (anEvent instanceof MouseEvent) { 
			return ((MouseEvent)anEvent).getClickCount() >= clickCountToStart;
		}
		return true;
	}
}