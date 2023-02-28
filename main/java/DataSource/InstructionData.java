package DataSource;

public class InstructionData {
    private InstructionType type;

    public InstructionData(InstructionType type) {
        this.type = type;
    }

    public InstructionType getType() {
        return this.type;
    }

    public String toString() {
        return this.type.toString();
    }

    public void setInstructionType(InstructionType type) {
        this.type = type;
    }

}
