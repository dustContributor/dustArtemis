package com.artemis.utils;

import static java.lang.Integer.MAX_VALUE;

import java.util.ArrayList;

/**
 * Integer id allocator. It manages integer IDs in a given range. Retrieve IDs
 * with {@link #alloc()} method, and free them with the {@link #free(int)}
 * method.
 * 
 * @author dustContributor
 */
public final class IdAllocator
{
	/** List of ranges of free ID numbers. */
	private final ArrayList<FreeRange> freeRanges;

	/**
	 * Creates an IdAllocator that will manage IDs in the interval [0,
	 * Integer.MAX_VALUE ).
	 */
	public IdAllocator ()
	{
		this( 0, MAX_VALUE );
	}

	/**
	 * Creates an IdAllocator that will manage IDs in the range of the provided
	 * parameters.
	 * 
	 * @param rangeStart
	 *            start of the range of IDs this allocator will provide.
	 *            Inclusive.
	 * @param rangeEnd
	 *            start of the range of IDs this allocator will provide.
	 *            Exclusive.
	 */
	public IdAllocator ( int rangeStart, int rangeEnd )
	{
		this.freeRanges = new ArrayList<>( 16 );
		this.freeRanges.add( new FreeRange( rangeStart, rangeEnd ) );
	}

	/**
	 * Returns an unused integer ID. Doesn't checks if you ran out of IDs given
	 * the initial range of the allocator.
	 * 
	 * @return an unused integer ID.
	 */
	public final int alloc ()
	{
		// Get lowest free range.
		FreeRange fRange = freeRanges.get( 0 );
		// Free range's start will be new ID.
		int id = fRange.start;
		// New start will be that ID plus one.
		int nStart = id + 1;
		fRange.start = nStart;

		// If free range's end was reached,
		// remove it from the list.
		if ( nStart >= fRange.end )
		{
			freeRanges.remove( 0 );
		}
		/*
		 * We're going to assume that you didn't ran out of IDs (ie, that
		 * freeRanges isn't empty) for the sake of simplicity.
		 */
		return id;
	}

	/**
	 * Indicates that an ID isn't used anymore to this allocator.
	 * 
	 * @param id
	 *            to be freed.
	 */
	public final void free ( int id )
	{
		/*
		 * We're going to assume you're not freeing an ID thats outside of this
		 * IdAllocator's initial range.
		 */
		int frSize = freeRanges.size();

		for ( int i = 0; i < frSize; ++i )
		{
			FreeRange fRange = freeRanges.get( i );
			int frStart = fRange.start;
			// If ID is to the left.
			if ( frStart > id )
			{
				// If ID is at free range start.
				if ( frStart == (id + 1) )
				{
					// Set new free range start as ID.
					fRange.start = id;
					// If it isn't the first range, update range on the left.
					if ( i != 0 )
					{
						// Grab range on the left.
						FreeRange lfRange = freeRanges.get( i - 1 );
						int lfrEnd = lfRange.end;
						// If range on the left ends at ID.
						if ( lfrEnd == id )
						{
							// Extend left range limit to right range limit.
							lfRange.end = fRange.end;
							// Remove right range.
							freeRanges.remove( i );
						}
					}
					return;
				}
				// If ID isn't next to free range.
				if ( i != 0 )
				{
					// Grab range to the left.
					FreeRange lfRange = freeRanges.get( i - 1 );
					// If left range ends at ID.
					if ( lfRange.end == id )
					{
						// Update left range's end and return.
						lfRange.end = id + 1;
						return;
					}
				}
				// No adjacent free range was found for given ID, make a new
				// one.
				freeRanges.add( i, new FreeRange( id, id + 1 ) );
				return;
			}
		}

	}

	private static final class FreeRange
	{
		/** Start of the free ID range. Inclusive. */
		int start;
		/** End of the free ID range. Exclusive. */
		int end;

		FreeRange ( int start, int end )
		{
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString ()
		{
			return super.toString() + System.lineSeparator() + "START: " + start + " - END: " + end;
		}
	}
}
