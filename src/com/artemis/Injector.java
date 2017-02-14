package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.Function;
import java.util.function.Predicate;

//import com.artemis.annotations.EntitiesOf;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableIntBag;

/**
 * Class with static method for initializing {@link ComponentHandler}s and
 * {@link DustStep}s. <br>
 * It just plain initializes any {@link ComponentHandler} or {@link DustStep}
 * field it comes across in each object.
 *
 * @author dustContributor
 */
final class Injector
{
	private final DustContext world;
	private final Predicate<Field>[] tests;
	private final Function<Field, Object>[] suppliers;

	private Injector ( final DustContext world )
	{
		// Init all fields.
		this.world = world;
		// Predicates to test if the field is the appropriate one.
		this.tests = asArray(
				Injector::testForHandler,
				Injector::testForObserver );
		// Suppliers for each of the fields that need to be injected.
		this.suppliers = asArray(
				this::supplyHandler,
				this::supplyObserver );
	}

	/**
	 * Just to get around the generic array initializer limitation.
	 *
	 * @param params to convert to array.
	 * @return an array composed of the parameters.
	 */
	@SafeVarargs
	private static final <T> T[] asArray ( final T... params )
	{
		return params;
	}

	/**
	 * For each of the objects provided, they will get assigned an instance of
	 * their {@link DustStep}, {@link ComponentHandler} or entity
	 * {@link ImmutableIntBag} fields.
	 *
	 * @param objects that you need initialized.
	 */
	static final void init ( final DustContext world, final Object[] objects )
	{
		// Validate params.
		DustException.enforceNonNull( Injector.class, world, "world" );
		DustException.enforceNonNull( Injector.class, objects, "objects" );

		// Injector instance that holds a world reference.
		final Injector injector = new Injector( world );
		// Reasonable initial field amount.
		final Bag<Field> fields = new Bag<>( new Field[32] );

		// For each of the objects in the array:
		for ( int s = objects.length; s-- > 0; )
		{
			final Object obj = objects[s];
			/*
			 * Iterating over superclasses until reaching Object. Otherwise instances
			 * would only get assigned to the fields declared in the downmost subclass
			 * in the hierarchy.
			 */
			for ( Class<?> clazz = obj.getClass(); clazz != Object.class; clazz = clazz.getSuperclass() )
			{
				fields.addAll( clazz.getDeclaredFields() );
			}
			/*
			 * Initialize all the collected fields in this object.
			 */
			injector.initFields( fields, obj );
			// Clear the field bag for the next object.
			fields.setSize( 0 );
		}
	}

	/**
	 * Initializes all the fields in the bag, if they match a test, to the value
	 * produced by its respective supplier.
	 *
	 * @param fields to initialize.
	 * @param obj owner of these fields.
	 * @param tests to try on each field.
	 * @param suppliers to fetch the instances to set in the fields.
	 */
	private final void initFields ( final Bag<Field> fields, final Object obj )
	{
		final Field[] flds = fields.data();

		// Now for each of those ComponentHandler fields in the object:
		for ( int f = fields.size(); f-- > 0; )
		{
			final Field field = flds[f];

			for ( int i = tests.length; i-- > 0; )
			{
				if ( tests[i].test( field ) )
				{
					// Try to set the field with the supplied value.
					trySetField( field, obj, suppliers[i].apply( field ) );
					// Field already set, jump to the next one.
					break;
				}
			}
		}
	}

	private final Object supplyHandler ( final Field field )
	{
		final ParameterizedType genericType = (ParameterizedType) field.getGenericType();
		@SuppressWarnings( "unchecked" )
		final Class<? extends Component> componentType = (Class<? extends Component>) genericType
				.getActualTypeArguments()[0];
		final ComponentHandler<? extends Component> value = world.getHandler( componentType );
		// Check for missing handler.
		if ( value == null )
		{
			final String tname = componentType.getSimpleName();
			throw new DustException( this, "Cant find HANDLER for the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	private final Object supplyObserver ( final Field field )
	{
		final Class<?> type = field.getType();
		@SuppressWarnings( "unchecked" )
		final DustStep value = world.step( (Class<DustStep>) type );
		// Check for missing observer.
		if ( value == null )
		{
			final String tname = type.getSimpleName();
			throw new DustException( this, "Cant find OBSERVER of the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	/**
	 * Tests if the passed field is subclass of {@link ComponentHandler}.
	 *
	 * @param field to test.
	 * @return 'true' if it is, 'false' otherwise.
	 */
	private static final boolean testForHandler ( final Field field )
	{
		return ComponentHandler.class.isAssignableFrom( field.getType() );
	}

	/**
	 * Tests if the passed field is a subclass of {@link DustStep}.
	 *
	 * @param field to test.
	 * @return 'true' if it is, 'false' otherwise.
	 */
	private static final boolean testForObserver ( final Field field )
	{
		return DustStep.class.isAssignableFrom( field.getType() );
	}

	private static final void trySetField ( final Field field, final Object target, final Object value )
	{
		// Store current state of the field.
		final boolean wasntAccessible = !field.isAccessible();
		// If its accessible, no need to change it.
		if ( wasntAccessible )
		{
			field.setAccessible( true );
		}
		try
		{
			if ( field.get( target ) != null )
			{
				// Only modify the field if it hasn't been set already.
				throw new DustException( Injector.class, "The FIELD already has a value!" );
			}

			// Assign the passed value.
			field.set( target, value );
		}
		catch ( final IllegalArgumentException | IllegalAccessException | DustException e )
		{
			final String vmsg = String.valueOf( value );
			final String tmsg = String.valueOf( target );
			final String fmsg = String.valueOf( field );
			// Compose error message and throw exception.
			final String emsg = "While INJECTING object instance: " + System.lineSeparator()
					+ vmsg + System.lineSeparator()
					+ "In the field: " + System.lineSeparator()
					+ fmsg + System.lineSeparator()
					+ "Of the object: " + System.lineSeparator()
					+ tmsg;
			throw new DustException( Injector.class, emsg, e );
		}
		finally
		{
			/*
			 * Return the field to its initial state regardless of successful
			 * assignment or not.
			 */
			if ( wasntAccessible )
			{
				field.setAccessible( false );
			}
		}
	}

}
