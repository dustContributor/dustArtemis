package com.artemis;

import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ClassIndexer;
import com.artemis.utils.ImmutableBag;
import com.artemis.utils.MutableBitIterator;

/**
 * The most raw entity system. It should not typically be used, but you can
 * create your own entity system handling by extending this. It is recommended
 * that you use the other provided entity system implementations.
 * 
 * @author Arni Arent
 * 
 */
public abstract class EntitySystem extends EntityObserver
{
	private final int index;
	private final Aspect aspect;

	private final Bag<Entity> actives;

	private final OpenBitSet activeBits;
	private final MutableBitIterator bitIterator;

	private int minModifiedId = Integer.MAX_VALUE;

	/**
	 * Creates an entity system that uses the specified aspect as a matcher
	 * against entities.
	 * 
	 * @param aspect to match against entities
	 */
	public EntitySystem ( final Aspect aspect )
	{
		this.index = ClassIndexer.getIndexFor( this.getClass(), EntitySystem.class );
		this.bitIterator = new MutableBitIterator();

		this.aspect = aspect.isEmpty() ? null : aspect;

		// Fetch entity amount per system.
		int actSize = DAConstants.APPROX_ENTITIES_PER_SYSTEM;

		this.actives = new Bag<>( Entity.class, actSize );
		this.activeBits = new OpenBitSet( actSize );

		this.setActive( false );
	}

	/**
	 * Called before processing of entities begins.
	 */
	protected void begin ()
	{
		// Empty method.
	}

	@Override
	public final void process ()
	{
		if ( minModifiedId < Integer.MAX_VALUE )
		{
			rebuildEntityList();
			// Reset min modified entity ID.
			minModifiedId = Integer.MAX_VALUE;
		}

		begin();
		processEntities( actives );
		end();
	}

	private final void rebuildEntityList ()
	{
		final int nSize = activeBits.cardinality();
		actives.ensureCapacity( nSize );
		actives.setSize( nSize );

		final MutableBitIterator mbi = bitIterator;
		mbi.setBits( activeBits.getBits() );

		final Entity[] actEnts = actives.data();
		final Entity[] ents = world.getEntityManager().entities.data();

		// Fetch min modified entity ID.
		final int minId = minModifiedId;

		int i;
		int j = 0;

		// Basically, try to start counting from the lowest ID modified.
		while ( (i = mbi.nextSetBit()) < minId && i >= 0 )
		{
			++j;
		}
		// From the found position, copy all the entity references in the array.
		for ( ; i >= 0; i = mbi.nextSetBit(), ++j )
		{
			actEnts[j] = ents[i];
		}
	}

	/**
	 * Called after the processing of entities ends.
	 */
	protected void end ()
	{
		// Empty method.
	}

	/**
	 * Any implementing entity system must implement this method and the logic
	 * to process the given entities of the system.
	 * 
	 * @param entities the entities this system contains.
	 */
	protected abstract void processEntities ( ImmutableBag<Entity> entities );

	/**
	 * Called if the system has received a entity it is interested in, e.g.
	 * created or a component was added to it.
	 * 
	 * @param e the entity that was added to this system.
	 */
	protected void inserted ( final Entity e )
	{
		// Empty method.
	}

	/**
	 * Called if a entity was removed from this system, e.g. deleted or had one
	 * of it's components removed.
	 * 
	 * @param e the entity that was removed from this system.
	 */
	protected void removed ( final Entity e )
	{
		// Empty method.
	}

	private final void removeFromSystem ( final Entity e )
	{
		final int eid = e.id;

		minModifiedId = Math.min( minModifiedId, eid );

		activeBits.fastClear( eid );
		removed( e );
	}

	private final void insertToSystem ( final Entity e )
	{
		final int eid = e.id;

		minModifiedId = Math.min( minModifiedId, eid );

		activeBits.set( eid );
		inserted( e );
	}

	@Override
	public void added ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	@Override
	public void changed ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	@Override
	public void deleted ( final ImmutableBag<Entity> entities )
	{
		removeAll( entities );
	}

	@Override
	public void disabled ( final ImmutableBag<Entity> entities )
	{
		removeAll( entities );
	}

	private final void removeAll ( final ImmutableBag<Entity> entities )
	{
		if ( aspect == null )
		{
			// Basically, a void entity system.
			return;
		}

		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;

		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];

			if ( acBits.get( e.id ) )
			{
				removeFromSystem( e );
			}
		}
	}

	@Override
	public void enabled ( final ImmutableBag<Entity> entities )
	{
		checkAll( entities );
	}

	/**
	 * Adds entity if the system is interested in it and hasn't been added
	 * before.
	 * 
	 * Removes entity from system if its not interesting and it has been added
	 * before.
	 * 
	 * @param e entities to check.
	 */
	private final void checkAll ( final ImmutableBag<Entity> entities )
	{
		final Aspect asp = aspect;
		if ( asp == null )
		{
			// Basically, a void entity system.
			return;
		}

		// Fetch bits of active entities.
		final OpenBitSet acBits = activeBits;

		final Entity[] array = ((Bag<Entity>) entities).data();
		final int size = entities.size();

		for ( int i = 0; i < size; ++i )
		{
			final Entity e = array[i];
			// Second bit for 'contains'.
			int flags = acBits.getBit( e.id ) << 1;
			// First bit for 'interesting'.
			flags |= asp.isInteresting( e.componentBits ) ? 0b1 : 0b0;

			switch (flags)
			{
			// Interesting and system doesn't contains.
				case 0b01:
				{
					insertToSystem( e );
					continue;
				}
				// Not interesting and system does contains.
				case 0b10:
				{
					removeFromSystem( e );
					continue;
				}
				// Otherwise do nothing.
				default:
					continue;
			}
		}
	}

	public int getIndex ()
	{
		return index;
	}

	public ImmutableBag<Entity> getActives ()
	{
		return actives;
	}

}
