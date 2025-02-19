package it.unibo.tesi.chorol.controlflow.graph;

public class State {
	private static int stateCounter = 0;
	private final String id;
	private String label;
	private StateType type;

	private State(String id, String label) {
		this.id = id;
		this.label = label;
		this.type = StateType.NORMAL;
	}

	public static State createState(String label) {
		return new State(Integer.toString(State.stateCounter++), label);
	}

	public String getId() {
		return this.id;
	}

	String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public StateType getStateType() {
		return this.type;
	}

	public void setStateType(StateType type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.label;
	}
}
