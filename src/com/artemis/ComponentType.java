package com.artemis;

import java.util.IdentityHashMap;

public class ComponentType
{
	private static int INDEX = 0;

	private final int index;
	private final Class<? extends Component> type;

	private ComponentType ( Class<? extends Component> type )
	{
		index = INDEX++;
		this.type = type;
	}

	public int getIndex ()
	{
		return index;
	}

	@Override
	public String toString ()
	{
		return "ComponentType[" + type.getSimpleName() + "] (" + index + ")";
	}

	private static final IdentityHashMap<Class<? extends Component>, ComponentType> componentTypes = new IdentityHashMap<>();

	public static final ComponentType getTypeFor ( final Class<? extends Component> c )
	{
		ComponentType type = componentTypes.get( c );

		if ( type == null )
		{
			type = new ComponentType( c );
			componentTypes.put( c, type );
		}

		return type;
	}

	public static final int getIndexFor ( final Class<? extends Component> c )
	{
		return getTypeFor( c ).getIndex();
	}
}
