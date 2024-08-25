package com.artemis.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

import com.artemis.DAConstants;

/**
 * 
 * @author dustContributor
 */
public abstract class ImmutableIntBag {
	protected int[] data;
	protected int size;

	/**
	 * Constructs an empty Bag with an initial capacity of
	 * {@link #DEFAULT_CAPACITY}.
	 * 
	 */
	public ImmutableIntBag() {
		this(DEFAULT_CAPACITY);
	}

	/**
	 * Constructs an empty Bag with the defined initial capacity.
	 * 
	 * <p>
	 * NOTE: If capacity is less than {@value #MINIMUM_WORKING_CAPACITY}, the Bag
	 * will be created with a capacity of {@value #MINIMUM_WORKING_CAPACITY}
	 * instead.
	 * </p>
	 * 
	 * @param capacity of the Bag.
	 */
	public ImmutableIntBag(final int capacity) {
		final int newCap = (capacity > MINIMUM_WORKING_CAPACITY) ? capacity : MINIMUM_WORKING_CAPACITY;
		this.data = new int[newCap];
	}

	/**
	 * Returns the number of items in this bag.
	 * 
	 * @return number of items in this bag.
	 */
	public int size() {
		return size;
	}

	/**
	 * Returns the number of items the bag can hold without growing.
	 * 
	 * @return number of items the bag can hold without growing.
	 */
	public int capacity() {
		return data.length;
	}

	/**
	 * Returns true if this list contains no items.
	 * 
	 * @return true if this list contains no items.
	 */
	public boolean isEmpty() {
		return size < 1;
	}

	/**
	 * Check if the bag contains this item.
	 * 
	 * @param item to check if its contained in the bag.
	 * @return the index of the item, -1 if none of such item was found.
	 */
	public int contains(final int item) {
		final int size = this.size;
		final int[] data = this.data;

		for (int i = 0; i < size; ++i) {
			if (data[i] == item) {
				// Item found. Return its index.
				return i;
			}
		}

		// Item not found.
		return -1;
	}

	/**
	 * Check if the bag contains this item applying the criteria supplied.
	 * 
	 * @param criteria to be used to find an item.
	 * @return the index of the item that met the criteria, -1 if none of such items
	 *         were found.
	 */
	public int contains(final IntPredicate criteria) {
		Objects.requireNonNull(criteria, "criteria");

		final int size = this.size;
		final int[] data = this.data;

		for (int i = 0; i < size; ++i) {
			if (criteria.test(data[i])) {
				// Item found. Return its index.
				return i;
			}
		}

		// Item not found.
		return -1;
	}

	/**
	 * Iterates over the items of this Bag applying the criteria supplied.
	 * 
	 * @param criteria     to be used to find an item.
	 * @param defaultValue to return if the item isn't found.
	 * @return the item that met the criteria or defaultValue if none of such items
	 *         were found.
	 */
	public int find(final IntPredicate criteria, final int defaultValue) {
		Objects.requireNonNull(criteria, "criteria");

		final int index = contains(criteria);

		if (index > -1) {
			return data[index];
		}

		return defaultValue;
	}

	/**
	 * Iterates over each element of this bag and applying the supplied operation.
	 * 
	 * @param operation to be performed on each element of this bag.
	 */
	public void forEach(final IntConsumer operation) {
		Objects.requireNonNull(operation, "operation");

		final int size = this.size;
		final int[] data = this.data;

		for (int i = 0; i < size; ++i) {
			operation.accept(data[i]);
		}
	}

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * @param index        of the item to return
	 * @param defaultValue to return if the provided index is outside bounds.
	 * @return item at the specified position in bag
	 */
	public int get(final int index, final int defaultValue) {
		if (isInSize(index)) {
			return data[index];
		}

		return defaultValue;
	}

	/**
	 * Returns the item at the specified position in Bag.
	 * 
	 * <p>
	 * <b>UNSAFE: Avoids doing any bounds check.</b>
	 * </p>
	 * 
	 * @param index of the item to return
	 * @return item at the specified position in bag
	 */
	public int getUnsafe(final int index) {
		return data[index];
	}

	/**
	 * Checks if the index is within the capacity of the Bag (ie, if its bigger or
	 * equal than 0 and less than the length of the backing array).
	 * 
	 * @param index that needs to be checked.
	 * @return <code>true</code> if the index is within the bounds of the Bag,
	 *         <code>false</code> otherwise.
	 */
	protected boolean isInBounds(final int index) {
		return (index > -1 && index < data.length);
	}

	/**
	 * Checks if the index is within the size of the Bag (ie, if its bigger or equal
	 * than 0 and less than the size of the bag).
	 * 
	 * @param index that needs to be checked.
	 * @return <code>true</code> if the index is within the size of the Bag,
	 *         <code>false</code> otherwise.
	 */
	protected boolean isInSize(final int index) {
		return (index > -1 && index < size);
	}

	/**
	 * Creates an array with the elements of this bag.
	 * 
	 * @return an array containing the elements of this bag.
	 */
	public final int[] toArray() {
		return Arrays.copyOf(this.data, this.size);
	}

	private IntStream stream(final boolean parallel) {
		final Spliterator.OfInt split = Spliterators.spliterator(data, Spliterator.IMMUTABLE);
		return StreamSupport.intStream(split, parallel).limit(size);
	}

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE sequential Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	public IntStream stream() {
		return stream(false);
	}

	/**
	 * Provides a SIZED, SUBSIZED and IMMUTABLE parallel Stream over this bag.
	 *
	 * @return Stream representing this bag.
	 */
	public IntStream parallelStream() {
		return stream(true);
	}

	/**
	 * This value is fetched from
	 * {@link com.artemis.DAConstants#BAG_DEFAULT_CAPACITY}
	 */
	public static final int DEFAULT_CAPACITY = DAConstants.BAG_DEFAULT_CAPACITY;
	/**
	 * This value is fetched from
	 * {@link com.artemis.DAConstants#BAG_GROW_RATE_THRESHOLD}
	 */
	public static final int GROW_RATE_THRESHOLD = DAConstants.BAG_GROW_RATE_THRESHOLD;

	/** Non-configurable value. */
	public static final int MINIMUM_WORKING_CAPACITY = 4;

	protected static final int nextCapacity(final int dataLength) {
		if (dataLength < GROW_RATE_THRESHOLD) {
			// Exponential 2 growth.
			return (dataLength << 1);
		}

		return dataLength + (dataLength >> 1);
	}

	/**
	 * It finds the next capacity according to the grow strategy that could contain
	 * the supplied index.
	 * 
	 * @param index       that needs to be contained.
	 * @param arrayLength of the current backing array.
	 * @return proper capacity given by the grow strategy.
	 */
	protected static final int getCapacityFor(final int index, final int arrayLength) {
		int newSize = arrayLength;

		while (index >= newSize) {
			newSize = nextCapacity(newSize);
		}

		return newSize;
	}

}
