package com.artemis;

import java.util.HashMap;

/**
 * Thread safe class indexer, used for getting unique indices per subclass for
 * classes that extend whatever superclass you specify.
 * 
 * @author dustContributor
 *
 */
public final class ClassIndexer
{
	private static final HashMap<Class<?>, Integer> resolvedMap = new HashMap<>();
	private static final HashMap<Class<?>, IntCounter> counterMap = new HashMap<>();

	public static final <T> int getIndexFor ( Class<? extends T> type, Class<T> superType )
	{
		Integer i = resolvedMap.get( type );

		if ( i != null )
		{
			return i.intValue();
		}
		
		return fallbackIndexOf( type, superType );

	}
	
	public static final synchronized <T> int fallbackIndexOf ( Class<? extends T> type, Class<T> superType )
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

	private static final int nextIndex ( Class<?> type )
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
