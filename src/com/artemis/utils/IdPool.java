package com.artemis.utils;

/**
 * Integer id pool.
 * 
 * @author dustContributor
 */
public class IdPool
{
	private final IntStack idStack;
	private int nextId;

	/**
	 * Creates a new id pool with a capacity of 512 ids.
	 */
	public IdPool ()
	{
		idStack = new IntStack( 512 );
		nextId = 0;
	}

	/**
	 * Creates a new id pool with the specified capacity.
	 * 
	 * <p>
	 * NOTE: Since the pool will be backed by an IntStack, its initial size will
	 * be either the provided one or whatever IntStack's minimum capacity is.
	 * </p>
	 * 
	 * @param initialSize of ids for the pool.
	 */
	public IdPool ( int initialSize )
	{
		idStack = new IntStack( initialSize );
		nextId = 0;
	}

	/**
	 * Retrieves an available id from the pool.
	 * 
	 * @return available int id.
	 */
	public int getId ()
	{
		if ( idStack.size() > 0 )
		{
			return idStack.unsafePop();
		}

		return nextId++;
	}

	/**
	 * Stores an id into the pool so it can be used later.
	 * 
	 * @param id
	 *            to be stored.
	 */
	public void putId ( final int id )
	{
		idStack.push( id );
	}
}
