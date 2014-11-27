package com.artemis.utils;

import org.apache.lucene.util.FixedBitSet;

public final class FixedBitIterator
{
	private FixedBitSet bits;
	private long word;

	private int wordi;
	private int offset;

	public FixedBitIterator ()
	{
		// Empty.
	}

	public FixedBitIterator ( FixedBitSet bits )
	{
		this.setBits( bits );
	}

	public final void setBits ( FixedBitSet bits )
	{
		this.bits = bits;
		reset();
	}

	/**
	 * Resets the position of this iterator to 0.
	 */
	public final void reset ()
	{
		this.word = bits.getWord( 0 );
		this.wordi = 0;
		this.offset = 0;
	}

	/**
	 * Returns the next set bit of the bits in this iterator.
	 * 
	 * @return the index of the next set bit if there is one, -1 otherwise.
	 */
	public final int nextSetBit ()
	{
		while ( word == 0 )
		{
			// If no words left.
			if ( ++wordi >= bits.size() )
			{
				return -1;
			}
			// Store word.
			word = bits.getWord( wordi );
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
