package cz.jstools.classes.editors;

import java.awt.Color;
import java.awt.Component;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;

import cz.jstools.classes.definitions.Utils;



/**
 * Vlastni verze abulky (Custom Table), ktera spravne rendruje cisla.
 * Zarucene pouzitelne hodnoty pro tuto tanulku
 * jsou Double, Integer, String, Boolean, JComboBox a Icon.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:34 $</dt></dl>
 */
public class CTable extends JTable {
	private static final long serialVersionUID = 6279884648053024881L;


	public CTable() {
		super();
		
		super.getTableHeader().setReorderingAllowed(false);  // do not allow reorganization of columns
		
		super.setDefaultRenderer(Integer.class, new IntegerRenderer());
		super.setDefaultRenderer(Double.class, new DoubleRenderer());

		super.setDefaultEditor(Integer.class, new IntegerEditor());
		super.setDefaultEditor(Double.class, new DoubleEditor());
	}


	// ****************************************
	//      Default Renderers
	// ****************************************
	protected static class StringRenderer extends DefaultTableCellRenderer.UIResource {
		private static final long serialVersionUID = -3188945716578229036L;

		public StringRenderer() {
			super();
			setHorizontalAlignment(JLabel.LEFT);
		}
		
		public void setValue(Object value) {
			setText((value == null) ? "" : (String)value);
		}
	}

	protected static class NumberRenderer extends StringRenderer {
		private static final long serialVersionUID = -879578624203252473L;


		public NumberRenderer() {
			super();
			setHorizontalAlignment(JLabel.RIGHT);
		}
	}

	protected static class DoubleRenderer extends  NumberRenderer {
		private static final long  serialVersionUID = -7408503921071970197L;

		public void setValue(Object value) {
			setText((value == null) ? "" : ("<html>" + Utils.d2html(((Double)value).doubleValue()) + "</html>"));
		}
	}
	
	protected static class IntegerRenderer extends  NumberRenderer {
		private static final long  serialVersionUID = -1603135393804503748L;

		public void setValue(Object value) {
			setText((value == null) ? "" : ("<html>" + Utils.i2html(((Integer)value).intValue()) + "</html>"));
		}
	}


	// ****************************************
	//      Default Editors
	// ****************************************
	protected static class StringEditor extends DefaultCellEditor {
		private static final long serialVersionUID = 2706059657509080602L;


		public StringEditor() {
			super(new JTextField());
			super.setClickCountToStart(1);  // na prvni klik zacni editovat
			getComponent().setName("Table.editor");
			((JTextField)getComponent()).setHorizontalAlignment(JTextField.LEFT);
		}
	}
	
	protected static class NumberEditor extends StringEditor {
		private static final long serialVersionUID = 4254137382672889892L;

		protected static LineBorder  errorBorder  = new LineBorder(Color.red, 1);
		protected static LineBorder  editorBorder = new LineBorder(Color.black, 1);


		public NumberEditor() {
			super();
			((JTextField)getComponent()).setHorizontalAlignment(JTextField.RIGHT);
		}
	}
	
	protected static class DoubleEditor extends NumberEditor {
		private static final long serialVersionUID = 2092028901248748929L;
		
		private Double  value = null;


		public boolean stopCellEditing() {
			/* 
			 * underlaying component is JTextField and method
			 * getCellEditorValue() returns its getText() as
			 * defined in DefaultCellEditor (since in Number
			 *  editor this function is not overridden)
			 */
			String s = (String)super.getCellEditorValue();
			
			try {
				value = new Double(Utils.s2de(s));
			}
			catch (ParseException ex) {
				((JTextField)getComponent()).setBorder(errorBorder);
				return false;
			}
			return super.stopCellEditing();
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.value = null;
			((JTextField)getComponent()).setBorder(editorBorder);
			return super.getTableCellEditorComponent(table, Utils.d2s(((Double)value).doubleValue()), isSelected, row, column);
		}
		
		public Object getCellEditorValue() {
			return value;
		}
	}
	
	protected static class IntegerEditor extends NumberEditor {
		private static final long serialVersionUID = 5775214147526120852L;

		private Integer  value = null;


		public boolean stopCellEditing() {
			String s = (String)super.getCellEditorValue();
			
			try {
				value = new Integer(Utils.s2ie(s));
			}
			catch (ParseException ex) {
				((JTextField)getComponent()).setBorder(errorBorder);
				return false;
			}
			return super.stopCellEditing();
		}
		
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			this.value = null;
			((JTextField)getComponent()).setBorder(editorBorder);
			return super.getTableCellEditorComponent(table, Utils.i2s(((Integer)value).intValue()), isSelected, row, column);
		}
		
		public Object getCellEditorValue() {
			return value;
		}
	}
}