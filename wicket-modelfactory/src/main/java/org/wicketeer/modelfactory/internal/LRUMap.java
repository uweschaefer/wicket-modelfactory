package org.wicketeer.modelfactory.internal;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUMap<K, V> extends LinkedHashMap<K, V>
{

    protected static final float DEFAULT_LOAD_FACTOR = (float) 0.75;
    protected static final int DEFAULT_INITIAL_CAPACITY = 5000;
    private static final long serialVersionUID = -9179676638408888162L;

    private int maximumSize;

    public LRUMap(final int maximumSize)
    {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, true, maximumSize);
    }

    public LRUMap(final int maximumSize, final boolean accessOrder)
    {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, accessOrder, maximumSize);
    }

    public LRUMap(final int initialCapacity, final float loadFactor, final boolean accessOrder, final int maximumSize)
    {
        super(initialCapacity, loadFactor, accessOrder);
        this.maximumSize = maximumSize;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K, V> eldest)
    {
        return size() > maximumSize;
    }
}