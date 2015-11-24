package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.artemis.annotations.EntitiesOf;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableIntBag;

/**
 * Class with static method for initializing {@link ComponentMapper}s and
 * {@link EntityObserver}s. <br>
 * It just plain initializes any {@link ComponentMapper} or
 * {@link EntityObserver} field it comes across in each observer.
 * 
 * @author dustContributor
 */
final class Injector
{
	/** Non-instantiable class */
	private Injector ()
	{
		// Empty.
	}

	/**
	 * For each of the EntityObservers provided, they will get a proper instance
	 * for each of their {@link ComponentMapper} or {@link EntityObserver}
	 * fields.
	 * 
	 * @param observers that you need initialized.
	 */
	static final void init ( final Bag<? extends EntityObserver> observers )
	{
		final EntityObserver[] obs = observers.data();
		// Reasonable field amount.
		final Bag<Field> fields = new Bag<>( Field.class, 32 );
		// Predicates to test if the field is the appropiate one.
		@SuppressWarnings ( { "unchecked" } )
		final Predicate<Field>[] tests = new Predicate[3];
		tests[0] = Injector::testForMapper;
		tests[1] = Injector::testForObserver;
		tests[2] = Injector::testForEntitiesOf;
		// Suppliers for each of the fields that need to be injected.
		@SuppressWarnings ( "unchecked" )
		final BiFunction<Field, EntityObserver, Object>[] suppliers = new BiFunction[3];
		suppliers[0] = Injector::supplyMapper;
		suppliers[1] = Injector::supplyObserver;
		suppliers[2] = Injector::supplyEntities;

		// For each of the EntityObservers in observers Bag:
		for ( int s = observers.size(); s-- > 0; )
		{
			final EntityObserver observer = obs[s];
			/*
			 * Iterating over superclasses until reaching Object. This allows
			 * using EntityObserver class hierarchies inheriting fields to be
			 * injected. Otherwise instances would only get assigned to the
			 * fields declared in the downmost EntityObserver subclass in the
			 * hierarchy.
			 */
			for ( Class<?> clazz = observer.getClass(); clazz != Object.class; clazz = clazz.getSuperclass() )
			{
				fields.addAll( clazz.getDeclaredFields() );
			}
			/*
			 * Initialize all the collected fields in this observer.
			 */
			initFields( fields, observer, tests, suppliers );
			// Clear the field bag for the next observer.
			fields.setSize( 0 );
		}
	}

	/**
	 * Initializes all the fields in the bag, if they match a test, to the value
	 * produced by its respective supplier.
	 * 
	 * @param fields to initialize.
	 * @param observer owner of these fields.
	 * @param tests to try on each field.
	 * @param suppliers to fetch the instances to set in the fields.
	 */
	private static final void initFields (
		final Bag<Field> fields,
		final EntityObserver observer,
		final Predicate<Field>[] tests,
		final BiFunction<Field, EntityObserver, Object>[] suppliers )
	{
		final Field[] flds = fields.data();

		// Now for each of those ComponentMapper fields in the observer:
		for ( int f = fields.size(); f-- > 0; )
		{
			final Field field = flds[f];

			for ( int i = tests.length; i-- > 0; )
			{
				if ( tests[i].test( field ) )
				{
					// Try to set the field with the supplied value.
					trySetField( field, observer, suppliers[i].apply( field, observer ) );
					// Field already set, jump to the next one.
					continue;
				}
			}
		}
	}

	private static final Object supplyMapper ( final Field field, final EntityObserver observer )
	{
		final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
		@SuppressWarnings ( "unchecked" )
		final Class<? extends Component> componentType =
				(Class<? extends Component>) genericType.getActualTypeArguments()[0];
		final ComponentMapper<? extends Component> value = observer.world.getMapper( componentType );
		// Check for missing mapper.
		if ( value == null )
		{
			final String tname = componentType.getSimpleName();
			throw new DustException( Injector.class, "Cant find MAPPER for the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	private static final Object supplyObserver ( final Field field, final EntityObserver observer )
	{
		final Class<?> type = field.getType();
		@SuppressWarnings ( "unchecked" )
		final EntityObserver value = observer.world.getObserver( (Class<EntityObserver>) type );
		// Check for missing observer.
		if ( value == null )
		{
			final String tname = type.getSimpleName();
			throw new DustException( Injector.class, "Cant find OBSERVER of the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	private static final Object supplyEntities ( final Field field, final EntityObserver observer )
	{
		/*
		 * ImmutableIntBag has no superclasses besides Object so just check
		 * directly.
		 */
		if ( !(ImmutableIntBag.class == field.getType()) )
		{
			/*
			 * Fetch names at runtime so refactoring names wont mess up the
			 * message.
			 */
			final String anName = EntitiesOf.class.getSimpleName();
			final String listName = ImmutableIntBag.class.getSimpleName();
			final String tmsg = (observer == null) ? "null" : observer.getClass().getSimpleName();
			final String fmsg = field.toString();
			// Compose error message and throw exception.
			throw new DustException( Injector.class,
					"While injecting FIELD: " + fmsg +
							", in observer: " + tmsg + ". Can only use " + anName +
							" annotation on " + listName + " fields!" );
		}

		final EntitiesOf ients = field.getAnnotation( EntitiesOf.class );
		final Class<? extends EntitySystem> type = ients.value();
		final EntitySystem source = observer.world.getObserver( type );
		// Check if the entity list source is null.
		if ( source == null )
		{
			final String tname = type.getSimpleName();
			throw new DustException( Injector.class,
					"Cant find OBSERVER of the type " + tname
							+ " to fetch entity list from!" );
		}
		// Everything OK.
		return source.actives();
	}

	/**
	 * Tests if the passed field is a {@link ComponentMapper}.
	 * 
	 * @param field to test.
	 * @return 'true' if it is, 'false' otherwise.
	 */
	private static final boolean testForMapper ( final Field field )
	{
		return ComponentMapper.class == field.getType();
	}

	/**
	 * Tests if the passed field is a subclass of {@link EntityObserver}.
	 * 
	 * @param field to test.
	 * @return 'true' if it is, 'false' otherwise.
	 */
	private static final boolean testForObserver ( final Field field )
	{
		return EntityObserver.class.isAssignableFrom( field.getType() );
	}

	/**
	 * Tests if the passed field possesses the annotation {@link EntitiesOf}.
	 * 
	 * @param field to test.
	 * @return 'true' if it has it, 'false' otherwise.
	 */
	private static final boolean testForEntitiesOf ( final Field field )
	{
		return field.getAnnotation( EntitiesOf.class ) != null;
	}

	private static final void trySetField ( final Field field, final Object target, final Object value )
	{
		// Set accessible through Reflection.
		field.setAccessible( true );
		try
		{
			// Assign the passed value.
			field.set( target, value );
		}
		catch ( IllegalArgumentException | IllegalAccessException e )
		{
			final String vmsg = String.valueOf( value );
			final String tmsg = String.valueOf( target );
			final String fmsg = String.valueOf( field );
			// Compose error message and throw exception.
			throw new DustException( Injector.class,
					"While injecting object: " + vmsg +
							", in field: " + fmsg +
							", in instance: " + tmsg, e );
		}
		finally
		{
			/*
			 * Return the field to its initial state regardless of successful
			 * assignment or not.
			 */
			field.setAccessible( false );
		}
	}

}
