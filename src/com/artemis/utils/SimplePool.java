package com.artemis.utils;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class SimplePool<T>
{
	private final Bag<T> store;
	private final Supplier<T> supplier;
	private final Consumer<T> resetter;

	public SimplePool ( Class<T> type, Supplier<T> supplier, Consumer<T> resetter )
	{
		this.store = new Bag<>( type, 128 );
		this.supplier = supplier;
		this.resetter = resetter;
	}

	public T get ()
	{
		if ( store.size() > 0 )
		{
			final T cmp = store.removeLastUnsafe();
			resetter.accept( cmp );
			return cmp;
		}

		return supplier.get();
	}

	public void store ( T item )
	{
		store.add( item );
	}
	
	public void storeAll ( T[] items, int size )
	{
		store.addAll( items, size );
	}
}
