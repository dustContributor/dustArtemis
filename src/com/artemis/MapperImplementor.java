package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.artemis.utils.Bag;

/**
 * Class with static method for initializing ComponentMappers.
 * 
 * The init method of original Artemis got reworked this way so it operates on
 * an entire collection of systems.
 * 
 * It no longer uses annotations for defining what should be initialized. It
 * just plain initializes any ComponentMapper it comes across in each system.
 * 
 * Looking for annotations was quite a bit slower, so changing it for direct
 * type comparison, making it iterate over all the systems inside the method and
 * making it so it collects the fields before initializing them, it made the
 * whole initialization process faster.
 * 
 * @author dustContributor
 */
final class MapperImplementor
{
	/** Non-instantiable class */
	private MapperImplementor ()
	{
		// Empty.
	}

	/**
	 * For each of the EntitySystems provided in the Bag, they will get a proper
	 * instance for each of their ComponentMapper fields.
	 * 
	 * @param systems that you need initialized.
	 * @param world where the components come from.
	 */
	@SuppressWarnings("unchecked")
	static final void initFor ( final Bag<? extends EntitySystem> systems, final World world )
	{
		final EntitySystem[] arSystems = systems.data();
		final int sysLen = systems.size();
		
		// Bag big enough so it shouldn't grow when adding fields to it.
		final Bag<Field> fieldBag = new Bag<>( Field.class, 32 );
		
		// For each of the EntitySystems in systems Bag:
		for ( int s = 0; s < sysLen; ++s )
		{
			final EntitySystem trg = arSystems[s];
			{
				final Field[] fields = trg.getClass().getDeclaredFields();
				final int fieldsLen = fields.length;

				// Collect all the fields that are of ComponentMapper class.
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
			
			// Now for each of those ComponentMapper fields in the system:
			for ( int f = 0; f < fieldsLen; ++f )
			{
				final Field field = fields[f];
				
				final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
				final Class<? extends Component> componentType = (Class<? extends Component>) genericType.getActualTypeArguments()[0];
				final ComponentMapper<? extends Component> mapper = new ComponentMapper<>( componentType, world );

				// Set accessible through Reflection.
				field.setAccessible( true );
				try
				{
					// Assign proper ComponentMapper instance.
					field.set( trg, mapper );
				}
				catch ( IllegalArgumentException | IllegalAccessException e )
				{
					throw new RuntimeException( "Error while setting component mappers", e );
				}
				finally
				{
					// Return the field to its initial state regardless of successful assignment or not.
					field.setAccessible( false );
				}
			}
			// Clear the field bag for the next system.
			fieldBag.clear();
		}
	}
}
