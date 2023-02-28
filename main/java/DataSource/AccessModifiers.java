package DataSource;

public enum AccessModifiers {
	PUBLIC,
	PRIVATE,
	PROTECTED,
	DEFAULT,
	STATIC,
	FINAL;

	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}
