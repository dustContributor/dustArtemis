package com.artemis.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe class indexer, used for getting unique indices per subclass for
 * classes that extend whatever superclass you specify.
 * 
 * @author dustContributor
 *
 */
public final class ClassIndexer
{
	/** Non-instantiable class */
	private ClassIndexer ()
	{
		// Empty.
	}

	/** Map of resolved derived class indices. Stores immutable index. */
	private static final ConcurrentHashMap<Class<?>, Integer> RESOLVED;
	/** Map of derived class counters. Stores mutable integer counter. */
	private static final ConcurrentHashMap<Class<?>, int[]> COUNTERS;

	static
	{
		final int concurrency = Runtime.getRuntime().availableProcessors();

		RESOLVED = new ConcurrentHashMap<>( 32, 0.75f, concurrency );
		COUNTERS = new ConcurrentHashMap<>( 16, 0.75f, concurrency );
	}

	public static final <T> int getIndexFor ( final Class<? extends T> type, final Class<T> superType )
	{
		Integer i = RESOLVED.get( type );

		if ( i == null )
		{
			i = fallbackIndexOf( type, superType );
		}

		return i.intValue();
	}

	private static final synchronized <T> Integer fallbackIndexOf (
			final Class<? extends T> type,
			final Class<T> superType )
	{
		Integer i = RESOLVED.get( type );

		if ( i == null )
		{
			// Fetch index and store the result.
			final int index = nextIndex( superType );
			RESOLVED.put( type, i = Integer.valueOf( index ) );
		}

		return i;
	}

	private static final int nextIndex ( final Class<?> type )
	{
		int[] i = COUNTERS.get( type );

		if ( i == null )
		{
			// Create counter and store it.
			COUNTERS.put( type, i = new int[1] );
		}

		return i[0]++;
	}

}
