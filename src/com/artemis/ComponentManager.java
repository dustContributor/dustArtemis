package com.artemis;

import java.util.Arrays;
import java.util.stream.StreamSupport;

import org.apache.lucene.util.BitUtil;
import org.apache.lucene.util.OpenBitSet;

import com.artemis.utils.Bag;
import com.artemis.utils.ImmutableIntBag;
import com.artemis.utils.IntBag;
import com.artemis.utils.MutableBitIterator;

/**
 * @author Arni Arent
 * @author dustContributor
 */
public final class ComponentManager {
	/* 64 bit words per entity. */
	private final int wordsPerEntity;
	/* Class hash codes for each component type. */
	private final int[] componentHashCodes;
	/** Component bags by type index. */
	private final ComponentHandler<Component>[] componentHandlers;
	/** Component bits for all entities. */
	private final OpenBitSet componentBits;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	ComponentManager(final Iterable<Class<? extends Component>> componentTypes) {
		// Get array of component types. Essentially a defensive copy.
		var types = StreamSupport.stream(componentTypes.spliterator(), false).toArray(Class[]::new);
		// Get a sorted hash code array from it.
		var componentHashCodes = Arrays.stream(types).mapToInt(ComponentManager::typeHashCode).sorted().toArray();

		if (Arrays.stream(componentHashCodes).distinct().count() != componentHashCodes.length) {
			/*
			 * There is really no recovery from this, if the JVM returns the same hashCode
			 * for two different Class objects, we're screwed.
			 */
			throw new DustException(this,
					"Found a duplicate hashCode for different elements in 'componentTypes'!"
							+ " dustArtemis can't work if this happens, it needs different hash codes"
							+ " for different component Class objects.");
		}

		// Sort the component types by hash code.
		Arrays.sort(types, (a, b) -> Integer.compare(typeHashCode(a), typeHashCode(b)));
		// Fetch the constant.
		int wordsPerEntity = computeWordsPerEntity(types.length);

		// Init bitsets for all entities.
		int bitCount = (DAConstants.APPROX_LIVE_ENTITIES * wordsPerEntity) * 64;
		var componentBits = new OpenBitSet(bitCount);

		// Reasonable initial capacity.
		int handlerCap = Math.max(32, DAConstants.APPROX_LIVE_ENTITIES / 2);

		var componentHandlers = new ComponentHandler[types.length];
		// Init all the component handlers.
		for (int i = 0; i < componentHandlers.length; ++i) {
			componentHandlers[i] = new ArrayComponentHandler(types[i], componentBits, wordsPerEntity, i, handlerCap);
		}

		// Now keep everything we need.
		this.componentHashCodes = componentHashCodes;
		this.componentHandlers = componentHandlers;
		this.wordsPerEntity = wordsPerEntity;
		this.componentBits = componentBits;
	}

	private static final int computeWordsPerEntity(final int componentTypeCount) {
		// Enforce a positive minimum so not to screw up the math.
		int itmp = Math.max(componentTypeCount, 1);
		// Now get how many 64 bit words do we need.
		return ((itmp - 1) / 64) + 1;
	}

	/**
	 * @param id   of the entity to fetch all its components from.
	 * @param dest bag to store all the components.
	 * @return bag passed as 'dest' parameter.
	 */
	public final Bag<Component> getComponentsFor(final int id, final Bag<Component> dest) {
		var cmpBags = componentHandlers;
		var it = new MutableBitIterator(componentBits());
		int start = id * wordsPerEntity;
		int end = start + wordsPerEntity;
		// Implementation detail, bit set uses 64 bit words.
		int cmpBitOffset = start * 64;
		// Start from the entity's bits.
		it.selectWord(start);

		int cmp;

		while ((cmp = it.nextSetBit(end) - cmpBitOffset) > -1 && cmp < cmpBags.length) {
			dest.add(cmpBags[cmp].get(id));
		}

		return dest;
	}

	/**
	 * Either creates or retrieves a component handler for the passed component
	 * type.
	 *
	 * @param type of the component to create or retrieve a handler from.
	 * @return handler of the component type.
	 */
	@SuppressWarnings("unchecked")
	public final <T extends Component> ComponentHandler<T> getHandlerFor(final Class<T> type) {
		return (ComponentHandler<T>) componentHandlers[indexFor(type)];
	}

	final void registerEntity(final int eid) {
		int index = eid * wordsPerEntity;
		if (index >= componentBits.length()) {
			componentBits.resizeWords(index + 1);
		}
	}

	final long[] componentBits() {
		return componentBits.getBits();
	}

	final void markChanges() {
		var handlers = componentHandlers;
		int size = handlers.length;
		for (int i = 0; i < size; ++i) {
			handlers[i].markChanges();
		}
	}

	final void cleanup(final ImmutableIntBag deleted) {
		// Let handlers clean removed components.
		cleanupHandlers();
		// And clean all components from deleted entities.
		cleanupComponents(((IntBag) deleted).data(), deleted.size());
	}

	private final void cleanupHandlers() {
		var handlers = componentHandlers;
		int size = handlers.length;
		for (int i = 0; i < size; ++i) {
			handlers[i].cleanup();
		}
	}

	private final void cleanupComponents(final int[] ents, final int size) {
		var handlers = componentHandlers;
		var bits = componentBits();
		var it = new MutableBitIterator(componentBits());
		int wordCount = wordsPerEntity;

		for (int i = size; i-- > 0;) {
			int eid = ents[i];
			int start = eid * wordCount;
			int end = start + wordCount;
			// Implementation detail, bit set uses 64 bit words.
			int cmpBitOffset = start * 64;
			// Now iterate over normal component bits and remove them.
			it.selectWord(start);

			int cmp;

			while ((cmp = it.nextSetBit(end) - cmpBitOffset) > -1 && cmp < handlers.length) {
				handlers[cmp].delete(eid);
			}

			// Now clear all component bits from the entity.
			BitUtil.clearWords(bits, start, end);
		}
	}

	/**
	 * Searches for the index of the passed component type, based on its hash code.
	 *
	 * @param type of the component.
	 * @return index of the component type, or -1 if it wasn't found.
	 */
	final int indexFor(final Class<? extends Component> type) {
		return Arrays.binarySearch(componentHashCodes, typeHashCode(type));
	}

	/**
	 * @return how many 64 bit words an entity requires to have its components
	 *         tracked.
	 */
	final int wordsPerEntity() {
		return wordsPerEntity;
	}

	private static final int typeHashCode(Class<?> c) {
		// Let the caller handle the nulls
		return c == null ? 0 : c.getTypeName().hashCode();
	}
}
