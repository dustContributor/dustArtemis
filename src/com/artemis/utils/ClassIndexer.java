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
	private static final ConcurrentHashMap<Class<?>, Integer> resolvedMap;
	private static final ConcurrentHashMap<Class<?>, IntCounter> counterMap;
	
	static
	{
		final int concurrency = Runtime.getRuntime().availableProcessors() * 2;
		
		resolvedMap = new ConcurrentHashMap<>( 32, 0.75f, concurrency );
		counterMap = new ConcurrentHashMap<>( 32, 0.75f, concurrency );
	}

	public static final <T> int getIndexFor ( final Class<? extends T> type, final Class<T> superType )
	{
		Integer i = resolvedMap.get( type );

		if ( i != null )
		{
			return i.intValue();
		}
		
		return fallbackIndexOf( type, superType );

	}
	
	private static final synchronized <T> int fallbackIndexOf ( final Class<? extends T> type, final Class<T> superType )
	{
		Integer i = resolvedMap.get( type );
		
		if ( i != null )
		{
			return i.intValue();
		}
		
		i = Integer.valueOf( nextIndex( superType ) );
		
		resolvedMap.put( type, i );
		
		return i.intValue();
	}

	private static final int nextIndex ( final Class<?> type )
	{
		IntCounter i = counterMap.get( type );

		if ( i != null )
		{
			return i.getAndIncrement();
		}

		i = new IntCounter();
		counterMap.put( type, i );

		return i.getAndIncrement();
	}

	private static final class IntCounter
	{
		private int count;

		public IntCounter ()
		{
			count = 0;
		}

		public final int getAndIncrement ()
		{
			++count;
			return count - 1;
		}
	}

}