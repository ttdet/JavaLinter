package Domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Warning {
	private WarningType warningType;
    private List<WarningLocation> locations;
    private String warningText;

    public Warning(List<WarningLocation> locations, WarningType type, String warningText) {
        this.locations = new LinkedList<>(locations);
        this.warningType = type;
        this.warningText = warningText;
    }
    
    public Warning(List<WarningLocation> locations, WarningType type) {
        this(locations, type, "");
    }

    public Warning(WarningLocation location, WarningType type) {
        this(location, type, "");
    }

    public Warning(WarningLocation location, WarningType type, String warningText) {
        this(new LinkedList<WarningLocation>(Collections.singletonList(location)), type, warningText);
    }

    public Warning(WarningType type, String warningText) {
        this(new LinkedList<WarningLocation>(), type, warningText);
    }

    public Warning(WarningType type) {
        this(new LinkedList<WarningLocation>(), type, "");
    }
    
    public String generateFullWarning() {   
        String res = this.warningType.toString() + ": " + this.warningText;
        if (this.locations.size() == 0) return res + "\n";
        String locations = " at \n";
        for (WarningLocation fl: this.locations) {
            String locStr = fl.locationToString();
            locations += locStr + "\n";
        }
        return res + locations;
    }

    public void setWarningText(String text) {
        this.warningText = text;
    }

    public WarningType getWarningType() {
        return warningType;
    }

    public String getWarningText() {
        return warningText;
    }

    public List<WarningLocation> getLocations() {
        return this.locations;
    }
    
    public boolean sameLocations(Warning other) {
        List<WarningLocation> otherLocations = other.getLocations();
        if (this.locations.size() != otherLocations.size()) return false;
        if (!this.locations.containsAll(otherLocations)) return false;
        return true;
    }

}
