package Domain;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import DataSource.ClassData;
import DataSource.InstructionData;
import DataSource.InstructionType;
import DataSource.MethodCallInstruction;
import DataSource.MethodData;

/**
 * @author Qingyuan Jiao
 * Check: A class that contains only static methods shouldn't have a non-default constructor
 */
public class CheckRedundantConstructor implements Check{

    @Override
    public List<Warning> check(ClassRegistry classRegistry) {
        List<Warning> warnings = new LinkedList<>();
        for (ClassData classData: classRegistry.getAllClasses()) {
            List<MethodData> constructors = this.getConstructorIfAllStaticMethods(classData);
            if (constructors == null) continue;
            if (constructors.size() > 1) {
                warnings.add(this.buildWarning(classData.getName()));
            } else {
                MethodData constructor = constructors.get(0);
                List<InstructionData> instList = constructor.getInstructions();
                if (instList.size() > 2) { //default constructor has only two instructions
                    warnings.add(this.buildWarning(classData.getName()));
                }
                
            }
        }
        return warnings;
    }

    private List<MethodData> getConstructorIfAllStaticMethods(ClassData classData) {
        List<MethodData> methods = classData.getMethods();
        List<MethodData> constructors = new LinkedList<>();
        for (MethodData method: methods) {
            String methodName = method.getName();
            if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
                constructors.add(method);
                continue;
            }
            if (!method.isStatic()) return null;
        }
        return constructors;
    }

    private Warning buildWarning(String className) {
        WarningLocation location = new WarningLocation(className);
        List<WarningLocation> locations = new LinkedList<>();
        locations.add(location);
        Warning w = new Warning(locations, WarningType.REDUNDANT_CONSTRUCTOR);
        w.setWarningText("class containing only static methods doesn't need constructors");
        return w;
    }

}
