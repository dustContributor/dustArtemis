package com.artemis;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread safe class indexer, used for getting unique indices per subclass for
 * classes that extend whatever superclass you specify.
 * 
 * @author dustContributor
 *
 */
public class ClassIndexer
{
	private static ConcurrentHashMap<Class<?>, Integer> indexMap = 
			new ConcurrentHashMap<>( 64, 0.75f, Runtime.getRuntime().availableProcessors() * 2 );
	private static ConcurrentHashMap<Class<?>, IntCounter> counterMap = 
			new ConcurrentHashMap<>( 64, 0.75f, Runtime.getRuntime().availableProcessors() * 2 );

	public static <T> int getIndexFor ( Class<? extends T> type, Class<T> superType )
	{
		Integer i = indexMap.get( type );

		if ( i != null )
		{
			return i.intValue();
		}

		i = Integer.valueOf( getNextIndex( superType ) );
		indexMap.put( type, i );
		return i.intValue();
	}

	private static int getNextIndex ( Class<?> type )
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

	private static class IntCounter
	{
		private int count;

		public IntCounter ()
		{
			count = 0;
		}

		public int getAndIncrement ()
		{
			++count;
			return count - 1;
		}
	}

}
