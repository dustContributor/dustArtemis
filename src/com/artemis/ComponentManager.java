package com.artemis;

import java.util.Arrays;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
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

	/** Component bags by type index. */
	private ComponentHandler<Component>[] componentHandlers;
	/** Component bits for all entities. */
	private final OpenBitSet componentBits;

	@SuppressWarnings( { "unchecked" } )
	ComponentManager ()
	{
		// Init all type bags.
		componentHandlers = new ComponentHandler[DAConstants.COMPONENT_TYPES_COUNT];
		// Init bitsets for all entities.
		final int wCnt = DAConstants.COMPONENT_BITS_WORD_COUNT;
		final int bitCount = (DAConstants.APPROX_LIVE_ENTITIES * wCnt) * 64;
		componentBits = new OpenBitSet( bitCount );
		bitIterator.setBits( componentBits.getBits() );
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
		final int start = id * DAConstants.COMPONENT_BITS_WORD_COUNT;
		final int end = start + DAConstants.COMPONENT_BITS_WORD_COUNT;
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
		return (ComponentHandler<T>) initIfAbsent( indexFor( type ), type );
	}

	final void registerEntity ( final int eid )
	{
		final int index = eid * DAConstants.COMPONENT_BITS_WORD_COUNT;

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
		BitUtil.setRelative( componentBits(), typeIndex, id, DAConstants.COMPONENT_BITS_WORD_COUNT );
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
		BitUtil.clearRelative( componentBits(), typeIndex, id, DAConstants.COMPONENT_BITS_WORD_COUNT );
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
		final int wordCount = DAConstants.COMPONENT_BITS_WORD_COUNT;

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
	 * If the component index passed is too high for the
	 * {@link #componentHandlers} to hold, it resizes it.
	 *
	 * It also initializes the handler if it isn't present.
	 *
	 * @param typeIndex component type index to check if it has a component bag
	 *          initialized.
	 * @return Component bag for the given component index.
	 */
	@SuppressWarnings( { "rawtypes", "unchecked" } )
	private final ComponentHandler<Component> initIfAbsent (
			final int typeIndex,
			final Class<? extends Component> type )
	{
		// If type bag can't hold this component type.
		if ( typeIndex >= componentHandlers.length )
		{
			final int newLen = BitUtil.nextHighestPowerOfTwo( typeIndex + 1 );
			componentHandlers = Arrays.copyOf( componentHandlers, newLen );
		}

		final ComponentHandler<Component>[] handlers = componentHandlers;

		if ( handlers[typeIndex] == null )
		{
			final int cap = DAConstants.APPROX_LIVE_ENTITIES / 2;
			handlers[typeIndex] = new ComponentHandler( type, this, typeIndex, cap );
		}

		return handlers[typeIndex];
	}

	private static final int indexFor ( final Class<? extends Component> type )
	{
		return ClassIndexer.getIndexFor( type, Component.class );
	}

}
