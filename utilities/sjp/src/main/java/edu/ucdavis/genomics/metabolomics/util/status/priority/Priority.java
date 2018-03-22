package edu.ucdavis.genomics.metabolomics.util.status.priority;

/**
 * general priority
 * @author wohlgemuth
 *
 */
public enum Priority {
	TRACE(1, "trace"), DEBUG(2, "debug"), INFO(3, "info"), WARNING(4, "warning"), ERROR(5, "error"), FATAL(6, "fatal");

	private int position = 0;

	private String name = "no name assigned";

	public int getPosition() {
		return position;
	}

	private Priority(int position, String name) {
		this.position = position;
		this.name = name;
	}

	public String toString() {
		return name + "(" + position + ")";
	}

}
