package edu.buffalo.cse.jive.finiteStateMachine.monitor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import edu.buffalo.cse.jive.finiteStateMachine.models.Context;
import edu.buffalo.cse.jive.finiteStateMachine.models.Event;
import edu.buffalo.cse.jive.finiteStateMachine.models.State;
import edu.buffalo.cse.jive.finiteStateMachine.parser.expression.expression.Expression;

/**
 * @author Shashank Raghunath
 * @email sraghuna@buffalo.edu
 *
 */
public abstract class Monitor implements Runnable {

	private Set<String> keyFields;
	private BlockingQueue<Event> source;
	private Map<State, Set<State>> states;
	private State rootState;
	private State previousState;

	/**
	 * Initializes Key Fields, source, adjacency list and a dummy state
	 * 
	 * @param keyFields
	 * @param source
	 */
	public Monitor(Set<String> keyFields, BlockingQueue<Event> source) {
		this.keyFields = keyFields;
		this.source = source;
		this.states = new HashMap<State, Set<State>>();
		previousState = new State();
		for (String field : getKeyFields()) {
			if (Event.abbreviations.containsKey(field))
				previousState.getVector().put(Event.abbreviations.get(field), null);
			else
				previousState.getVector().put(field, null);
		}
	}

	/**
	 * Given an event, builds a state and adds it into the adjacency list
	 * 
	 * @param event
	 * @return
	 */
	protected boolean buildStates(Event event) {
		boolean result = false;
		if (keyFields.contains(event.getField()) || keyFields.contains(getEventKey(event.getField()))) {
			State newState = previousState.copy();
			newState.getVector().put(event.getField(), event.getValue());
			if (!newState.getVector().values().contains(null) && !previousState.getVector().values().contains(null)) {
				result = states.get(previousState).add(newState);
				if (!states.containsKey(newState))
					states.put(newState, new LinkedHashSet<State>());
			} else if (!newState.getVector().values().contains(null)
					&& previousState.getVector().values().contains(null)) {
				states.put(newState, new LinkedHashSet<State>());
				rootState = newState;
			}
			previousState = newState;
		}
		return result;
	}

	/**
	 * Simple helper function
	 * 
	 * @param value
	 * @return
	 */
	private String getEventKey(String value) {
		if (Event.abbreviations == null)
			return null;
		for (Map.Entry<String, String> entry : Event.abbreviations.entrySet()) {
			if (entry.getValue().equals(value))
				return entry.getKey();
		}
		return null;
	}

	/**
	 * Validates the adjacency list against the list of properties
	 * 
	 * @param expressions
	 * @return
	 * @throws Exception
	 */
	public boolean validate(List<Expression> expressions) throws Exception {
		boolean result = validate(rootState, expressions);
		rootState.setValid(result);
		return result;
	}

	public void resetStates() {
		for (State key : states.keySet()) {
			for (State state : states.get(key))
				state.reset();
		}
	}

	/**
	 * Hold tight, a long journey begins here.
	 * 
	 * @param root
	 * @param expressions
	 * @return
	 */
	private boolean validate(State root, List<Expression> expressions) {
		boolean valid = true;
		for (Expression expression : expressions) {
			valid = expression.evaluate(new Context(root, null, states)) && valid;
		}
		return valid;
	}

	/**
	 * You're welcome
	 */
	protected void printStates() {
		for (State key : states.keySet()) {
			System.out.print(key + " : ");
			for (State state : states.get(key)) {
				System.out.print(state + " ");
			}
			System.out.println();
		}
	}

	public Set<String> getKeyFields() {
		return keyFields;
	}

	public void setKeyFields(Set<String> keyFields) {
		this.keyFields = keyFields;
	}

	public BlockingQueue<Event> getSource() {
		return source;
	}

	public void setSource(BlockingQueue<Event> source) {
		this.source = source;
	}

	public Map<State, Set<State>> getStates() {
		return states;
	}

	public void setStates(Map<State, Set<State>> states) {
		this.states = states;
	}

	public State getRootState() {
		return rootState;
	}

	public void setRootState(State rootState) {
		this.rootState = rootState;
	}
}
