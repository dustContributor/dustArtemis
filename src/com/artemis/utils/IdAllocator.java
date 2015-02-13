package com.artemis.utils;

import static java.lang.Integer.MAX_VALUE;

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
	private final IntBag freeRanges;

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
	 * @param rangeStart start of the range of IDs this allocator will provide.
	 *            Inclusive.
	 * @param rangeEnd start of the range of IDs this allocator will provide.
	 *            Exclusive.
	 */
	public IdAllocator ( int rangeStart, int rangeEnd )
	{
		this.freeRanges = new IntBag( 16 );
		insertFreeRange( 0, rangeStart, rangeEnd );
	}

	/**
	 * Returns an unused integer ID. Doesn't checks if you ran out of IDs given
	 * the initial range of the allocator.
	 * 
	 * @return an unused integer ID.
	 */
	public final int alloc ()
	{
		final int[] fRanges = freeRanges.data;
		// Free range's start will be new ID.
		final int id = fRanges[0]++;

		// If free range's end was reached,
		// remove it from the list.
		if ( fRanges[0] >= fRanges[1] )
		{
			freeRanges.eraseRangeUnsafe( 0, 2 );
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
	 * @param id to be freed.
	 */
	public final void free ( int id )
	{
		/*
		 * We're going to assume you're not freeing an ID thats outside of this
		 * IdAllocator's initial range.
		 */
		final int frSize = freeRanges.size();
		final int[] fRanges = freeRanges.data();

		for ( int i = 0; i < frSize; i += 2 )
		{
			int frStart = fRanges[i];
			// If ID is to the left.
			if ( frStart > id )
			{
				// If ID is at free range start.
				if ( frStart == (id + 1) )
				{
					// Set new free range start as ID.
					fRanges[i] = id;
					// If it isn't the first range, update range on the left.
					if ( i != 0 )
					{
						// If range on the left ends at ID.
						if ( fRanges[i - 1] == id )
						{
							// Extend left range limit to right range limit.
							fRanges[i - 1] = fRanges[i + 1];
							// Remove right range.
							freeRanges.eraseRangeUnsafe( i, 2 );
						}
					}
					return;
				}
				// If ID isn't next to free range.
				if ( i != 0 )
				{
					// If left range ends at ID.
					if ( fRanges[i - 1] == id )
					{
						// Extend left range's end and return.
						fRanges[i - 1] = id + 1;
						return;
					}
				}
				/*
				 * No adjacent free range was found for given ID, make a new
				 * one.
				 */
				insertFreeRange( i, id, id + 1 );
				return;
			}
		}

	}

	private final void insertFreeRange ( final int index, final int start, final int end )
	{
		final int newSize = freeRanges.size() + 2;
		freeRanges.ensureCapacity( newSize );
		freeRanges.setSize( newSize );
		final int[] fRanges = freeRanges.data();
		// Shift to the right.
		System.arraycopy( fRanges, index, fRanges, index + 2, newSize - index );
		// Store free range.
		fRanges[index] = start;
		fRanges[index + 1] = end;
	}

	@Override
	public final String toString ()
	{
		StringBuilder sb = new StringBuilder( 512 );
		sb.append( "ID ALLOCATOR: " );
		sb.append( super.toString() );
		sb.append( System.lineSeparator() );
		sb.append( "FREE RANGES: " );

		int[] fRanges = freeRanges.data();

		for ( int i = 0; i < freeRanges.size(); ++i )
		{
			sb.append( System.lineSeparator() );
			sb.append( "START: " + fRanges[i] + " - END: " + fRanges[i + 1] );
		}

		return sb.toString();
	}

}
