package edu.buffalo.cse.jive.finiteStateMachine.models;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.buffalo.cse.jive.finiteStateMachine.util.Pair;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 *
 */
/**
 * Given the adjacency list and the root state, builds the String required for
 * SVG generator.
 *
 */
public class TransitionBuilder {

	private StringBuilder transitions;
	private State rootState;
	private Map<State, Set<State>> states;

	public TransitionBuilder(State rootState, Map<State, Set<State>> states) {
		transitions = new StringBuilder();
		transitions.append("@startuml\n");
		this.rootState = rootState;
		this.states = states;
	}

	private void addInitialState(State state, boolean result) {
		if (result)
			this.transitions.append("(*) --> " + "\"" + state.toString() + "\"");
		else
			this.transitions.append("(*) --> " + "\"" + state.toString() + "\"" + " #red");
		addNewLine();
	}

	public String getTransitions() {
		return new StringBuilder(transitions).append("@enduml\n").toString();
	}

	private void addTransition(State state1, State state2, boolean result) {
		if (!result) {
			addColorTransition(state1, state2, "red");
		} else {
			String s = "\"" + state1.toString() + "\"" + " --> " + "\"" + state2.toString() + "\"";
			this.transitions.append(s);
			addNewLine();
		}
	}

	private void addColorTransition(State state1, State state2, String color) {
		String s = "\"" + state1.toString() + "\"" + " --> " + "\"" + state2.toString() + "\"" + " #" + color;
		this.transitions.append(s);
		addNewLine();
	}

	private void addNewLine() {
		this.transitions.append("\n");
	}

	public void build() {
		addInitialState(rootState, rootState.isValid());
		buildTransitions(null, rootState, new HashSet<Pair<State, State>>());
	}

	private void buildTransitions(State prev, State curr, Set<Pair<State, State>> visited) {
		for (State next : states.get(curr))
			if (visited.add(new Pair<State, State>(curr, next)))
				buildTransitions(curr, next, visited);
		if (prev != null)
			addTransition(prev, curr, curr.isValid());
	}
}
