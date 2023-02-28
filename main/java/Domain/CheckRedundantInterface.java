package Domain;

import DataSource.ClassData;
import DataSource.ClassType;
import DataSource.MethodData;

import java.util.*;

public class CheckRedundantInterface implements Check {

    public List<Warning> check(ClassRegistry classRegistry) {
        List<Warning> warnings = new LinkedList<>();

        for(ClassData classData : classRegistry.getAllClasses()) {
            warnings.addAll(checkClass(classRegistry, classData));
        }

        return warnings;
    }

    private List<Warning> checkClass(ClassRegistry classRegistry, ClassData classData) {
        List<Warning> warnings = new ArrayList<>();

        if(classData.getClassType() != ClassType.CONCRETE || classData.getInterfaces().size() == 0) {
            return warnings;
        }

        List<String> interfaces = classData.getInterfaces();
        ClassData cd = classRegistry.get(classData.getSuperClassName());
        while(cd != null) {

            for(String interfaceName : interfaces) {
                ClassData interfaceClass = classRegistry.get(interfaceName);
                for(MethodData interfaceMethodData : interfaceClass.getMethods()) {
                    for(MethodData classMethodData : cd.getMethods()) {
                        if(interfaceMethodData.getSignature().equals(classMethodData.getSignature())) {
                            List<WarningLocation> locations = Collections.singletonList(new WarningLocation(classData.getUserFriendlyClassName(), classMethodData.getSignature()));
                            String sb = "Class both inherits and implements method from class " + cd.getUserFriendlyClassName() +
                                    " and interface " + interfaceClass.getUserFriendlyClassName();
                            warnings.add(new Warning(locations, WarningType.REDUNDANT_INTERFACE, sb));
                        }
                    }
                }
            }

            if(cd.getSuperClassName().equals("java.lang.Object")) {
                break;
            }

            ClassData parent = classRegistry.get(cd.getSuperClassName());
            cd = parent;
        }


        return warnings;

    }
}
