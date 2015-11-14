package org.apache.lucene.util;

public abstract class FixedBitSet
{
	private final byte size;
	private final short capacity;

	protected FixedBitSet ( final int size )
	{
		// Max of 255 words.
		this.size = (byte) size;
		// 64 bits per word.
		this.capacity = (short) (size * 64);
	}

	public abstract boolean get ( int index );

	public abstract int getBit ( int index );

	public abstract void set ( int index );

	public abstract void clear ( int index );

	public abstract boolean intersects ( FixedBitSet bits );

	protected abstract boolean intersectsTo ( FixedBitSet64 bits );

	protected abstract boolean intersectsTo ( FixedBitSet128 bits );

	protected abstract boolean intersectsTo ( FixedBitSet192 bits );

	protected abstract boolean intersectsTo ( FixedBitSet256 bits );

	public abstract int intersectCount ( FixedBitSet bits );

	protected abstract int intersectCountTo ( FixedBitSet64 bits );

	protected abstract int intersectCountTo ( FixedBitSet128 bits );

	protected abstract int intersectCountTo ( FixedBitSet192 bits );

	protected abstract int intersectCountTo ( FixedBitSet256 bits );

	public abstract void and ( FixedBitSet bits );

	protected abstract void andTo ( FixedBitSet64 bits );

	protected abstract void andTo ( FixedBitSet128 bits );

	protected abstract void andTo ( FixedBitSet192 bits );

	protected abstract void andTo ( FixedBitSet256 bits );

	public abstract void or ( FixedBitSet bits );

	protected abstract void orTo ( FixedBitSet64 bits );

	protected abstract void orTo ( FixedBitSet128 bits );

	protected abstract void orTo ( FixedBitSet192 bits );

	protected abstract void orTo ( FixedBitSet256 bits );

	public abstract void andNot ( FixedBitSet bits );

	protected abstract void andNotTo ( FixedBitSet64 bits );

	protected abstract void andNotTo ( FixedBitSet128 bits );

	protected abstract void andNotTo ( FixedBitSet192 bits );

	protected abstract void andNotTo ( FixedBitSet256 bits );

	public abstract boolean isIntersectionEqual ( FixedBitSet bits );

	protected abstract boolean isIntersectionEqualTo ( FixedBitSet64 bits );

	protected abstract boolean isIntersectionEqualTo ( FixedBitSet128 bits );

	protected abstract boolean isIntersectionEqualTo ( FixedBitSet192 bits );

	protected abstract boolean isIntersectionEqualTo ( FixedBitSet256 bits );

	public abstract void copyFrom ( FixedBitSet bits );

	protected abstract void copyTo ( FixedBitSet64 bits );

	protected abstract void copyTo ( FixedBitSet128 bits );

	protected abstract void copyTo ( FixedBitSet192 bits );

	protected abstract void copyTo ( FixedBitSet256 bits );

	/**
	 * Sets all bit values to {@literal false}.
	 */
	public abstract void clear ();

	/**
	 * Sets all bit values to {@literal true}.
	 */
	public abstract void fill ();

	public abstract int cardinality ();

	public abstract boolean isEmpty ();

	public abstract long getWord ( int index );

	/**
	 * Returns the word count that this fixed bit set can store.
	 * 
	 * @return integer representing the word count of this bit set.
	 */
	public final int size ()
	{
		return Byte.toUnsignedInt( size );
	}

	/**
	 * Returns the amount of bits that this fixed bit set can store.
	 * 
	 * @return integer representing the amount of bits this bit set can store.
	 */
	public final int capacity ()
	{
		return Short.toUnsignedInt( capacity );
	}

	public static final FixedBitSet newBitSetByWords ( final int words )
	{
		switch (words)
		{
			case 1:
				return new FixedBitSet64();
			case 2:
				return new FixedBitSet128();
			case 3:
				return new FixedBitSet192();
			case 4:
				return new FixedBitSet256();
			default:
				return null;
		}
	}
}
