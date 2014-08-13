package com.artemis;

import java.util.BitSet;

import com.artemis.utils.ClassIndexer;

/**
 * <p>An Aspects is used by systems as a matcher against entities, to check if a system is
 * interested in an entity. Aspects define what sort of component types an entity must
 * possess, or not possess.</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C:
 * 
 * <p>new Aspect().all(A.class, B.class, C.class)</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V.</p>
 * 
 * <p>new Aspect().all(A.class, B.class, C.class).exclude(U.class, V.class)</p>
 * <p></p>
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V, but must possess one of X or Y or Z.</p>
 * 
 * <p>new Aspect().all(A.class, B.class, C.class).exclude(U.class, V.class).one(X.class, Y.class, Z.class)</p>
 * 
 * @author Arni Arent
 *
 */
public final class Aspect
{
	// Empty aspect.
	private static final Aspect EMPTY_ASPECT = new Aspect();

	// Bit sets marking the components this aspect is interested in.
	protected final BitSet allSet;
	protected final BitSet exclusionSet;
	protected final BitSet oneSet;

	/**
	 * Creates a new empty aspect.
	 */
	public Aspect ()
	{
		allSet = new BitSet();
		exclusionSet = new BitSet();
		oneSet = new BitSet();
	}

	/**
	 * Returns an aspect where an entity must possess all of the specified
	 * component types.
	 * 
	 * @param types
	 *            a required component types
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect all ( final Class<? extends Component>... types )
	{
		for ( int i = types.length; i-- > 0; )
		{
			allSet.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
		}

		return this;
	}

	/**
	 * Excludes all of the specified component types from the aspect. A system
	 * will not be interested in an entity that possesses one of the specified
	 * exclusion component types.
	 * 
	 * @param types
	 *            component types to exclude
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect exclude ( final Class<? extends Component>... types )
	{
		for ( int i = types.length; i-- > 0; )
		{
			exclusionSet.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
		}

		return this;
	}

	/**
	 * Returns an aspect where an entity must possess one of the specified
	 * component types.
	 * 
	 * @param types
	 *            many of the types the entity must possess
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect one ( final Class<? extends Component>... types )
	{
		for ( int i = types.length; i-- > 0; )
		{
			oneSet.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
		}

		return this;
	}

	/**
	 * Returns an empty aspect. This can be used if you want a system that
	 * processes no entities, but still gets invoked. Typical usages is when you
	 * need to create special purpose systems for debug rendering, like
	 * rendering FPS, how many entities are active in the world, etc.
	 * <p></p>
	 * <p>
	 * <b>NOTE: Do not modify this aspect</b>, its a single static reference in
	 * Aspect class, it returns the same object always, if you modify it, you
	 * modify all the aspects returned by this method. Or you can ignore this
	 * and break everything, your call.
	 * </p>
	 * 
	 * @return an empty Aspect that will reject all entities.
	 */
	public static final Aspect getEmpty ()
	{
		return EMPTY_ASPECT;
	}

}
