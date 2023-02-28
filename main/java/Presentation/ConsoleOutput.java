package Presentation;

import Domain.Warning;

import java.util.List;
import java.util.Map;

public class ConsoleOutput implements WarningOutputStrategy {

    @Override
    public void outputWarnings(List<Warning> warnings, Map<String, Object> config) {

        Map<String, Object> fileOutputConfig = (Map<String, Object>) ((Map<String, Object>) config.get("output")).get("ConsoleOutput");

        boolean enabled = (boolean) fileOutputConfig.get("enabled");

        if(enabled) {

            System.out.println("Running linter...\n");
            System.out.println("Directories:");
            for(String directory : (List<String>) config.get("directories")) {
                System.out.println(directory + "\n");
            }
            System.out.println("===============\n");

            for(Warning warning : warnings) {
                System.out.println(warning.generateFullWarning());
            }

        }

    }

}
