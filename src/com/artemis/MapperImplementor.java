package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import com.artemis.utils.Bag;

/**
 * Class with static method for initializing ComponentMappers.
 * 
 * The init method of original Artemis got reworked this way so it operates on
 * an entire collection of observers.
 * 
 * It no longer uses annotations for defining what should be initialized. It
 * just plain initializes any ComponentMapper it comes across in each observer.
 * 
 * Looking for annotations was quite a bit slower, so changing it for direct
 * type comparison, making it iterate over all the observers inside the method
 * and making it so it collects the fields before initializing them, it made the
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
	 * For each of the EntityObservers provided in the Bag, they will get a
	 * proper instance for each of their ComponentMapper fields.
	 * 
	 * @param observers that you need initialized.
	 * @param world where the components come from.
	 */
	static final void initFor ( final Bag<? extends EntityObserver> observers, final World world )
	{
		final EntityObserver[] arObservers = observers.data();
		// Fetch mapper amount per observer.
		final int fbSize = DAConstants.APPROX_MAPPERS_PER_SYSTEM;
		// If correctly set, this bag won't need to grow.
		final Bag<Field> fieldBag = new Bag<>( Field.class, fbSize );

		// For each of the EntityObservers in observers Bag:
		for ( int s = observers.size(); s-- > 0; )
		{
			final EntityObserver observer = arObservers[s];
			/*
			 * Iterating over superclasses until reaching EntityObserver (or as
			 * a safety check, until reaching Object). This allows using
			 * EntityObserver class hierarchies inheriting mapper fields.
			 * Otherwise mappers would only get assigned to the mappers declared
			 * in the downmost EntityObserver subclass in the hierarchy.
			 */
			for ( Class<?> clazz = observer.getClass(); clazz != EntityObserver.class
					&& clazz != Object.class; clazz = clazz.getSuperclass() )
			{
				/*
				 * Collect all the fields that are of ComponentMapper class in
				 * this observer class.
				 */
				collectMapperFields( clazz.getDeclaredFields(), fieldBag );
			}
			/*
			 * Initialize all the collected mappers in this observer for the
			 * supplied World.
			 */
			initMapperFields( fieldBag, observer, world );
			// Clear the field bag for the next observer.
			fieldBag.setSize( 0 );
		}
	}

	/**
	 * Collects all the fields that are of ComponentMapper class into the
	 * supplied bag.
	 * 
	 * @param fields to be checked.
	 * @param fieldBag where the mappers will be collected.
	 */
	private static final void collectMapperFields ( final Field[] fields, final Bag<Field> fieldBag )
	{
		// Collect all the fields that are of ComponentMapper class.
		for ( int i = fields.length; i-- > 0; )
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
	 * @param observer owner of these fields.
	 * @param world where to pull the ComponentMappers from.
	 */
	@SuppressWarnings ( "unchecked" )
	private static final void initMapperFields (
		final Bag<Field> fieldBag,
		final EntityObserver observer,
		final World world )
	{
		final Field[] fields = fieldBag.data();

		// Now for each of those ComponentMapper fields in the observer:
		for ( int f = fieldBag.size(); f-- > 0; )
		{
			final Field field = fields[f];

			final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
			final Class<? extends Component> componentType =
					(Class<? extends Component>) genericType.getActualTypeArguments()[0];
			final ComponentMapper<? extends Component> mapper = world.getMapper( componentType );

			// Set accessible through Reflection.
			field.setAccessible( true );
			try
			{
				// Assign proper ComponentMapper instance.
				field.set( observer, mapper );
			}
			catch ( IllegalArgumentException | IllegalAccessException e )
			{
				throw new RuntimeException(
						"[dustArtemis] Error while injecting the component mappers.", e );
			}
			finally
			{
				/*
				 * Return the field to its initial state regardless of
				 * successful assignment or not.
				 */
				field.setAccessible( false );
			}
		}
	}
}
