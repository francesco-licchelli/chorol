package it.unibo.tesi.joliegraph.flow.graph;

public class State {
	private static int stateCounter = 0;
	private final String id;
	private boolean isMain;
	private String label;
	private StateType type;

	private State(String id, String label) {
		this.id = id;
		this.label = label;
		this.type = StateType.NORMAL;
		this.isMain = false;
	}

	public static State createState() {
		return new State(Integer.toString(State.stateCounter++), null);
	}

	public String getId() {
		return this.id;
	}

	void setLabel(String label) {
		this.label = label;
	}

	public StateType getStateType() {
		return this.type;
	}

	public void setStateType(StateType type) {
		this.type = type;
	}

	public boolean isMain() {
		return this.isMain;
	}

	public void setMain() {
		this.isMain = true;
	}

	@Override
	public String toString() {
		return this.id;
	}

	public String toPrettyString() {
		return this.label;
	}
}
