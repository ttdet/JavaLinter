package Domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import DataSource.ClassData;
import DataSource.ClassType;
import DataSource.InstructionData;
import DataSource.InstructionType;
import DataSource.MethodCallInstruction;
import DataSource.MethodData;
import DataSource.VariableData;

/**
 * @author Qingyuan Jiao
 * This class checks for abstract factory pattern. 
 * When an application of the pattern is found, it will generate a warning containing the client and the abstract factories it uses. 
 * A variable (af) is considered a valid abstract factory of a client (c) if:
 *  1. af is a field variable of c.
 *  2. af is an interface.
 *  3. af contains at least 2 methods.
 *  4. c has field variables of all the types returned by methods in af. 
 *  5. All methods in af are called at least once. 
 * 
 * A few notes:
 *  1. This detection model does not mandate passing the abstract factory through the constructor. 
 *  2. Multiple abstract factories within one client is allowed and separate warning messages will be generated. 
 *      However, the client class must have enough number of fields for additional absract factories to be valid. Consider the following example:
 *          interface FactoryA{ ClassA methodA(); ClassB methodB();}
 *          interface FactoryB{ ClassA methodC(); ClassC methodD();}
 *      In this case, the client class must have at least two fields of type ClassA, one field of type ClassB, one field of type ClassC 
 *      for the two factories to be valid. 
 *  3. If enforceCallInOneMethod is true, a factory is only valid when all of its methods are called within a single method of the client.
 *      If it is false, a factory is valid as long as all its methods are called throughout the client class. 
 *  4. This model does not mandate assigning [the return value of a call to a abstract factory method] to [a field variable the client holds].
 *  5. If an abstract factory has a method that returns itself or other recognized abstract factories, it is not valid. 
 *      
 */
public class CheckAbstractFactoryPattern implements Check{

    private Map<String, ClassData> interfaces;
    private List<Warning> warnings;
    private boolean enforceCallInOneMethod; //deprecated; default to true


    public CheckAbstractFactoryPattern() {
        this.interfaces = new HashMap<>();
        this.warnings = new LinkedList<>();
        this.enforceCallInOneMethod = true;
    }

    @Override
    public List<Warning> check(ClassRegistry classRegistry) {
        List<ClassData> filesToCheck = new LinkedList<>(classRegistry.getAllClasses());
        for (ClassData javaClass: filesToCheck) {
            String className = javaClass.getName();
            if (javaClass.getClassType() == ClassType.INTERFACE) {
                this.interfaces.put(className, javaClass);
            }
        }

        for (ClassData javaClass: filesToCheck) {
            this.checkOneClass(javaClass);
        }

        return this.warnings;
        
    }

    /**
     * Run check on one class
     * @param javaClass
     */
    private void checkOneClass(ClassData javaClass) {
        //Set<String> abstractFactories = new HashSet<>();
        List<VariableData> fields = javaClass.getFieldVariables();
        List<VariableData> interfaceFields = new LinkedList<>(); 
        Map<String, Integer> fieldsTypes = new HashMap<>();
        for (VariableData field: fields) {
            String fieldType = field.getDataType();
            if (this.interfaces.containsKey(fieldType)) {
                interfaceFields.add(field);
            }
            Integer count = fieldsTypes.get(fieldType);
            if (count == null) {
                fieldsTypes.put(fieldType, 1);
            } else {
                fieldsTypes.put(fieldType, count + 1);
            }
        }
        
        //check each interface field to see if it's a abstract factory
        for (VariableData interfaceField: interfaceFields) {
            
            if (fieldsTypes.get(interfaceField.getDataType()) < 1) continue;
            Map<String, Integer> fieldTypesModified = this.isAbstractFactory(interfaceField, fieldsTypes);
            if (fieldTypesModified != null && this.ifUsedToInitializeFields(interfaceField, javaClass)) {
                fieldsTypes = fieldTypesModified;
                Warning w = new Warning(WarningType.ABSTRACT_FACTORY_FOUND);
                String text = String.format("Client class: %s. Abstract Factory it uses: %s", 
                                    javaClass.getUserFriendlyClassName(), 
                                    interfaceField.getDataType());
                w.setWarningText(text);
                this.warnings.add(w);
                //abstractFactories.add(interfaceField.getDataType());
            }
        }
    }

    /**
     * Checks if an interface field is a potential abstract factory.
     * That is, if the class contains fields of all the types returned by the potential abstract factory's method.  
     * @param interfaceField The potential abstract factory
     * @param fieldTypes A mapping from fields of the potential client and their counts
     * @param abstractFactories A set that contains all recognized Abstract Factories field of the potential clinet
     * @return 
     */
    private Map<String, Integer> isAbstractFactory(VariableData interfaceField, Map<String, Integer> fieldTypes) {
        
        String interfaceName = interfaceField.getDataType();
        ClassData interfaceData = this.interfaces.get(interfaceName);
        List<MethodData> interfaceMethods = interfaceData.getMethods();
        Map<String, Integer> fieldTypesLocal = new HashMap<>(fieldTypes);
        if (interfaceMethods.size() < 2) return null; //an abstract factory must have at least two methods
        for (MethodData method: interfaceMethods) {
            String returnType = method.getReturnType();
            // if (returnType.equals(interfaceName) || abstractFactories.contains(returnType)) { //return type cannot be itself or other abstract factories
            //     return false;
            // }
            Integer count = fieldTypes.get(returnType);
            if (count == null || count == 0) { //check if there's a field with the return type of the method
                return null;
            }
            fieldTypesLocal.put(returnType, count - 1); //update the count of fields
        }

        Integer facCount = fieldTypesLocal.get(interfaceName);
        if (facCount < 1) return null;
        fieldTypesLocal.put(interfaceName, facCount - 1);

        return fieldTypesLocal;
    }

    /**
     * Checks all the methods of the potential abstract factory are called, either in the scope of one method or the entire class
     * if enforceCallInOneMethod, all abstract factory methods must be called within one method
     * if not, abstract factory methods can be called in multiple methods
     * @param interfaceData the potential abstract factory
     * @param client the potential client
     * @return true if the all factory methods are called
     */
    private boolean ifUsedToInitializeFields(VariableData interfaceField, ClassData client) {
        //A map is used here not a set, considering the possibility of multiple methods with the same name
        Map<String, Integer> interfaceMethods = new HashMap<>();
        ClassData interfaceData = this.interfaces.get(interfaceField.getDataType());
        for (MethodData method: interfaceData.getMethods()) {
            String methodName = method.getName();
            Integer count = interfaceMethods.get(methodName);
            if (count == null) {
                interfaceMethods.put(methodName, 1);
            } else {
                interfaceMethods.put(methodName, count + 1);
            }
        }
        List<MethodData> clientMethods = client.getMethods();

        if (this.enforceCallInOneMethod) {
            for (MethodData clientMethod: clientMethods) {
                boolean res = this.ifMethodAppliesFactory(interfaceField.getDataType(), new HashMap<String, Integer>(interfaceMethods), clientMethod);
                if (res) return true;
            }
        } 
        // else {
        //     Map<String, Integer> interfaceMethodsCopy = new HashMap<>(interfaceMethods);
        //     for (MethodData clientMethod: clientMethods) {
        //         interfaceMethodsCopy = this.removeFacMethodsCalled(interfaceField.getDataType(), interfaceMethodsCopy, clientMethod);
        //     }
        //     if (interfaceMethodsCopy.size() == 0) return true;
        //     return false;
        // }

        return false;

    }

    /**
     * Checks if a method calls all of the methods in the potential abstract factory
     * @param interfaceName the potential abstract factory
     * @param interfaceMethods map from abstract factory methods to their counts. In case of no duplicate method names, counts will all be 1
     * @param method the method to run the check on
     * @return true if all factory methods are called
     */
    private boolean ifMethodAppliesFactory(String interfaceName, Map<String, Integer> interfaceMethods, MethodData method) {
        List<InstructionData> instList = method.getInstructions();
        for (int i = 0; i < instList.size(); i++) {
            InstructionData currInst = instList.get(i);
            if (currInst.getType() == InstructionType.METHOD_CALL) {
                MethodCallInstruction methodCallInst = (MethodCallInstruction) currInst;
                
                //strict match enabled. The value must be directly stored to a field var. 
                

                if (methodCallInst.getMethodOwner().equals(interfaceName)) {
                    String methodName = methodCallInst.getMethodName();
                    Integer methodCountInteger = interfaceMethods.get(methodName);
                    if (methodCountInteger == null) continue;
                    if (methodCountInteger == 1) {
                        interfaceMethods.remove(methodName);
                    } else {
                        interfaceMethods.put(methodName, methodCountInteger - 1);
                    }
                    
                }
            }
        }

        if (interfaceMethods.size() == 0) return true;
        return false;
    }

    /**
     * Obselete
     * Updates the abstract factory methods map. Every time a method in the factory is called, its corresponding count in the map will reduce 1
     * @param interfaceName The potential abstract factory
     * @param interfaceMethods map from abstract factory methods to their counts. In case of no duplicate method names, counts will all be 1
     * @param method The method to run checks on
     * @return The updated abstract factory methods map
     */
    // private Map<String, Integer> removeFacMethodsCalled(String interfaceName, Map<String, Integer> interfaceMethods, MethodData method) {
    //     List<InstructionData> instList = method.getInstructions();
    //     for (int i = 0; i < instList.size(); i++) {
    //         InstructionData currInst = instList.get(i);
    //         if (currInst.getType() == InstructionType.METHOD_CALL) {
    //             MethodCallInstruction methodCallInst = (MethodCallInstruction) currInst;
                
    //             //strict match enabled. The value must be directly stored to a field var. 
                

    //             if (methodCallInst.getMethodOwner().equals(interfaceName)) {
    //                 String methodName = methodCallInst.getMethodName();
    //                 Integer methodCountInteger = interfaceMethods.get(methodName);
    //                 if (methodCountInteger == null) continue;
    //                 if (methodCountInteger == 1) {
    //                     interfaceMethods.remove(methodName);
    //                 } else {
    //                     interfaceMethods.put(methodName, methodCountInteger - 1);
    //                 }
                    
    //             }
    //         }
    //     }

    //     return interfaceMethods;
    // }


    
}
