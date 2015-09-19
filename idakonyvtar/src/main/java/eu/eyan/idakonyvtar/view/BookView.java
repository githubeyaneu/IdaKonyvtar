package eu.eyan.idakonyvtar.view;

import static com.google.common.collect.Lists.newArrayList;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import eu.eyan.idakonyvtar.model.ColumnKonfiguration;
import eu.eyan.idakonyvtar.model.ColumnKonfiguration.ColumnConfigurations;

public class BookView extends AbstractView {
	public static final String ISBN_TEXT = "isbnText";

	public static final String ISBN_LABEL = "isbnLabel";

	public List<Component> getEditors() {
		return editors;
	}

	public JLabel getIsbnSearchLabel() {
		return isbnSearchLabel;
	}

	public JTextField getIsbnText() {
		return isbnText;
	}

	public void setIsbnEnabled(boolean isbnEnabled) {
		this.isbnEnabled = isbnEnabled;
	}

	public void setColumnConfiguration(ColumnKonfiguration columnConfiguration) {
		this.columnConfiguration = columnConfiguration;
	}

	private List<String> columns = newArrayList();

	private boolean isbnEnabled = false;

	private List<Component> editors = newArrayList();

	private JLabel isbnSearchLabel = new JLabel();

	private JTextField isbnText = new JTextField();

	private ColumnKonfiguration columnConfiguration;

	@Override
	protected Component createViewComponent() {
		String rowSpec = "";
		if (isbnEnabled) {
			rowSpec += "pref, 3dlu, pref, 3dlu, ";
		}
		rowSpec += rowSpec + "pref";
		for (int i = 0; i < columns.size(); i++) {
			rowSpec += ",3dlu ,pref";
		}
		PanelBuilder panelBuilder = new PanelBuilder(new FormLayout(
				"pref, 3dlu, pref:grow", rowSpec));

		int row = 1;
		if (isbnEnabled) {
			panelBuilder.addSeparator("Isbn", CC.xyw(1, row, 3));
			row = row + 2;
			panelBuilder.add(isbnSearchLabel, CC.xyw(1, row, 1));
			isbnSearchLabel.setName(ISBN_LABEL);
			panelBuilder.add(isbnText, CC.xyw(3, row, 1));
			isbnText.setName(ISBN_TEXT);
			row = row + 2;
		}
		panelBuilder.addSeparator("Adatok", CC.xyw(1, row, 3));
		for (int i = 0; i < columns.size(); i++) {
			row = row + 2;
			String columnName = columns.get(i);
			panelBuilder.addLabel(columnName, CC.xy(1, row));

			Component editor;
			boolean multi = columnConfiguration.isTrue(columnName,
					ColumnConfigurations.MULTIFIELD);
			if (columnConfiguration.isTrue(columnName,
					ColumnConfigurations.AUTOCOMPLETE)) {
				if (multi) {
					editor = new MultiFieldJComboBox(columnName);
				} else {
					JComboBox<String> jComboBox = new JComboBox<String>();
					jComboBox.setEditable(true);
					editor = jComboBox;
				}
			} else {
				if (multi) {
					editor = new MultiFieldJTextField(columnName);
				} else {
					editor = new JTextField(20);
				}
			}
			editor.setName(columnName);
			editors.add(editor);
			panelBuilder.add(editor, CC.xy(3, row));
		}
		return panelBuilder.build();
	}

	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
}
