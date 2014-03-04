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
	static final void initFor ( final Bag<? extends EntitySystem> systems, final World world )
	{
		final EntitySystem[] arSystems = systems.data();
		final int sysLen = systems.size();
		
		// Bag big enough so it shouldn't grow when adding fields to it.
		final Bag<Field> fieldBag = new Bag<>( Field.class, 32 );
		
		// For each of the EntitySystems in systems Bag:
		for ( int s = 0; s < sysLen; ++s )
		{
			final EntitySystem system = arSystems[s];
			
			// Collect all the fields that are of ComponentMapper class in this system.
			collectMapperFields( system.getClass().getDeclaredFields(), fieldBag );
			// Initialize all the collected mappers in this system for the supplied World.
			initMapperFields( fieldBag, system, world );
			
			// Clear the field bag for the next system.
			fieldBag.clear();
		}
	}
	
	/**
	 * Collects all the fields that are of ComponentMapper class into the supplied bag.
	 * 
	 * @param fields to be checked.
	 * @param fieldBag where the mappers will be collected.
	 */
	private static final void collectMapperFields ( final Field[] fields, final Bag<Field> fieldBag )
	{
		// Collect all the fields that are of ComponentMapper class.
		for ( int i = 0; i < fields.length; ++i )
		{
			final Field field = fields[i];

			if ( field.getType() == ComponentMapper.class )
			{
				fieldBag.add( field );
			}
		}
	}
	
	/**
	 * Initializes all ComponentMapper fields in the bag.
	 * 
	 * @param fieldBag filled with ComponentMappers.
	 * @param system owner of these fields.
	 * @param world where to pull the ComponentMappers from.
	 */
	@SuppressWarnings ( "unchecked" )
	private static final void initMapperFields ( final Bag<Field> fieldBag, final EntitySystem system, final World world )
	{
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
				field.set( system, mapper );
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
	}
}
