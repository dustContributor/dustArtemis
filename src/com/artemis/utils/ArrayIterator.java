package com.artemis.utils;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Just a simple iterator over an array.
 * 
 * @author dustContributor
 *
 * @param <T> type of the elements it holds.
 */
public final class ArrayIterator<T> implements Iterator<T> {
	private final T[] data;
	private final int size;
	// Always starts at zero.
	private int cursor;

	public ArrayIterator(final T[] data, final int size) {
		this.data = Objects.requireNonNull(data);
		this.size = requireBetween(size, 0, data.length, "'size' is out of range!");
	}

	@Override
	public final boolean hasNext() {
		return cursor < size;
	}

	@Override
	public final T next() {
		if (cursor < size) {
			// Return current element and increment.
			return data[cursor++];
		}
		throw new NoSuchElementException();
	}

	@Override
	public final void forEachRemaining(final Consumer<? super T> action) {
		Objects.requireNonNull(action);
		// Fetch all we need to iterate.
		final int size = this.size;
		final T[] data = this.data;
		final int cursor = this.cursor;
		// Update cursor so further 'next' calls fail.
		this.cursor = size;
		// Now we're free to iterate over the remaining elements.
		for (int i = cursor; i < size; ++i) {
			action.accept(data[i]);
		}
	}

	/**
	 * This checks the value is equal or greater than the 'lower' limit, and
	 * strictly less than the 'upper' limit.
	 * 
	 * @param value   to validate.
	 * @param lower   inclusive limit for the value.
	 * @param upper   exclusive limit for the value.
	 * @param message to display if value lies outside the bounds.
	 * @return the validated value.
	 */
	private static final int requireBetween(final int value, final int lower, final int upper, final String message) {
		if (value >= lower && value < upper) {
			return value;
		}

		throw new RuntimeException(new StringBuilder(message)
				.append(" 'value' is '").append(value)
				.append("' when it should be >= '").append(lower)
				.append("' and < '").append(upper).append('\'')
				.toString());
	}

}