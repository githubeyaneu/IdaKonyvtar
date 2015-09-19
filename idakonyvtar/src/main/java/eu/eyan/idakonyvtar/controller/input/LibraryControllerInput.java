package eu.eyan.idakonyvtar.controller.input;

import java.io.File;

public class LibraryControllerInput {
	private File file;

	public LibraryControllerInput(File file) {
		this.file = file;
	}

	public File getFile() {
		return file;
	}
}
