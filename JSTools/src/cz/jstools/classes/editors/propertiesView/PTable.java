package cz.jstools.classes.editors.propertiesView;

import java.awt.Insets;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;


/**
 * Vlastni verze abulky, ktera spravne rendruje cisla a poskutuje editor
 * v zavislosti na typu bunky a ne v zavislosti na typu sloupce (coz je
 * default). S vyhodou se da vyuzit pro Properties table.
 * Zarucene pouzitelne hodnoty pro tuto tanulku
 * jsou Double, Integer, String, Boolean, JComboBox a Icon.
 *
 *
 * @author   Svoboda Jiri, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.2 $</dt>
 *               <dt>$Date: 2012/03/03 18:31:32 $</dt></dl>
 */
public class PTable extends JTable {
	private static final long  serialVersionUID = 3701237918867168393L;

	/** define a step of nesting level */
	private static final int  LEVEL_SKIP = 15;


	public PTable(boolean showUnits) {
		// table model se musi zadat uz v konstruktoru jelikoz mam prepsanou
		// metodu getModel(), ktera uz vraci muj typ. JTable pri inicializaci
		// zrejme nekde vola getModel() a pokud je implicitni model TableModel
		// hazi to cast exception
		super(new PTableModel(showUnits));
		
		getTableHeader().setReorderingAllowed(false);  // do not allow reorganization of columns
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setRowSelectionAllowed(false);
		setColumnSelectionAllowed(false);
		setFocusable(false);
		setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		int fontHeight = (int)Math.ceil(super.getFont().getSize2D());
		double scale = 1.3;
		int rowHeight = (int)Math.round(scale*fontHeight + getRowMargin());
		rowHeight = Math.max(rowHeight, 16+getRowMargin());
		setRowHeight(rowHeight);
	}
	
	/**
	 * Overrides JTable.getModel(), so now getModel() returns a subclass of TableModel
	 * (PTableModel).
	 */
	public PTableModel getModel() {
		return ((PTableModel)super.getModel());
	}
	
	/**
	 * Package private pomocna funkce tvorici levy okraj v zavislosti na urovni zanoreni
	 */
	static Insets getLevelMargin(int level) {
		return new Insets(0, level*LEVEL_SKIP, 0, 0);
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////
	//                                 RENDERER SECTION                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ackoli normalce tato funkce ma parametry radek a sloupec,
	 * v JTable se standardne uzival pouze sloupec, ktery z column modelu
	 * vzal prislusny renderer.
	 */
	public TableCellRenderer getCellRenderer(int row, int column) {
		PItem pItem = getModel().getPropertyItemAt(row);
		if (column == 0) { 
			return new PropertyNameRenderer(pItem);
		}
		else if (column == 1) {
			return new PropertyValueRenderer(pItem);
		}
		else if (column == 2) {
			return new PropertyValueRenderer(pItem);
		}
		return null;
	}


	//////////////////////////////////////////////////////////////////////////////////////////
	//                                   EDITOR SECTION                                     //
	//////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Ackoli normalce tato funkce ma parametry radek a sloupec,
	 * v JTable se standardne uzival pouze sloupec, ktery z column modelu
	 * vzal prislusny editor.
	 *
	 * Puvodne se vybiral editor podle tridy sloupce (ci se bral editor
	 * nastaveny v TableColumnModelu). Jenze v "properties table" ma kazda
	 * radka jinou tridu pro hodnoty a proto je butno vybirat editor
	 * primo pro konkretni bunku. Nastesti getCellEditor pouzivaji vsechny
	 * interni metody k ziskani editoru, takze lze snadno prepsat jeho chovani
	 * a prizpusobit ho memu.
	 * 
	 * Pozn. Kdyby byla k dispozici v TableModelu funkce getColumnClass(col, row)
	 * tak bych mohl ovlivnit chovani jiz v modelu a do tabulky standartdne pridat
	 * metodou setDefaultEditor(columnClass) dalsi editory pro jednotlive druhy
	 * obsahu. Jenze v modelu je
	 * pouze metoda getColumnClass(col) (coz je m.j. logicke), takze tenhle figl
	 * nelze pouzit a musim na to jit pres getCellEditor...
	 */
	public TableCellEditor getCellEditor(int row, int column) {
		PItem pItem = getModel().getPropertyItemAt(row);
		
		if (column == 0) {
			return new PropertyNameEditor(pItem);  
		}
		else if (column == 1) {
			if (pItem.isList()) {
				return new ListEditor(pItem.getList());
			} else if (pItem.isBrowsable()) {
				return new BrowsableEditor(pItem.getObjectToBrowse(), pItem.isDirectInput());
			} else if (pItem.getValue() instanceof VBoolean) {
				//return new BooleanEditor((VBoolean)pItem.getValue());
				return new ListEditor(((VBoolean)pItem.getValue()).getBooleanStates());
			} else {
				return new PropertyValueEditor(pItem.getValueChecker());
			}
		}

		return null;
	}
}