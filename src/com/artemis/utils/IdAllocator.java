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
	private final Bag<int[]> freeRanges;

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
		this.freeRanges = new Bag<>( int[].class, 16 );
		this.freeRanges.addUnsafe( new int[] { rangeStart, rangeEnd } );
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
		int[] fRange = freeRanges.getUnsafe( 0 );
		// Free range's start will be new ID.
		int id = fRange[0];
		// New start will be that ID plus one.
		int nStart = id + 1;
		fRange[0] = nStart;

		// If free range's end was reached,
		// remove it from the list.
		if ( nStart >= fRange[1] )
		{
			freeRanges.eraseUnsafe( 0 );
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
	public final void free ( final int id )
	{
		/*
		 * We're going to assume you're not freeing an ID thats outside of this
		 * IdAllocator's initial range.
		 */
		final int frSize = freeRanges.size();
		final int[][] fRanges = freeRanges.data();

		for ( int i = 0; i < frSize; ++i )
		{
			int[] fRange = fRanges[i];
			int frStart = fRange[0];
			// If ID is to the left.
			if ( frStart > id )
			{
				// If ID is at free range start.
				if ( frStart == (id + 1) )
				{
					// Set new free range start as ID.
					fRange[0] = id;
					// If it isn't the first range, update range on the left.
					if ( i != 0 )
					{
						// Grab range on the left.
						int[] lfRange = fRanges[i - 1];
						int lfrEnd = lfRange[1];
						// If range on the left ends at ID.
						if ( lfrEnd == id )
						{
							// Extend left range limit to right range limit.
							lfRange[1] = fRange[1];
							// Remove right range.
							freeRanges.eraseUnsafe( i );
						}
					}
					return;
				}
				// If ID isn't next to free range.
				if ( i != 0 )
				{
					// Grab range to the left.
					int[] lfRange = fRanges[i - 1];
					// If left range ends at ID.
					if ( lfRange[1] == id )
					{
						// Update left range's end and return.
						++lfRange[1];
						return;
					}
				}
				/*
				 * No adjacent free range was found for given ID, make a new
				 * one.
				 */
				freeRanges.ensureCapacity( frSize + 1 );
				freeRanges.insertUnsafe( i, new int[] { id, id + 1 } );
				return;
			}
		}

	}

	@Override
	public String toString ()
	{
		StringBuilder sb = new StringBuilder( 512 );
		sb.append( "ID ALLOCATOR: " );
		sb.append( super.toString() );
		sb.append( System.lineSeparator() );
		sb.append( "FREE RANGES: " );

		for ( int i = 0; i < freeRanges.size(); ++i )
		{
			final int[] range = freeRanges.getUnsafe( i );
			sb.append( System.lineSeparator() );
			sb.append( "Range N_" ).append( i );
			sb.append( " Start: " ).append( range[0] );
			sb.append( " _ End: " ).append( range[1] );
		}

		return sb.toString();
	}

}
