package com.artemis;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.artemis.annotations.EntitiesOf;
import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableIntBag;

/**
 * Class with static method for initializing {@link ComponentHandler}s and
 * {@link EntityObserver}s. <br>
 * It just plain initializes any {@link ComponentHandler} or
 * {@link EntityObserver} field it comes across in each object.
 * 
 * @author dustContributor
 */
final class Injector
{
	private final World world;

	private Injector ( final World world )
	{
		if ( world == null )
		{
			throw new DustException( this, "world can't be null!" );
		}
		this.world = world;
	}

	/**
	 * For each of the objects provided, they will get assigned an instance of
	 * their {@link EntityObserver}, {@link ComponentHandler} or entity
	 * {@link ImmutableIntBag} fields.
	 * 
	 * @param objects that you need initialized.
	 */
	static final void init ( final World world, final Object[] objects )
	{
		// Injector instance that holds a world reference.
		final Injector injector = new Injector( world );
		// Reasonable initial field amount.
		final Bag<Field> fields = new Bag<>( new Field[32] );
		// Predicates to test if the field is the appropriate one.
		@SuppressWarnings( { "unchecked" } )
		final Predicate<Field>[] tests = new Predicate[3];
		tests[0] = Injector::testForHandler;
		tests[1] = Injector::testForObserver;
		tests[2] = Injector::testForEntitiesOf;
		// Suppliers for each of the fields that need to be injected.
		@SuppressWarnings( "unchecked" )
		final BiFunction<Field, Object, Object>[] suppliers = new BiFunction[3];
		suppliers[0] = injector::supplyHandler;
		suppliers[1] = injector::supplyObserver;
		suppliers[2] = injector::supplyEntities;

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
			initFields( fields, obj, tests, suppliers );
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
	private static final void initFields (
			final Bag<Field> fields,
			final Object obj,
			final Predicate<Field>[] tests,
			final BiFunction<Field, Object, Object>[] suppliers )
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
					trySetField( field, obj, suppliers[i].apply( field, obj ) );
					// Field already set, jump to the next one.
					break;
				}
			}
		}
	}

	private final Object supplyHandler ( final Field field, final Object obj )
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
			throw new DustException( Injector.class, "Cant find HANDLER for the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	private final Object supplyObserver ( final Field field, final Object obj )
	{
		final Class<?> type = field.getType();
		@SuppressWarnings( "unchecked" )
		final EntityObserver value = world.getObserver( (Class<EntityObserver>) type );
		// Check for missing observer.
		if ( value == null )
		{
			final String tname = type.getSimpleName();
			throw new DustException( Injector.class, "Cant find OBSERVER of the type " + tname + " to inject!" );
		}
		// Everything OK.
		return value;
	}

	private final Object supplyEntities ( final Field field, final Object obj )
	{
		/*
		 * ImmutableIntBag has no superclasses besides Object so just check
		 * directly.
		 */
		if ( !(ImmutableIntBag.class == field.getType()) )
		{
			/*
			 * Fetch names at runtime so refactoring names wont mess up the message.
			 */
			final String anName = EntitiesOf.class.getSimpleName();
			final String listName = ImmutableIntBag.class.getSimpleName();
			final String tmsg = (obj == null) ? "null" : obj.getClass().getSimpleName();
			final String fmsg = field.toString();
			// Compose error message and throw exception.
			throw new DustException( Injector.class,
					"While injecting FIELD: " + fmsg +
							", in object: " + tmsg + ". Can only use " + anName +
							" annotation on " + listName + " fields!" );
		}

		final EntitiesOf ients = field.getAnnotation( EntitiesOf.class );
		final Class<? extends EntitySystem> type = ients.value();
		final EntitySystem source = world.getObserver( type );
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
	 * Tests if the passed field is a {@link ComponentHandler}.
	 * 
	 * @param field to test.
	 * @return 'true' if it is, 'false' otherwise.
	 */
	private static final boolean testForHandler ( final Field field )
	{
		return ComponentHandler.class == field.getType();
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
		// Set accessible through reflection.
		field.setAccessible( true );
		try
		{
			// Only modify the field if it hasn't been set already.
			if ( field.get( target ) == null )
			{
				// Assign the passed value.
				field.set( target, value );
			}
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
							", in instance: " + tmsg,
					e );
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
