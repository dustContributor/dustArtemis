package com.artemis;

import org.apache.lucene.util.BitUtil;

import com.artemis.utils.ClassIndexer;

/**
 * <p>
 * An Aspect is used by systems as a matcher against entities, to check if a
 * system is interested in an entity. Aspects define what sort of component
 * types an entity must possess, or not possess.
 * </p>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C:
 * </p>
 *
 * <pre>
 * Aspect a = Aspect.all( A.class, B.class, C.class ).build()
 * </pre>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C, but must
 * not possess U or V.
 * </p>
 *
 * <pre>
 * Aspect a = Aspect
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.build()
 * </pre>
 *
 * <p>
 * This creates an aspect where an entity must possess A and B and C, but must
 * not possess U or V, but must possess one of X or Y or Z.
 * </p>
 *
 * <pre>
 * Aspect a = Aspect
 * 	.all( A.class, B.class, C.class )
 * 	.exclude( U.class, V.class )
 * 	.one( X.class, Y.class, Z.class )
 * 	.build()
 * </pre>
 *
 * @author Arni Arent
 * @author dustContributor
 *
 */
public final class Aspect
{
	// Bit sets marking the components this aspect is interested in.
	private final long[] allSet;
	private final long[] noneSet;
	private final long[] oneSet;

	Aspect ( final long[] all, final long[] none, final long[] one )
	{
		// If any of the bit sets is empty, do not store them.
		DustException.enforceNonNull( Aspect.class, all, "all" );
		allSet = BitUtil.isEmpty( all ) ? null : all;

		DustException.enforceNonNull( Aspect.class, none, "none" );
		noneSet = BitUtil.isEmpty( none ) ? null : none;

		DustException.enforceNonNull( Aspect.class, one, "one" );
		oneSet = BitUtil.isEmpty( one ) ? null : one;
	}

	/**
	 * Checks if the components bits are interesting to this aspect.
	 *
	 * @param id of the entity to check
	 *
	 * @param bits to check
	 * @return 'true' if it's interesting, 'false' otherwise.
	 */
	public final boolean isInteresting ( final int id, final long[] bits )
	{
		// A bit of global access never hurt anyone.
		final int wordCount = DAConstants.COMPONENT_BITS_WORD_COUNT;
		final int start = id * wordCount;

		return checkNone( bits, start, wordCount )
				& checkOne( bits, start, wordCount )
				& checkAll( bits, start, wordCount );
	}

	/**
	 * Checks if the provided bit set has at none of the bits set in noneSet.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets don't intersect, false otherwise.
	 */
	private final boolean checkNone ( final long[] other, final int otherStart, final int wordCount )
	{
		if ( noneSet == null )
		{
			return true;
		}
		// Reject entity if it has any of 'none' bits.
		return !BitUtil.intersects( noneSet, 0, other, otherStart, wordCount );
	}

	/**
	 * Checks if the provided bit set has at least one of the bits set in oneSet.
	 *
	 * @param bits to check.
	 * @return true if the two bit sets intersect, false otherwise.
	 */
	private final boolean checkOne ( final long[] other, final int otherStart, final int wordCount )
	{
		if ( oneSet == null )
		{
			return true;
		}
		// Reject entity if it has none of 'one' bits.
		return BitUtil.intersects( oneSet, 0, other, otherStart, wordCount );
	}

	/**
	 * Checks if the provided bit set has all of the specified bits in allSet.
	 *
	 * @param bits to check.
	 * @return true if bits has all the set bits in allSet, false otherwise.
	 */
	private final boolean checkAll ( final long[] other, final int otherStart, final int wordCount )
	{
		if ( allSet == null )
		{
			return true;
		}
		/*
		 * If the intersection of the two bit sets is equal to allSet, it means the
		 * passed bit set has all of allSet's bits set. Otherwise, reject the
		 * entity.
		 */
		return BitUtil.intersectionEqual( allSet, 0, other, otherStart, wordCount );
	}

	/**
	 * Returns 'true' if this Aspect can't be interested in an Entity, 'false'
	 * otherwise.
	 *
	 * @return 'true' if this Aspect can't be interested in an Entity, 'false'
	 *         otherwise.
	 */
	public final boolean isEmpty ()
	{
		return (allSet == null && noneSet == null && allSet == null);
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from.
	 *
	 * @return new {@link Builder} instance.
	 */
	public static final Builder builder ()
	{
		return new Aspect.Builder();
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if {@link Builder#all(Class...)}
	 * was called on the {@link Builder}.
	 *
	 * @see Builder#all(Class...)
	 *
	 * @param types required for an entity to be accepted.
	 * @return new Builder instance.
	 */
	@SafeVarargs
	public static final Builder all ( final Class<? extends Component>... types )
	{
		return builder().all( types );
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if
	 * {@link Builder#exclude(Class...)} was called on the {@link Builder}.
	 *
	 * @see Builder#exclude(Class...)
	 *
	 * @param types avoided for an entity to be accepted.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder exclude ( final Class<? extends Component>... types )
	{
		return builder().exclude( types );
	}

	/**
	 * Factory method that returns a new {@link Builder} instance to build
	 * {@link Aspect} instances from. Works as if {@link Builder#one(Class...)}
	 * was called on the {@link Builder}.
	 *
	 * @see Builder#one(Class...)
	 *
	 * @param types any of the types the entity must possess.
	 * @return new {@link Builder} instance.
	 */
	@SafeVarargs
	public static final Builder one ( final Class<? extends Component>... types )
	{
		return builder().one( types );
	}

	/**
	 * Builder class to configure and create Aspects from it.
	 *
	 * @author dustContributor
	 *
	 */
	public static final class Builder
	{
		private final long[] all;
		private final long[] none;
		private final long[] one;

		public Builder ()
		{
			this( DAConstants.COMPONENT_BITS_WORD_COUNT );
		}

		private Builder ( final int wordCount )
		{
			this.all = new long[wordCount];
			this.none = new long[wordCount];
			this.one = new long[wordCount];
		}

		/**
		 * Returns a {@link Builder} where an entity must possess all of the
		 * specified component types.
		 *
		 * @param types a required component types.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder all ( final Class<? extends Component>... types )
		{
			setBits( this.all, types );
			return this;
		}

		/**
		 * Excludes all of the specified component types from the Builder. A system
		 * will not be interested in an entity that possesses one of the specified
		 * exclusion component types.
		 *
		 * @param types component types to exclude.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder exclude ( final Class<? extends Component>... types )
		{
			setBits( this.none, types );
			return this;
		}

		/**
		 * Returns an {@link Builder} where an entity must possess any of the
		 * specified component types.
		 *
		 * @param types any of the types the entity must possess.
		 * @return this {@link Builder} instance.
		 */
		@SafeVarargs
		public final Builder one ( final Class<? extends Component>... types )
		{
			setBits( this.one, types );
			return this;
		}

		@SafeVarargs
		private static final void setBits (
				final long[] bits,
				final Class<? extends Component>... types )
		{
			for ( int i = types.length; i-- > 0; )
			{
				BitUtil.set( bits, ClassIndexer.getIndexFor( types[i], Component.class ) );
			}
		}

		/**
		 * Builds an Aspect based on how this {@link Builder} was configured.
		 *
		 * @return Aspect based on this {@link Builder} configuration.
		 */
		public final Aspect build ()
		{
			return new Aspect( this.all, this.none, this.one );
		}
	}

}
