package cz.jstools.classes.editors;

import java.text.NumberFormat;

import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import cz.jstools.classes.definitions.Utils;




/**
 * Custom version of JSpinner that satisfactorily renders numbers.
 * It means --- numbers are converted via Utils conversion
 * methods like all numbers in RestraxGUI program.
 *
 *
 * @author   Svoboda Jan Saroun, PhD.
 * @version  <dl><dt>$Name: HEAD $</dt>
 *               <dt>$Revision: 1.1 $</dt>
 *               <dt>$Date: 2014/06/18 20:07:34 $</dt></dl>
 */
public class CSpinner extends JSpinner {
	private static final long serialVersionUID = 4648808924545964606L;


	public CSpinner() {
		super();
	}
	public void setModel(SpinnerModel model) {
		super.setModel(model);
		if (model instanceof SpinnerNumberModel) {
			super.setEditor(new CNumberEditor(this));
		}
	}
	
	
	/**
	 * Custom version of editor. JSpinner strictly uses JFormattedTextField
	 * so only way how to change formatting is via its formatter. One can
	 * specify DecimalFormat and set its conversion pattern. DecimalFormat
	 * is taken from Utils in order to all conversion patterns were located
	 * in one place.
	 */
	protected static class CNumberEditor extends NumberEditor {
		private static final long  serialVersionUID = -2599041323031148191L;

		public CNumberEditor(JSpinner spinner) {
			super(spinner);
			
			getTextField().setFormatterFactory(
				new DefaultFormatterFactory(
					new CNumberEditorFormatter(
						(SpinnerNumberModel)spinner.getModel(),
						Utils.getNumberFormat())));
		}			
	}
	
	/**
	 * There is need to use custom number formatter in JSPinner. Formatter
	 * takes minimum and maximum values from spinner model. There is a
	 * JSpinner.NumberEditorFormatter that takes a decimal format pattern,
	 * but strictly takes decimal format symbols from current locale.
	 * But one can set a fix decimal format symbols that are included in
	 * decimal format. So JSpinner.NumberEditorFormatter should have a constructor
	 * that takes a DecimalFormat argument. Such constructor exosts but is
	 * private (one does not know why!!! It should be protected!).
	 * That's why it must be newly implemented here.
	 */
	protected static class CNumberEditorFormatter extends NumberFormatter {
		private static final long  serialVersionUID = -1330803577970064995L;

		/** local copy of spinner number model */
		private final SpinnerNumberModel model;


		public CNumberEditorFormatter(SpinnerNumberModel model, NumberFormat format) {
			super(format);
			this.model = model;
			setValueClass(model.getValue().getClass());
		}
		
		public void setMinimum(Comparable min) {
			model.setMinimum(min);
		}
		
		public Comparable getMinimum() {
			return  model.getMinimum();
		}
		
		public void setMaximum(Comparable max) {
			model.setMaximum(max);
		}
		
		public Comparable getMaximum() {
			return model.getMaximum();
		}
	}
}