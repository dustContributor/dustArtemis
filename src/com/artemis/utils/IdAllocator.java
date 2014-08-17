package com.artemis.utils;

import static java.lang.Integer.MAX_VALUE;

import java.util.ArrayList;

public final class IdAllocator
{
	private final ArrayList<IdRange> freeRanges;
	
	public IdAllocator ()
	{
		this.freeRanges = new ArrayList<>( 16 );
		freeRanges.add(  new IdRange( 0, MAX_VALUE ) );
 	}
	
	public final int alloc ()
	{
		IdRange first = freeRanges.get( 0 );
		final int id = first.start;
		first.start = id + 1;
		
		if ( first.start >= first.end )
		{
			freeRanges.remove( 0 );
		}
		
		return id;
	}
	
	public final void free ( int id )
	{
		
	}
	
	private static final class IdRange
	{
		int start;
		int end;
		
		IdRange ()
		{
			// Default.
		}
		
		IdRange ( int start, int end )
		{
			this.start = start;
			this.end = end;
		}
		
		final int capacity ()
		{
			return end - start;
		}
	}
}
