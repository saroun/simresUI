package cz.jstools.classes.editors.propertiesView;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;


/**
 * Editor for values used in PTable (VInt, VDouble, VString). 
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2012/01/12 15:08:53 $</dt></dl>
 */
public class PropertyValueEditor extends DefaultCellEditor {
	private static final long    serialVersionUID = 1651281305058280097L;
	private static final Border  BORDER           = new LineBorder(Color.BLACK, 1);
	
	private PValue        value        = null;
	private ValueChecker  valueChecker = null;


	public PropertyValueEditor(ValueChecker valueChecker) {
		super(new JTextField());
		super.setClickCountToStart(1);  // na prvni klik zacni editovat
		((JTextField)getComponent()).setName("Table.editor");
		((JTextField)getComponent()).setHorizontalAlignment(JTextField.LEFT);
		((JTextField)getComponent()).setBorder(BORDER);
		
		/*
		 * Kdyz se editovala tabulka a kliklo se na nejaky button, editor sice
		 * ztratil focus, ale nezavrel se. Zaviral se jedine, kdyz se kliklo nekam
		 * to tabulky. Kdyz tedy udivatel zmenil hodnotu a hned klikl na Apply, 
		 * editor se nezaviral a tudiz se ani neupdatovala jeho hodnota. Proto
		 * pridam focus listener, ktery editor zavre. V nepriznivem pripade
		 * (kdyz se klikne nekam jinam do tabulky a editor bude zaviran)
		 * se asi bude dvakrat volat stopCellEditing(), coz ale nevadi.
		 */
		((JTextField)getComponent()).addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent arg0) {
				stopCellEditing();
			}
		});
		
		this.valueChecker = valueChecker;
	}

	public boolean stopCellEditing() {
		// underlaying component is JTextField and method
		// getCellEditorValue() returns its getText() as
		// defined in DefaultCellEditor
		String s = (String)super.getCellEditorValue();
		
		try {
			if (valueChecker == null) {
				value.assignValue(s);				
			} else {
				PValue value2 = value.duplicate();
				value2.assignValue(s);
				if (valueChecker.checkValue(value2) == true) {
					value = value2;
				}
			}
		}
		catch (ParseException ex) {} // when error value is not assigned and remains the same

		return super.stopCellEditing();
	}
	
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
		this.value = ((PValue)value).duplicate();
		Component component = super.getTableCellEditorComponent(table, value.toString(), isSelected, row, column);		
		component.setForeground(table.getForeground());
		component.setBackground(table.getBackground());	
		return component;
	}
	
	public Object getCellEditorValue() {
		return (Object)value;
	}
}