package com.artemis.utils;

import static java.lang.Integer.MAX_VALUE;

import java.util.Arrays;

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
	private int[] freeRanges;
	private int freeRangesSize;

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
		this.freeRanges = new int[256];
		this.freeRanges[0] = rangeStart;
		this.freeRanges[1] = rangeEnd;
		this.freeRangesSize = 2;
	}

	/**
	 * Returns an unused integer ID. Doesn't checks if you ran out of IDs given
	 * the initial range of the allocator.
	 * 
	 * @return an unused integer ID.
	 */
	public final int alloc ()
	{
		final int[] fRanges = freeRanges;
		// Free range's start will be new ID.
		final int id = fRanges[0]++;

		// If free range's end was reached,
		// remove it from the list.
		if ( fRanges[0] >= fRanges[1] )
		{
			final int newSize = freeRangesSize - 2;
			// Shift left overwriting range.
			System.arraycopy( fRanges, 2, fRanges, 0, newSize );
			freeRangesSize = newSize;
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
		final int frSize = freeRangesSize;
		final int[] fRanges = freeRanges;

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
							final int newSize = frSize - 2;
							System.arraycopy( fRanges, i + 2, fRanges, i, newSize - i );
							freeRangesSize = newSize;
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
				final int newSize = frSize + 2;
				// Ensure capacity.
				if ( newSize >= fRanges.length )
				{
					freeRanges = Arrays.copyOf( fRanges, fRanges.length << 1 );
				}
				// Set new size.
				freeRangesSize = newSize;
				// Fetch possibly reallocated array.
				final int[] nfRanges = freeRanges;
				// Shift to the right.
				System.arraycopy( nfRanges, i, nfRanges, i + 2, newSize - i );
				// Store free range.
				nfRanges[i] = id;
				nfRanges[i + 1] = id + 1;
				return;
			}
		}

	}

	@Override
	public final String toString ()
	{
		final StringBuilder sb = new StringBuilder( 512 );
		sb.append( "ID ALLOCATOR: " );
		sb.append( super.toString() );
		sb.append( System.lineSeparator() );
		sb.append( "BACKING ARRAY SIZE: " );
		sb.append( freeRanges.length );
		sb.append( System.lineSeparator() );
		sb.append( " - FREE RANGES LIST - " );

		final int frSize = freeRangesSize;
		final int[] fRanges = freeRanges;

		for ( int i = 0; i < frSize; i += 2 )
		{
			sb.append( System.lineSeparator() );
			sb.append( "RANGE: " );
			sb.append( i / 2 );
			sb.append( " - START: " );
			sb.append( fRanges[i] );
			sb.append( " - END: " );
			sb.append( fRanges[i + 1] );
		}

		return sb.toString();
	}

}
