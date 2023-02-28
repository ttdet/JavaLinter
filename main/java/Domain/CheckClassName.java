package Domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import DataSource.ClassData;

public class CheckClassName implements Check {

    private static final String PASCAL_CASE_REGEX = "^[A-Z][a-z]+(?:[A-Z][a-z]+)*[A-Z]?$";
    private static final String NUMERICAL_REGEX = ".*\\d.*";

    @Override
    public List<Warning> check(ClassRegistry classRegistry) {
        List<Warning> warnings = new ArrayList<>();

        for(ClassData classData : classRegistry.getAllClasses()) {
            String simpleClassName = classData.getShortenedClassName();
            WarningLocation warningLocation;

            if(containsNumbers(simpleClassName)) {
                warningLocation = new WarningLocation(classData.getUserFriendlyClassName());
                warnings.add(new Warning(warningLocation, WarningType.CLASS_NAME_NUMBERS, "Class contains a number in its name"));
            }
            else if(simpleClassName.contains("_")) {
                warningLocation = new WarningLocation(classData.getUserFriendlyClassName());
                warnings.add(new Warning(warningLocation, WarningType.CLASS_NAME_UNDERSCORE, "Class contains an underscore in its name"));
            }
            else if(!isPascalCase(simpleClassName)) {
                warningLocation = new WarningLocation(classData.getUserFriendlyClassName());
                warnings.add(new Warning(warningLocation, WarningType.CLASS_NAME_CASE, "Class does not have a proper PascalCase name"));
            }
        }

        return warnings;
    }

    private boolean isPascalCase(String className) {
        return className.matches(PASCAL_CASE_REGEX);
    }

    private boolean containsNumbers(String className) {
        return className.matches(NUMERICAL_REGEX);
    }
    
}
