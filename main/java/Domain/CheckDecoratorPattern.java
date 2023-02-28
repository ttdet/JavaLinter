package Domain;

import DataSource.*;

import java.util.*;

public class CheckDecoratorPattern implements Check {

    @Override
    public List<Warning> check(ClassRegistry classRegistry) {

        List<Warning> warnings = new LinkedList<>();

        for(ClassData classData : classRegistry.getAllClasses()) {

            DecoratorInformation info = getDecoratorInformation(classRegistry, classData);

            if(info != null) {
                List<WarningLocation> locations = new LinkedList<>();
                locations.add(new WarningLocation(info.decorator));
                locations.add(new WarningLocation(info.componentInterface));
                for(String concreteDecorator : info.concreteDecorators) {
                    locations.add(new WarningLocation(concreteDecorator));
                }
                Warning warning = new Warning(locations, WarningType.DECORATOR_PATTERN_FOUND, "Detected Decorator Pattern with " + info.concreteDecorators.size() + " concrete implementation(s)");
                warnings.add(warning);
                warnings.addAll(checkBadDecoratorPatternImplementation(classRegistry, info));
            }

        }

        return warnings;

    }

    private static class DecoratorInformation {
        String componentInterface = null;
        VariableData concreteComponent = null;
        String decorator = null;
        List<String> concreteDecorators = new LinkedList<>();
    }

    /*
     * Component Interface: An abstract class or interface that defines the methods that the concrete component and decorators must implement.
     * Concrete Component: A class that implements the Component Interface and represents the base object to which additional functionality will be added.
     * Decorator: An abstract class that implements the Component Interface and contains a reference to a Component object. It provides a wrapper around the component object and adds additional behavior or state to it.
     * Concrete Decorator: A class that extends the Decorator class and adds specific behavior or state to the wrapped Component object.
     */
    private DecoratorInformation getDecoratorInformation(ClassRegistry classRegistry, ClassData classData) {

        if(classData.getInterfaces().isEmpty() || classData.getFieldVariables().isEmpty()) {
            return null;
        }

        DecoratorInformation info = new DecoratorInformation();
        List<VariableData> fieldVariables = classData.getFieldVariables();
        List<String> interfaces = classData.getInterfaces();

        for(String interfaceName : interfaces) {
            for(VariableData fieldVariable : fieldVariables) {
                if(classRegistry.classIsA(fieldVariable.getDataType(), interfaceName)) {
                    info.concreteComponent = fieldVariable;
                    info.componentInterface = interfaceName;
                    info.decorator = classData.getName();
                    break;
                }
            }
        }

        if(info.decorator == null) {
            return null;
        }

        List<ClassData> concreteDecorators = classRegistry.getChildren(info.decorator);
        for(ClassData concreteDecorator : concreteDecorators) {
            if(concreteDecorator.getName().equals(classData.getName())) continue;
            info.concreteDecorators.add(concreteDecorator.getName());
        }

        return info;

    }

    private List<Warning> checkBadDecoratorPatternImplementation(ClassRegistry classRegistry, DecoratorInformation info) {

        List<Warning> warnings = new LinkedList<>();

        ClassData decorator = classRegistry.get(info.decorator);

        if(decorator.getClassType() != ClassType.ABSTRACT) {
            warnings.add(new Warning(new WarningLocation(info.decorator), WarningType.DECORATOR_NOT_ABSTRACT, "The decorator is not abstract"));
        }

        if(!info.concreteComponent.getModifiers().contains(AccessModifiers.PROTECTED)) {
            warnings.add(new Warning(new WarningLocation(info.decorator), WarningType.DECORATOR_CONCRETE_COMPONENT_NOT_PROTECTED, "The concrete component field is not protected"));
        }

        warnings.addAll(checkBadDecoratorClass(classRegistry, classRegistry.get(info.decorator), info.concreteComponent));
        for(String concreteDecoratorStr : info.concreteDecorators) {
            ClassData concreteDecorator = classRegistry.get(concreteDecoratorStr);
            warnings.addAll(checkBadDecoratorClass(classRegistry, concreteDecorator, info.concreteComponent));
        }

        return warnings;

    }

    private List<Warning> checkBadDecoratorClass(ClassRegistry classRegistry, ClassData concreteDecorator, VariableData concreteComponent) {
        List<Warning> warnings = new LinkedList<>();

        boolean properConstructorParams = false;

        for(MethodData methodData : concreteDecorator.getMethods()) {
            WarningLocation location = new WarningLocation(concreteDecorator.getName(), methodData.getSignature());
            if(methodData.getMethodName().equals("<init>")) {
                List<String> params = methodData.getParams();
                for(String param : params) {
                    if(classRegistry.classIsA(param, concreteComponent.getDataType())) {
                        properConstructorParams = true;
                        break;
                    }
                }
            } else {
                for(InstructionData instructionData : methodData.getInstructions()) {
                    if(instructionData.getType() == InstructionType.SELF_METHOD_CALL && !((MethodCallInstruction) instructionData).getMethodOwner().equals(concreteDecorator.getName())) {
                        warnings.add(new Warning(location, WarningType.DECORATOR_SUPER_CALL, "The decorator is calling \"super\" (call the concrete component instead)"));
                    }
                }
            }

        }

        if(!properConstructorParams) {
            WarningLocation location = new WarningLocation(concreteDecorator.getName());
            warnings.add(new Warning(location, WarningType.DECORATOR_CONSTRUCTOR_PARAMS, "A decorator's constructor should take in the concrete component as a parameter"));
        }

        return warnings;
    }

}
