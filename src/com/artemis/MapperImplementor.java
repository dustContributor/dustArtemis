package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.artemis.utils.Bag;

final class MapperImplementor
{
	/** Non-instantiable class */
	private MapperImplementor ()
	{
		// Empty.
	}

	@SuppressWarnings("unchecked")
	static final void initFor ( final Bag<? extends EntitySystem> systems, final World world )
	{
		final EntitySystem[] arSystems = systems.data();
		final int sysLen = systems.size();
		
		final Bag<Field> fieldBag = new Bag<>( Field.class, 32 );
		
		for ( int s = 0; s < sysLen; ++s )
		{
			final EntitySystem trg = arSystems[s];
			{
				final Field[] fields = trg.getClass().getDeclaredFields();
				final int fieldsLen = fields.length;

				for ( int i = 0; i < fieldsLen; ++i )
				{
					final Field field = fields[i];

					if ( field.getType() == ComponentMapper.class )
					{
						fieldBag.add( field );
					}
				}
			}
			
			final Field[] fields = fieldBag.data();
			final int fieldsLen = fieldBag.size();
			
			for ( int f = 0; f < fieldsLen; ++f )
			{
				final Field field = fields[f];
				
				final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
				final Class<? extends Component> componentType = (Class<? extends Component>) genericType.getActualTypeArguments()[0];
				final ComponentMapper<? extends Component> mapper = new ComponentMapper<>( componentType, world );

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
			
			fieldBag.clear();
		}
	}
}
