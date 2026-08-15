package com.example.game.quest.domain;

public final class ObjectiveProgress {

	private int currentAmount;
	private boolean completed;

	public ObjectiveProgress(int currentAmount, boolean completed) {
		this.currentAmount = currentAmount;
		this.completed = completed;
	}

	public int currentAmount() {
		return currentAmount;
	}

	public boolean completed() {
		return completed;
	}

	public boolean setAmount(int amount, int required) {
		int next = Math.min(required, Math.max(0, amount));
		boolean nextCompleted = next >= required;
		boolean changed = next != currentAmount || nextCompleted != completed;
		currentAmount = next;
		completed = nextCompleted;
		return changed;
	}

	public boolean add(int delta, int required) {
		return setAmount(currentAmount + delta, required);
	}
}
