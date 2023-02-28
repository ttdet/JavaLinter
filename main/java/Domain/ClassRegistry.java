package Domain;

import DataSource.ClassData;

import java.util.*;

public class ClassRegistry {

    private Map<String, ClassData> classMap;

    public ClassRegistry(List<ClassData> classes) {
        classMap = new HashMap<>();

        for(ClassData classData : classes) {
            classMap.put(classData.getName(), classData);
        }
    }

    public ClassData get(String className) {
        return classMap.get(className);
    }

    public Collection<ClassData> getAllClasses() {
        return classMap.values();
    }

    public List<ClassData> getAncestors(String className) {
        List<ClassData> ancestors = new LinkedList<>();
        ClassData classData = get(className);
        if(classData == null) {
            return ancestors;
        }

        ancestors.add(classData);
        for(String interfaceStr : classData.getInterfaces()) {
            ancestors.add(get(interfaceStr));
        }
        ClassData cd = get(classData.getSuperClassName());
        while(cd != null) {

            ancestors.add(cd);

            for(String interfaceStr : cd.getInterfaces()) {
                ancestors.add(get(interfaceStr));
            }

            if(cd.getSuperClassName().equals("java.lang.Object")) {
                break;
            }

            ClassData parent = get(cd.getSuperClassName());
            cd = parent;
        }

        return ancestors;
    }

    public boolean classIsA(String className, String isA) {

        List<ClassData> ancestors = getAncestors(className);

        for(ClassData ancestor : ancestors) {

            if(ancestor.getName().equals(isA) || ancestor.getInterfaces().contains(isA)) {
                return true;
            }

        }

        return false;

    }

    public List<ClassData> getChildren(String className) {
        List<ClassData> children = new LinkedList<>();
        ClassData classData = get(className);
        if(classData == null) {
            return children;
        }

        Set<String> visited = new HashSet<>();
        Queue<ClassData> queue = new LinkedList<>();
        visited.add(classData.getName());
        queue.add(classData);

        while(!queue.isEmpty()) {
            ClassData cd = queue.remove();
            List<ClassData> subclasses = getSubclasses(cd.getName());
            for(ClassData sub : subclasses) {
                if(!visited.contains(sub.getName())) {
                    visited.add(sub.getName());
                    queue.add(sub);
                }
            }
            if(!cd.getName().equals(className)) children.add(cd);
        }

        return children;
    }

    public List<ClassData> getSubclasses(String className) {
        List<ClassData> subclasses = new LinkedList<>();
        for(ClassData cd : getAllClasses()) {
            if(cd.getSuperClassName().equals(className) || cd.getInterfaces().contains(className)) {
                subclasses.add(cd);
            }
        }

        return subclasses;
    }

}
