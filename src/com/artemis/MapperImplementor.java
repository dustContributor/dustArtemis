package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

final class MapperImplementor
{
	/** Non-instantiable class */
	private MapperImplementor ()
	{
		// Empty.
	}

	@SuppressWarnings("unchecked")
	static final void initFor ( final Object trg, final World world )
	{
		final Field[] fields = trg.getClass().getDeclaredFields();
		final int size = fields.length;
		
		for ( int i = 0; i < size; ++i )
		{
			final Field field = fields[i];
			
			if ( field.getType() == ComponentMapper.class )
			{
				final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
				final Class<? extends Component> componentType = (Class<? extends Component>) genericType.getActualTypeArguments()[0];
				final ComponentMapper<? extends Component> mapper = ComponentMapper.getFor( componentType, world );

				field.setAccessible( true );
				
				try
				{
					field.set( trg, mapper );
				}
				catch ( IllegalArgumentException | IllegalAccessException e )
				{
					throw new RuntimeException( "Error while setting component mappers", e );
				}
				finally
				{
					field.setAccessible( false );
				}
			}
		}
	}
}
