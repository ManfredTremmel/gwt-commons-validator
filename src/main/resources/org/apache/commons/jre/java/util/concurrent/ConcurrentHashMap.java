package java.util.concurrent;

import java.util.HashMap;

public class ConcurrentHashMap<K, V> extends HashMap<K, V> {
	private static final long serialVersionUID = -6255993066795301169L;

	public ConcurrentHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	public ConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }

	public ConcurrentHashMap() {
        super();
    }
}
