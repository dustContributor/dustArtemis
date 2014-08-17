package com.artemis.utils;

import static java.lang.Integer.MAX_VALUE;

import java.util.ArrayList;

public final class IdAllocator
{
	private final ArrayList<FreeRange> freeRanges;
	
	public IdAllocator ()
	{
		this.freeRanges = new ArrayList<>( 16 );
		this.freeRanges.add(  new FreeRange( 0, MAX_VALUE ) );
 	}
	
	public final int alloc ()
	{
		FreeRange range = freeRanges.get( 0 );
		
		int id = range.start;
		int nStart = id + 1;
		range.start = nStart;
		
		if ( nStart >= range.end )
		{
			freeRanges.remove( 0 );
		}
		
		return id;
	}
	
	public final void free ( int id )
	{
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
		int start;
		int end;

		FreeRange ( int start, int end )
		{
			this.start = start;
			this.end = end;
		}
		
		@Override
		public String toString ()
		{
			return super.toString() +
					System.lineSeparator() +
					"START: " + start + " - END: " + end;
		}
	}
}
