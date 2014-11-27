package org.apache.lucene.util;

public abstract class FixedBitSet
{
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

	public abstract void clear ();

	public abstract int cardinality ();

	public abstract int capacity ();

	public abstract int size ();

	public abstract boolean isEmpty ();
	
	public abstract long getWord( int index);

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
