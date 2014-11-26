package org.apache.lucene.util;

public class FixedBitSet128 extends FixedBitSet64
{
	public long word1;

	public FixedBitSet128 ()
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
			default:
				return;
		}
	}

	@Override
	public boolean intersects ( FixedBitSet bits )
	{
		return intersects( (FixedBitSet128) bits );
	}

	@Override
	public void and ( FixedBitSet bits )
	{
		and( (FixedBitSet128) bits );
	}

	@Override
	public void or ( FixedBitSet bits )
	{
		or( (FixedBitSet128) bits );
	}

	@Override
	public void andNot ( FixedBitSet bits )
	{
		andNot( (FixedBitSet128) bits );
	}

	@Override
	public int intersectCount ( FixedBitSet bits )
	{
		return intersectCount( (FixedBitSet128) bits );
	}

	public final boolean intersects ( FixedBitSet128 bits )
	{
		return super.intersects( bits ) && (bits.word1 & word1) != 0L;
	}

	public final void or ( FixedBitSet128 bits )
	{
		super.or( bits );
		word1 |= bits.word1;
	}

	public final void and ( FixedBitSet128 bits )
	{
		super.and( bits );
		word1 &= bits.word1;
	}

	public final void andNot ( FixedBitSet128 bits )
	{
		super.andNot( bits );
		word1 &= ~bits.word1;
	}

	public final int intersectCount ( FixedBitSet128 bits )
	{
		return super.intersectCount( bits ) + Long.bitCount( bits.word1 & word1 );
	}

	@Override
	public void clear ()
	{
		super.clear();
		word1 = 0L;
	}

	@Override
	public int capacity ()
	{
		return 128;
	}
	
	@Override
	public int size ()
	{
		return 2;
	}

	@Override
	public int cardinality ()
	{
		return super.cardinality() + Long.bitCount( word1 );
	}
	
	public static class BitIterator
	{
		
	}

}
