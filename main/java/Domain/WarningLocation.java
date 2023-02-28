package Domain;

public class WarningLocation {
	private String className;
	private String methodSignature;


	public WarningLocation(String className, String methodSignature) {
		this.className = className;
		this.methodSignature = methodSignature;
	}

	public WarningLocation(String className) {
		this(className, null);
	}

	public String locationToString() {
		String location = this.className;

		if (this.methodSignature != null) {
			location += " -- " + this.methodSignature;
		}

		return location;
	}

	public boolean sameLocation(WarningLocation other) {
		return this.locationToString().equals(other.locationToString());
	}

}
