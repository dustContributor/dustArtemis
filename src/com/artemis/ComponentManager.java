package com.artemis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.StreamSupport;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * @author Arni Arent
 * @author dustContributor
 */
public final class ComponentManager
{
	/** Mutable iterator for component bits. */
	private final MutableBitIterator bitIterator = new MutableBitIterator();

	private final int wordsPerEntity;

	private final int[] componentHashCodes;
	/** Component bags by type index. */
	private final ComponentHandler<Component>[] componentHandlers;
	/** Component bits for all entities. */
	private final OpenBitSet componentBits;

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	ComponentManager ( final Iterable<Class<? extends Component>> componentTypes )
	{
		// Get array of component types. Essentially a defensive copy.
		final Class<? extends Component>[] types = StreamSupport
				.stream( componentTypes.spliterator(), false )
				.toArray( Class[]::new );
		// Get a sorted hash code array from it.
		final int[] componentHashCodes = Arrays.stream( types ).mapToInt( Object::hashCode ).sorted().toArray();

		if ( hasDuplicates( componentHashCodes ) )
		{
			/*
			 * There is really no recovery from this, if the JVM returns the same
			 * hashCode for two different Class objects, we're screwed.
			 */
			throw new DustException( this,
					"Found a duplicate hashCode for different elements in 'componentTypes'!"
							+ " dustArtemis can't work if this happens, it needs different hash codes"
							+ " for different component Class objects." );
		}

		// Sort the component types by hash code.
		Arrays.sort( types, ( a, b ) -> Integer.compare( a.hashCode(), b.hashCode() ) );

		// Reasonable initial capacity.
		final int handlerCap = Math.max( 32, DAConstants.APPROX_LIVE_ENTITIES / 2 );

		final ComponentHandler[] componentHandlers = new ComponentHandler[types.length];
		// Init all the component handlers.
		for ( int i = componentHandlers.length; i-- > 0; )
		{
			componentHandlers[i] = new ComponentHandler( types[i], this, i, handlerCap );
		}
		// Fetch the constant.
		final int wordsPerEntity = computeWordsPerEntity( types.length );

		// Init bitsets for all entities.
		final int bitCount = (DAConstants.APPROX_LIVE_ENTITIES * wordsPerEntity) * 64;
		final OpenBitSet componentBits = new OpenBitSet( bitCount );

		// Now keep everything we need.
		this.componentHashCodes = componentHashCodes;
		this.componentHandlers = componentHandlers;
		this.wordsPerEntity = wordsPerEntity;
		this.componentBits = componentBits;
		this.bitIterator.setBits( componentBits.getBits() );
	}

	private static final int computeWordsPerEntity ( final int componentTypeCount )
	{
		// Enforce a positive minimum so not to screw up the math.
		final int itmp = Math.max( componentTypeCount, 1 );
		// Now get how many 64 bit words do we need.
		return ((itmp - 1) / 64) + 1;
	}

	private static final boolean hasDuplicates ( final int[] numbers )
	{
		final HashSet<Integer> values = new HashSet<>( numbers.length );

		for ( final int val : numbers )
		{
			if ( !values.add( Integer.valueOf( val ) ) )
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * @param id of the entity to fetch all its components from.
	 * @param dest bag to store all the components.
	 * @return bag passed as 'dest' parameter.
	 */
	public final Bag<Component> getComponentsFor ( final int id, final Bag<Component> dest )
	{
		final ComponentHandler<Component>[] cmpBags = componentHandlers;
		final MutableBitIterator it = bitIterator;
		final int start = id * wordsPerEntity;
		final int end = start + wordsPerEntity;
		it.selectWord( start );

		for ( int i = it.nextSetBit( end ); i >= 0; i = it.nextSetBit( end ) )
		{
			dest.add( cmpBags[i].get( id ) );
		}

		return dest;
	}

	/**
	 * Either creates or retrieves a component handler for the passed component
	 * type.
	 *
	 * @param type of the component to create or retrieve a handler from.
	 * @return handler of the component type.
	 */
	@SuppressWarnings( "unchecked" )
	public final <T extends Component> ComponentHandler<T> getHandlerFor ( final Class<T> type )
	{
		return (ComponentHandler<T>) componentHandlers[indexFor( type )];
	}

	final void registerEntity ( final int eid )
	{
		final int index = eid * wordsPerEntity;

		if ( index >= componentBits.length() )
		{
			componentBits.resizeWords( index + 1 );
			bitIterator.setBits( componentBits() );
		}
	}

	/**
	 * Notifies this component manager that a component was added to an entity.
	 *
	 * @param id of the entity to add a component to.
	 * @param typeIndex of the component that was added.
	 */
	final void notifyAddedComponent ( final int id, final int typeIndex )
	{
		BitUtil.setRelative( componentBits(), typeIndex, id, wordsPerEntity );
	}

	/**
	 * Notifies this component manager that a component was removed from an
	 * entity.
	 *
	 * @param id of the entity to remove a component from.
	 * @param typeIndex of the component that was removed.
	 */
	final void notifyRemovedComponent ( final int id, final int typeIndex )
	{
		BitUtil.clearRelative( componentBits(), typeIndex, id, wordsPerEntity );
	}

	final long[] componentBits ()
	{
		return componentBits.getBits();
	}

	final void clean ( final ImmutableIntBag deleted )
	{
		// Clear all components.
		clearComponents( ((IntBag) deleted).data(), deleted.size() );
	}

	private final void clearComponents ( final int[] ents, final int size )
	{
		final ComponentHandler<Component>[] cmpBags = componentHandlers;
		final long[] bits = componentBits();
		final MutableBitIterator it = bitIterator;
		final int wordCount = wordsPerEntity;

		for ( int i = size; i-- > 0; )
		{
			final int eid = ents[i];
			final int start = eid * wordCount;
			final int end = start + wordCount;
			// Now iterate over normal component bits and remove them.
			it.selectWord( start );

			for ( int j = it.nextSetBit( end ); j >= 0 && j < cmpBags.length; j = it.nextSetBit( end ) )
			{
				cmpBags[j].data()[eid] = null;
			}

			// Now clear all component bits from the entity.
			BitUtil.clearWords( bits, start, end );
		}
	}

	/**
	 * Searches for the index of the passed component type, based on its hash
	 * code.
	 *
	 * @param type of the component.
	 * @return index of the component type, or -1 if it wasn't found.
	 */
	final int indexFor ( final Class<? extends Component> type )
	{
		return Arrays.binarySearch( componentHashCodes, type.hashCode() );
	}

	/**
	 * @return how many 64 bit words an entity requires to have its components
	 *         tracked.
	 */
	final int wordsPerEntity ()
	{
		return wordsPerEntity;
	}
}
