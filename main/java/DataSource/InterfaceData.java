package DataSource;

import java.util.LinkedList;
import java.util.List;

/**
 * Obsolete
 */
public class InterfaceData {
    
    private ClassData myInterface;
    private List<ClassData> implementedBy;

    public InterfaceData(ClassData myInterface) {
        this.myInterface = myInterface;
        this.implementedBy = new LinkedList<>();
    }

    public List<ClassData> getImplementedBy() {
        return this.getImplementedBy();
    }

    public ClassData getInterface() {
        return this.myInterface;
    }

    public void addImplementedBy(ClassData javaClass) {
        this.implementedBy.add(javaClass);
    }

}
