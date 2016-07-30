package com.artemis;

import java.util.Arrays;
import java.util.function.BiConsumer;

import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * This kind of observer will track an "active" list of entities that match a
 * certain {@link Aspect} and when they get inserted/removed into/from the
 * observer.
 *
 * @author Arni Arent
 * @author dustContributor
 */
public abstract class EntitySystem extends AbstractEntitySystem
{
	private final IntBag actives = new IntBag( activeBits.capacity() );
	private final MutableBitIterator bitIterator = new MutableBitIterator();

	/**
	 * Creates an entity observer that uses the specified aspect as a matcher
	 * against entities.
	 *
	 * @param aspect to match against entities.
	 */
	public EntitySystem ( final Aspect aspect )
	{
		super( aspect );
	}

	/**
	 * Called before processing of entities begins. Always called.
	 */
	protected void begin ()
	{
		// Empty method.
	}

	@Override
	public final void process ()
	{
		// Always called.
		begin();
		// If there is any entity to process.
		if ( !actives.isEmpty() )
		{
			processEntities( actives );
		}
		// Also always called.
		end();
	}

	/**
	 * Called after the processing of entities ends. Always called.
	 */
	protected void end ()
	{
		// Empty method.
	}

	/**
	 * Any implementing entity observer must implement this method and the logic
	 * to process the given entities of the observer. Called only if there are
	 * entities to process.
	 *
	 * @param entities the entities this observer contains.
	 */
	protected abstract void processEntities ( final ImmutableIntBag entities );

	@Override
	final void processModifiedEntities ()
	{
		// Now start checking of something actually changed.
		final int minIdRemoved = processIfModified( removed, EntitySystem::removed );
		final int minIdInserted = processIfModified( inserted, EntitySystem::inserted );
		// Compute real min modified entity id.
		final int minId = Math.min( minIdRemoved, minIdInserted );
		// Using max value as flag for no changes.
		if ( minId < Integer.MAX_VALUE )
		{
			// And rebuild this observer's entity list.
			rebuildEntityList( minId );
		}
	}

	/**
	 * If there is something in the passed entities bag, it will call the
	 * operation on the bag, clear it, then return the minimum affected entity id
	 * found.
	 *
	 * @param entities to process.
	 * @param operation to make on the entities.
	 * @return minimum affected entity id, or {@link Integer#MAX_VALUE} if there
	 *         was nothing in the bag.
	 */
	private final int processIfModified (
			final IntBag entities,
			final BiConsumer<EntitySystem, IntBag> operation )
	{
		// Using max value as flag for no changes.
		int minId = Integer.MAX_VALUE;

		if ( entities.isEmpty() )
		{
			// Nothing to compute.
			return minId;
		}

		final int[] data = entities.data();
		// Check changes in removed entities.
		for ( int i = entities.size(); i-- > 0; )
		{
			minId = Math.min( minId, data[i] );
		}
		// Let the observer process the events.
		operation.accept( this, entities );
		// Clear the container.
		entities.setSize( 0 );
		// Return min entity id that was modified.
		return minId;
	}

	private final void rebuildEntityList ( final int startId )
	{
		final int newSize = activeBits.cardinality();
		final int oldSize = actives.size();
		actives.setSize( newSize );
		actives.ensureCapacity( newSize );

		final int[] ids = actives.data();

		final MutableBitIterator mbi = bitIterator;
		mbi.setBits( activeBits.getBits() );
		mbi.selectIndex( startId );

		int j = Arrays.binarySearch( ids, 0, oldSize, startId );
		// Fix index if Entity ID isn't on the list yet.
		j = Math.max( j, -j - 1 );

		/*
		 * From the found position, rebuild the entity ID list.
		 *
		 * NOTE: It seems explicitly checking for j < ids.length helps the JIT a
		 * bit, j wont ever be bigger than ids.length, but probably the JIT can't
		 * infer that and checks every loop if it has to raise an out of bounds
		 * exception.
		 */
		for ( int i = mbi.nextSetBit(); i >= 0 && j < ids.length; i = mbi.nextSetBit(), ++j )
		{
			ids[j] = i;
		}
	}

	public final ImmutableIntBag actives ()
	{
		return actives;
	}

}
