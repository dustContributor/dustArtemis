package com.artemis;

import java.util.BitSet;

import com.artemis.utils.ClassIndexer;

/**
 * <p>An Aspects is used by systems as a matcher against entities, to check if a system is
 * interested in an entity. Aspects define what sort of component types an entity must
 * possess, or not possess.</p>
 * 
 * <p>This creates an aspect where an entity must possess A and B and C:
 * 
 * <p>Aspect.getAspectForAll(A.class, B.class, C.class)</p>
 * 
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V.</p>
 * 
 * <p>Aspect.getAspectForAll(A.class, B.class, C.class).exclude(U.class, V.class)</p>
 * 
 * <p>This creates an aspect where an entity must possess A and B and C, but must not possess U or V, but must possess one of X or Y or Z.</p>
 * 
 * <p>Aspect.getAspectForAll(A.class, B.class, C.class).exclude(U.class, V.class).one(X.class, Y.class, Z.class)</p>
 *
 * <p>You can create and compose aspects in many ways:</p>
 * 
 * <p>Aspect.getEmpty().one(X.class, Y.class, Z.class).all(A.class, B.class, C.class).exclude(U.class, V.class) is the same as:</p>
 * 
 * <p>Aspect.getAspectForAll(A.class, B.class, C.class).exclude(U.class, V.class).one(X.class, Y.class, Z.class)</p>
 *
 * @author Arni Arent
 *
 */
public class Aspect
{
	// Empty aspect.
	private static final Aspect EMPTY_ASPECT = new Aspect();
	
	// Bit sets marking the components this aspect is interested in.
	protected final BitSet allSet, exclusionSet, oneSet;

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
	 * @param type
	 *            a required component type
	 * @param types
	 *            a required component type
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect all ( final Class<? extends Component> type, final Class<? extends Component>... types )
	{
		allSet.set( ClassIndexer.getIndexFor( type, Component.class ) );

		for ( int i = 0; i < types.length; ++i )
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
	 * @param type
	 *            component type to exclude
	 * @param types
	 *            component type to exclude
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect exclude ( final Class<? extends Component> type, final Class<? extends Component>... types )
	{
		exclusionSet.set( ClassIndexer.getIndexFor( type, Component.class ) );

		for ( int i = 0; i < types.length; ++i )
		{
			exclusionSet.set( ClassIndexer.getIndexFor( types[i], Component.class ) );
		}

		return this;
	}

	/**
	 * Returns an aspect where an entity must possess one of the specified
	 * component types.
	 * 
	 * @param type
	 *            one of the types the entity must possess
	 * @param types
	 *            one of the types the entity must possess
	 * @return an aspect that can be matched against entities
	 */
	@SafeVarargs
	public final Aspect one ( final Class<? extends Component> type, final Class<? extends Component>... types )
	{
		oneSet.set( ClassIndexer.getIndexFor( type, Component.class ) );

		for ( int i = 0; i < types.length; ++i )
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
	 * 
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
