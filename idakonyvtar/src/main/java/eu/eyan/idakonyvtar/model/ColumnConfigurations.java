package eu.eyan.idakonyvtar.model;

public enum ColumnConfigurations {
	MULTIFIELD("MultiMező"), AUTOCOMPLETE("AutoComplete"), MARC_CODE("MarcKód"), REMEMBERING(
			"Emlékező"), SHOW_IN_TABLE("Táblázatban");

	private String name;

	ColumnConfigurations(String configurationNév) {
		this.name = configurationNév;
	}

	public String getName() {
		return name;
	}
}