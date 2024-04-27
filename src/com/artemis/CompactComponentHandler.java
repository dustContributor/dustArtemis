package com.artemis;

import java.util.Arrays;

import org.apache.lucene.util.OpenBitSet;

/**
 * This component handler doesn't allows for gaps in between components, they're
 * stored contiguously in a single array, and a separate "used index ranges"
 * array is kept to find each element by their logical index.
 *
 * @author dustContributor
 *
 * @param <T> type of component this handler will manage.
 */
public final class CompactComponentHandler<T extends Component> extends ComponentHandler<T> {
	private long[] ranges;
	private int rangesSize;

	protected CompactComponentHandler(final Class<T> type, final OpenBitSet componentBits, final int wordsPerEntity,
			final int index,
			final int capacity) {
		super(type, componentBits, wordsPerEntity, index, capacity);
		this.ranges = new long[128];
	}

	@Override
	protected final void set(final int id, final T component) {
		final int curri = searchRangeIndex(ranges, rangesSize, id);
		// Check if we could just insert a range at zero, or expand the first range.
		if (curri == 0) {
			// We already know if we're here is because it will be the first item.
			insertItem(0, component);

			final long currRange = ranges[0];
			final int currStart = Range.start(currRange);

			if ((currStart - 1) == id) {
				// Just update the range's start.
				ranges[0] = Range.start(id, currRange);
			} else {
				// Insert new range at the start of the array.
				insertRange(0, Range.of(id, id + 1));
			}

			updateOffsets(1);
			return;
		}

		final int lefti = curri - 1;
		final long leftRange = ranges[lefti];
		final int leftOffset = Range.offset(leftRange);
		final int leftStart = Range.start(leftRange);
		final int leftEnd = Range.end(leftRange);

		// Check if its just overwriting an existing value.
		if (id >= leftStart && id < leftEnd) {
			final int reali = id - leftStart + leftOffset;
			data[reali] = component;
			return;
		}

		// Check if we could just expand or merge to the left.
		if (id == leftEnd) {
			final int nLeftEnd = leftEnd + 1;
			final int reali = id - leftStart + leftOffset;

			insertItem(reali, component);

			// Expand left range's end.
			ranges[lefti] = Range.end(nLeftEnd, leftRange);

			if (curri >= rangesSize) {
				// Nothing to merge, return.
				return;
			}

			final long currRange = ranges[curri];
			final int currStart = Range.start(currRange);

			if (nLeftEnd == currStart) {
				// Merge to the left and update the range.
				eraseRange(lefti);
				ranges[lefti] = Range.of(leftStart, Range.end(currRange));
			}
			// Recalc all counters from last index and onwards.
			updateOffsets(lefti);
			return;
		}

		// If we got here, we need to insert a range to the right.

		// Compute item index by off-setting by current item count.
		final int leftSize = leftEnd - leftStart;
		final int currOffset = leftOffset + leftSize;
		// Insert item at that index, and insert new range.
		insertItem(currOffset, component);
		insertRange(curri, Range.of(id, id + 1));
		// Recalc all counters from current index and onwards.
		updateOffsets(curri);
		return;
	}

	@Override
	public final T get(final int id) {
		final int curri = searchRangeIndex(ranges, rangesSize, id);
		final long leftRange = ranges[curri - 1];
		return this.data[id - Range.start(leftRange) + Range.offset(leftRange)];
	}

	@Override
	protected final void delete(final int id) {
		final int curri = searchRangeIndex(ranges, rangesSize, id);
		// Clamp in case curri is first range.
		final int lefti = Math.max(0, curri - 1);
		final long leftRange = ranges[lefti];
		final int leftOffset = Range.offset(leftRange);
		final int leftStart = Range.start(leftRange);
		final int leftEnd = Range.end(leftRange);
		// Erase first before changing the ranges.
		eraseItem(id - leftStart + leftOffset);
		// Fix up the ranges.
		if (id == leftStart) {
			final int newStart = id + 1;
			if (leftEnd - newStart < 1) {
				// Range can't hold anything else, remove.
				eraseRange(lefti);
			} else {
				// Otherwise just adjust the start.
				ranges[lefti] = Range.start(newStart, leftRange);
			}
		} else {
			// Id is in the middle or at the end, we might need to split.
			ranges[lefti] = Range.end(id, leftRange);
			final int newStart = id + 1;
			// If there is any space left to the right, add the range.
			if (newStart < leftEnd) {
				insertRange(curri, Range.of(newStart, leftEnd));
			}
		}
		// Update all the range item counters to the right.
		updateOffsets(lefti);
	}

	/**
	 * @return amount of stored components.
	 */
	public final int size() {
		if (rangesSize < 1) {
			return 0;
		}

		final long range = ranges[rangesSize - 1];
		return Range.offset(range) + Range.size(range);
	}

	@Override
	public final String toString() {
		return rangesToString();
	}

	/**
	 * @return the item count in between the used ranges.
	 */
	public final int gapCount() {
		final long[] r = ranges;
		final int rs = rangesSize;

		int counter = 0;
		for (int i = 1; i < rs; ++i) {
			counter += (Range.start(r[i]) - Range.end(r[i - 1]));
		}
		return counter;
	}

	/**
	 * @return ranges stored.
	 */
	public final int rangeCount() {
		return rangesSize;
	}

	/**
	 * @return index domain of the ranges stored.
	 */
	public final int rangeDomain() {
		if (rangesSize < 1) {
			return 0;
		}

		return Range.end(ranges[rangesSize - 1]) - Range.start(ranges[0]);
	}

	/**
	 * @return the logical index this handler starts at.
	 */
	public final int rangeStart() {
		if (rangesSize < 1) {
			return 0;
		}

		return Range.start(ranges[0]);
	}

	/**
	 * @return the logical index this handler ends at.
	 */
	public final int rangeEnd() {
		if (rangesSize < 1) {
			return 0;
		}

		return Range.end(ranges[rangesSize - 1]);
	}

	private final void insertItem(final int index, final T item) {
		final int size = size();
		resize(size + 1);
		// Make space for the new item and write.
		System.arraycopy(data, index, data, index + 1, size - index);
		data[index] = item;
	}

	private final void insertRange(final int index, final long range) {
		final int size = rangesSize;
		final int newSize = size + 1;
		final long[] ranges = fixRangeCapacity(newSize);
		// Make space for the new item and write.
		System.arraycopy(ranges, index, ranges, index + 1, size - index);
		ranges[index] = range;
		rangesSize = newSize;
	}

	private final void eraseRange(final int index) {
		System.arraycopy(ranges, index + 1, ranges, index, rangesSize - index - 1);
		--rangesSize;
	}

	private final T eraseItem(final int index) {
		final T item = data[index];
		final int lasti = size() - 1;
		System.arraycopy(data, index + 1, data, index, lasti - index);
		data[lasti] = null;
		return item;
	}

	private final long[] fixRangeCapacity(final int expected) {
		return expected < ranges.length ? ranges
				: (ranges = Arrays.copyOf(ranges, ranges.length << 1));
	}

	private final void updateOffsets(final int index) {
		Range.updateOffsets(index, ranges, rangesSize);
	}

	private static final int searchRangeIndex(final long[] data, final int size, final int value) {
		// Range index.
		int rangei = 0;
		int limit = size;

		while (rangei < limit) {
			final int mid = (rangei + limit) >>> 1;

			final int start = Range.start(data[mid]);
			/*
			 * Condition is a bit different, we're looking for the free range start that is
			 * strictly less than the id.
			 */
			if (value >= start) {
				rangei = mid + 1;
			} else {
				limit = mid;
			}
		}
		// Return found index.
		return rangei;
	}

	/**
	 * @return a string representation of the stored ranges.
	 */
	private final String rangesToString() {
		return rangesToString(ranges, rangesSize);
	}

	/**
	 * Used as name-space for the methods to pack and unpack ranges.
	 */
	@SuppressWarnings("unused")
	private static final class Range {
		// Bits dedicated to each field. Can't be bigger than 32.
		private static final int BTS_OFFSET = 20;
		private static final int BTS_START = 20;
		private static final int BTS_END = 20;
		private static final int BTS_UNUSED = 4;
		// Shift to the start of each field.
		private static final int SHF_OFFSET = 0;
		private static final int SHF_START = SHF_OFFSET + BTS_OFFSET;
		private static final int SHF_END = SHF_START + BTS_START;
		private static final int SHF_UNUSED = SHF_END + BTS_END;
		// Masks to retrieve each field.
		private static final int MSK_OFFSET = ((1 << BTS_OFFSET) - 1);
		private static final int MSK_START = ((1 << BTS_START) - 1);
		private static final int MSK_END = ((1 << BTS_END) - 1);
		private static final int MSK_UNUSED = ((1 << BTS_UNUSED) - 1);
		// Masks to overwrite each field in an existing value.
		private static final long MSK_PCK_OFFSET = ((long) MSK_OFFSET) << SHF_OFFSET;
		private static final long MSK_PCK_START = ((long) MSK_START) << SHF_START;
		private static final long MSK_PCK_END = ((long) MSK_END) << SHF_END;
		private static final long MSK_PCK_UNUSED = ((long) MSK_UNUSED) << SHF_UNUSED;

		static final void updateOffsets(final int start, final long[] ranges, final int size) {
			// Init counter with previous range's count.
			int counter = start < 1 ? 0 : Range.offset(ranges[start - 1]) + Range.size(ranges[start - 1]);

			for (int i = start; i < size; ++i) {
				final long range = ranges[i];
				ranges[i] = Range.offset(counter, range);
				// Update the counter and store in the ranges array.
				counter = counter + Range.size(range);
			}
		}

		static final long of(final int start, final int end) {
			final long ls = ((long) start) << SHF_START;
			final long le = ((long) end) << SHF_END;
			return ls | le;
		}

		static final long of(final int offset, final int start, final int end) {
			final long lc = ((long) offset) << SHF_OFFSET;
			final long ls = ((long) start) << SHF_START;
			final long le = ((long) end) << SHF_END;
			return lc | ls | le;
		}

		static final long offset(final int offset, final long dest) {
			final long erased = dest & ~MSK_PCK_OFFSET;
			final long placed = ((long) offset) << SHF_OFFSET;
			return erased | placed;
		}

		static final long start(final int start, final long dest) {
			final long erased = dest & ~MSK_PCK_START;
			final long placed = ((long) start) << SHF_START;
			return erased | placed;
		}

		static final long end(final int end, final long dest) {
			final long erased = dest & ~MSK_PCK_END;
			final long placed = ((long) end) << SHF_END;
			return erased | placed;
		}

		static final int offset(final long packed) {
			return ((int) (packed >>> SHF_OFFSET)) & MSK_OFFSET;
		}

		static final int start(final long packed) {
			return ((int) (packed >>> SHF_START)) & MSK_START;
		}

		static final int end(final long packed) {
			return ((int) (packed >>> SHF_END)) & MSK_END;
		}

		private static final int unpack(final long packed, final int shift, final int mask) {
			return ((int) (packed >>> shift)) & mask;
		}

		static final int size(final long packed) {
			return end(packed) - start(packed);
		}
	}

	private static final String rangeToString(final long packed) {
		return "{count: " + Range.offset(packed)
				+ ", start: " + Range.start(packed)
				+ ", end: " + Range.end(packed) + "}";
	}

	private static final String rangesToString(final long[] ranges, final int size) {
		String res = "";
		for (int i = 0; i < size; ++i) {
			res += rangeToString(ranges[i]);

			if ((i + 1) == size) {
				break;
			}

			res += ", ";
		}
		return "[" + res + "]";
	}

	@Override
	protected final void ensureCapacity(final int id) {
		// Nothing to do since it ensures capacity by default on add operations.
		return;
	}
}
