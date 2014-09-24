package com.artemis.utils;

/**
 * 
 * Mutable bit field iterator.
 * 
 * You can provide it an array of longs representing bit fields, this iterator
 * will iterate from the first word until the last in the passed array.
 * 
 * You can call {@link #reset()} to reset the iterator to the first word and
 * start again.
 * 
 * You can also set other bit fields whenever you want to with
 * {@link #setBits(long[])} method.
 * 
 * @author dustContributor
 *
 */
public final class MutableBitIterator
{
	private long[] bits;
	private long word;

	private int wordi;
	private int offset;
	
	public MutableBitIterator ()
	{
		// Empty.
	}

	public MutableBitIterator ( long[] bits )
	{
		this.setBits( bits );
	}
	
	public void setBits ( long[] bits )
	{
		this.bits = bits;
		reset();
	}

	/**
	 * Resets the position of this iterator to 0.
	 */
	public void reset ()
	{
		this.wordi = 0;
		this.word = bits[0];
		this.offset = 0;
	}

	/**
	 * Returns the next set bit of the bits in this iterator.
	 * 
	 * @return the index of the next set bit if there is one, -1 otherwise.
	 */
	public int nextSetBit ()
	{
		while ( word == 0 )
		{
			// If no words left.
			if ( ++wordi >= bits.length )
			{
				return -1;
			}
			// Store word.
			word = bits[wordi];
			// Increase offset.
			offset = wordi * 64;
		}
		// Word and anti word.
		long val = word & -word;
		// Flip bit.
		word ^= val;
		// Return offset + next set bit of word.
		return offset + Long.bitCount( val - 1 );
	}
}
