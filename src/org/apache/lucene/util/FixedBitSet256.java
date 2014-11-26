package org.apache.lucene.util;

public class FixedBitSet256 extends FixedBitSet192
{
	public long word3;

	public FixedBitSet256 ()
	{
		// Empty.
	}

	@Override
	protected boolean getOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				return (word0 & msk) != 0L;
			case 1:
				return (word1 & msk) != 0L;
			case 2:
				return (word2 & msk) != 0L;
			case 3:
				return (word3 & msk) != 0L;
			default:
				return false;
		}
	}

	@Override
	protected int getBitOnWord ( int word, int index )
	{
		switch (word)
		{
			case 0:
				return (int) ((word0 >>> index) & 0x01L);
			case 1:
				return (int) ((word1 >>> index) & 0x01L);
			case 2:
				return (int) ((word2 >>> index) & 0x01L);
			case 3:
				return (int) ((word3 >>> index) & 0x01L);
			default:
				return 0;
		}
	}

	@Override
	protected void clearOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				word0 &= ~msk;
				return;
			case 1:
				word1 &= ~msk;
				return;
			case 2:
				word2 &= ~msk;
				return;
			case 3:
				word3 &= ~msk;
				return;
			default:
				return;
		}
	}

	@Override
	protected void setOnWord ( int word, long msk )
	{
		switch (word)
		{
			case 0:
				word0 |= msk;
				return;
			case 1:
				word1 |= msk;
				return;
			case 2:
				word2 |= msk;
				return;
			case 3:
				word3 |= msk;
				return;
			default:
				return;
		}
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return intersects( (FixedBitSet256) bits );
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		and( (FixedBitSet256) bits );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		or( (FixedBitSet256) bits );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		andNot( (FixedBitSet256) bits );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return intersectCount( (FixedBitSet256) bits );
	}

	public final boolean intersects ( FixedBitSet256 bits )
	{
		return super.intersects( bits ) && (bits.word3 & word3) != 0L;
	}

	public final void or ( FixedBitSet256 bits )
	{
		super.or( bits );
		word3 |= bits.word3;
	}

	public final void and ( FixedBitSet256 bits )
	{
		super.and( bits );
		word3 &= bits.word3;
	}

	public final void andNot ( FixedBitSet256 bits )
	{
		super.andNot( bits );
		word3 &= ~bits.word3;
	}

	public final int intersectCount ( FixedBitSet256 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word3 & word3 );
	}

	@Override
	public void clear ()
	{
		super.clear();
		word3 = 0L;
	}

	@Override
	public int capacity ()
	{
		return 256;
	}
	
	@Override
	public int size ()
	{
		return 4;
	}

	@Override
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word3 );
	}
}
