package org.apache.lucene.util;

public abstract class FixedBitSet
{
	public final boolean get ( int index )
	{
		return getOnWord( index / 64, 1L << index );
	}

	public final int getBit ( int index )
	{
		return getBitOnWord( index / 64, index );
	}

	public final void set ( int index )
	{
		setOnWord( index / 64, 1L << index );
	}

	public final void clear ( int index )
	{
		clearOnWord( index / 64, 1L << index );
	}

	protected abstract boolean getOnWord ( int word, long msk );

	protected abstract int getBitOnWord ( int word, int index );

	protected abstract void setOnWord ( int word, long msk );

	protected abstract void clearOnWord ( int word, long msk );

	public abstract boolean intersects ( FixedBitSet bits );

	public abstract int intersectCount ( FixedBitSet bits );

	public abstract void and ( FixedBitSet bits );

	public abstract void or ( FixedBitSet bits );

	public abstract void andNot ( FixedBitSet bits );

	public abstract void clear ();

	public abstract int cardinality ();

	public abstract int capacity ();
	
	public abstract int size();
}
