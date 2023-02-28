package Presentation;

import Domain.Warning;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class FileOutput implements WarningOutputStrategy {

    @Override
    public void outputWarnings(List<Warning> warnings, Map<String, Object> config) {

        Map<String, Object> fileOutputConfig = (Map<String, Object>) ((Map<String, Object>) config.get("output")).get("FileOutput");

        boolean enabled = (boolean) fileOutputConfig.get("enabled");
        String filePath = (String) fileOutputConfig.get("file");

        if (enabled) {
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date currentDate = new Date();
                writer.write("Linter warnings generated on " + formatter.format(currentDate) + "\n\n");
                writer.write("Directories:\n");
                for(String directory : (List<String>) config.get("directories")) {
                    writer.write(directory + "\n");
                }
                writer.write("\n==================\n");
                writer.newLine();

                for (Warning warning : warnings) {
                    writer.write(warning.generateFullWarning());
                    writer.newLine();
                }

                writer.close();
            } catch (IOException e) {
                System.err.println("Error writing warnings to file: " + e.getMessage());
            }
        }
    }
}
