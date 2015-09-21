package eu.eyan.idakonyvtar.model;

import static com.google.common.collect.Lists.newArrayList;

import java.util.ArrayList;
import java.util.List;

import eu.eyan.idakonyvtar.oszk.Marc;

//FIXME: Refactor, because it cannot be understood, constant pain i t a
public class ColumnKonfiguration {
	public static enum ColumnConfigurations {
		MULTIFIELD("MultiMező"), AUTOCOMPLETE("AutoComplete"), MARC_CODE(
				"MarcKód"), REMEMBERING("Emlékező"), SHOW_IN_TABLE(
				"Táblázatban");

		private String name;

		ColumnConfigurations(String configurationNév) {
			this.name = configurationNév;
		}

		public String getName() {
			return name;
		}
	}

	private String[][] table;

	public boolean isTrue(String columnName,
			ColumnConfigurations columnConfiguration) {
		return getValue(columnName, columnConfiguration).equalsIgnoreCase(
				"Igen");
	}

	private String getValue(String columnName,
			ColumnConfigurations columnConfiguration) {
		int columnIndex = getColumnIndex(columnName);
		int configurationIndex = getConfigurationIndex(columnConfiguration);
		if (columnIndex > 0 && configurationIndex > 0) {
			return table[configurationIndex][columnIndex];
		}
		return "";
	}

	private int getColumnIndex(String columnName) {
		if (table.length > 0) {
			for (int configurationIndex = 0; configurationIndex < table[0].length; configurationIndex++) {
				if (table[0][configurationIndex].equalsIgnoreCase(columnName)) {
					return configurationIndex;
				}
			}
		}
		return -1;
	}

	private int getConfigurationIndex(ColumnConfigurations configurationName) {
		for (int columnIndex = 0; columnIndex < table.length; columnIndex++) {
			if (table[columnIndex][0].equalsIgnoreCase(configurationName
					.getName())) {
				return columnIndex;
			}
		}
		return -1;
	}

	public List<Marc> getMarcCodes(String columnName) throws MarcException {
		ArrayList<Marc> ret = newArrayList();
		try {
			String[] marcCodeTexts = getValue(columnName,
					ColumnConfigurations.MARC_CODE).split(",");
			for (String string : marcCodeTexts) {
				String[] codes = string.split("-");
				if (codes.length > 2) {
					ret.add(new Marc(codes[0], codes[1], codes[2], null));
				}
			}
		} catch (Exception e) {
			throw new MarcException(
					"A Marc kódot nem lehet a configurationból beolvasni: "
							+ columnName);
		}
		return ret;
	}

	public List<String> getRememberingColumns() {
		List<String> rememberingColumnok = newArrayList();
		for (int columnIndex = 1; columnIndex < table[0].length; columnIndex++) {
			if (isTrue(table[0][columnIndex], ColumnConfigurations.REMEMBERING)) {
				rememberingColumnok.add(table[0][columnIndex]);
			}
		}
		return rememberingColumnok;
	}

	public String[][] getTable() {
		return table;
	}

	public void setTable(String[][] table) {
		this.table = table;
	}

	public static class Builder {
		public Builder(int columns, int rows) {
			this.columnConfiguration.setTable(new String[columns][rows]);
			this.actualRow = 0;
		}

		private int actualRow;
		private ColumnKonfiguration columnConfiguration = new ColumnKonfiguration();

		// FIXME phüjj...
		public Builder withRow(String... values) {
			for (int i = 0; i < values.length; i++) {
				this.columnConfiguration.getTable()[i][actualRow] = values[i];
			}
			actualRow++;
			return this;
		}

		public ColumnKonfiguration build() {
			return this.columnConfiguration;
		}
	}
}
