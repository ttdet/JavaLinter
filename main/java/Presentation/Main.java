package Presentation;

import java.util.*;

import DataSource.*;
import Domain.CheckManager;
import Domain.ClassRegistry;
import Domain.Warning;

public class Main {

	private static final List<WarningOutputStrategy> OUTPUTS = Arrays.asList(
			new ConsoleOutput(),
			new FileOutput()
	);

	private static final String CONFIG_PATH = "./config.yaml";

	public static void main(String[] args) {

		try {

			runLinter();

		} catch (RuntimeException exception) {

			exception.printStackTrace();
			System.out.println("\nPress enter to exit...");
			new Scanner(System.in).nextLine();

		}

	}

	private static void runLinter() {
		ConfigParser configParser = new ConfigParser();
		ClassParser classParser = new ClassParser();
		CheckManager checkManager = new CheckManager();
		Map<String, Object> config = configParser.parseConfig(CONFIG_PATH);
		List<String> directories = (List<String>) config.get("directories");

		List<ClassData> classes = new ArrayList<>();
		for(String directory : directories) {
			classParser.recursivelyParseAllClassFilesInDirectory(directory, classes);
		}

		ClassRegistry classRegistry = new ClassRegistry(classes);

		List<Warning> warnings = checkManager.runAllChecks(classRegistry, config);

		for(WarningOutputStrategy warningOutput : OUTPUTS) {
			warningOutput.outputWarnings(warnings, config);
		}
	}

}
