/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
 *
 * This software is licensed under the KSU Open Academic License.
 * You should have received a copy of the license with the distribution.
 * A copy can be found at
 *     http://www.cis.ksu.edu/santos/license.html
 * or you can contact the lab at:
 *     SAnToS Laboratory
 *     234 Nichols Hall
 *     Manhattan, KS 66506, USA
 */
package edu.ksu.cis.indus.common.collections;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * DOCUMENT ME!
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class MapUtils {

	/**
	 * DOCUMENT ME!
	 */
	public static final IFactory<Map> MAP_FACTORY = new IFactory<Map>() {

		public Map create() {
			return new HashMap();
		}

	};

	// / CLOVER:OFF
	/**
	 * Creates an instance of this class.
	 */
	private MapUtils() {
		super();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @return DOCUMENT ME!
	 */
	public static <K, V> IFactory<Map<K, V>> getFactory() {
		return (IFactory<Map<K, V>>) MAP_FACTORY;
	}

	// / CLOVER:ON

	/**
	 * DOCUMENT ME!
	 * 
	 * @param map DOCUMENT ME!
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param <T4> DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2, T3 extends T1> T2 getFromMap(final Map<T1, T2> map, final T3 key, final T2 defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		map.put(key, defaultValue);
		return defaultValue;
	}

	/**
	 * Retrieves the value for a key from the map. If the key has no mapping, a new mapping from the key to the given default
	 * value is inserted into the map.
	 * 
	 * @param <T1> The type of keys in the input map.
	 * @param <T2> The type of values in the input map.
	 * @param <T3> The type of the key.
	 * @param map to be read.
	 * @param key for which the value should be retrieved.
	 * @param factory used to create the default value.
	 * @return the value mapped to <code>key</code>.
	 * @pre map != null and key != null and factory != null
	 * @post map.get(key) != null
	 * @post result != null
	 */
	public static <T1, T2, T3 extends T1> T2 getFromMapUsingFactory(final Map<T1, T2> map, final T3 key,
			final IFactory<? extends T2> factory) {
		if (!map.containsKey(key)) {
			map.put(key, factory.create());
		}
		return map.get(key);
	}

	/**
	 * Inverts the given map. In the returned map, each key is mapped to a collection. This is to address situation where
	 * multiple keys may map to the same value in the given map.
	 * 
	 * @param <T1> the type of the keys in the given map.
	 * @param <T2> the type of the elements in the given map.
	 * @param map to be inverted.
	 * @return the inverted map.
	 * @pre map != null
	 * @post result != null
	 * @post map.values().containsAll(result.keySet())
	 * @post result.keySet().containsAll(map.values())
	 * @post result.values()->forall(o | map.keySet().containsAll(o))
	 * @post map.keySet()->forall(o | result.values()->exists(p | p.contains(o)))
	 */
	public static <T1, T2> Map<T2, Set<T1>> invertMap(final Map<T1, T2> map) {
		final Map<T2, Set<T1>> _result = new HashMap<T2, Set<T1>>();
		final Collection<T1> _values = map.keySet();
		final Iterator<T1> _i = _values.iterator();
		final int _iEnd = _values.size();

		for (int _iIndex = 0; _iIndex < _iEnd; _iIndex++) {
			final T1 _valueInResult = _i.next();
			final T2 _keyInResult = map.get(_valueInResult);
			if (!_result.containsKey(_keyInResult)) {
				final Set<T1> _set = new HashSet<T1>();
				_result.put(_keyInResult, _set);
			}
			final Collection<T1> _t = _result.get(_keyInResult);
			_t.add(_valueInResult);
		}
		return _result;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T4> DOCUMENT ME!
	 * @param map DOCUMENT ME!
	 * @param key DOCUMENT ME!
	 * @param values DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T3 extends T1, T2, T4 extends T2> boolean putAllIntoCollectionInMap(final Map<T1, Collection<T2>> map,
			final T3 key, final Collection<T4> values, final Collection<T2> col) {
		Collection<T2> _temp = map.get(key);

		if (_temp == null) {
			_temp = col;
			map.put(key, _temp);
		}
		return _temp.addAll(values);
	}

	/**
	 * Puts all values in <code>values</code> into the value of the given key in the given map . If no collection exists
	 * against the given key, the given collection is installed as the value for the given key and the values are loaded into
	 * it.
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T4> DOCUMENT ME!
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param values to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create the collection if there is no collection mapped to <code>key</code>.
	 * @return <code>true</code> if all values were added; <code>false</code>, otherwise.
	 * @pre map != null and key != null and values != null and factory != null
	 */
	public static <T1, T3 extends T1, T2, T4 extends T2> boolean putAllIntoCollectionInMapUsingFactory(final Map<T1, Collection<T2>> map,
			final T3 key, final Collection<T4> values, final IFactory<? extends Collection<T2>> factory) {
		Collection<T2> _temp = map.get(key);

		if (_temp == null) {
			_temp = factory.create();
			map.put(key, _temp);
		}
		return _temp.addAll(values);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param <T4> DOCUMENT ME!
	 * @param map DOCUMENT ME!
	 * @param key DOCUMENT ME!
	 * @param value DOCUMENT ME!
	 * @param col DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2, T3 extends T1, T4 extends T2> boolean putIntoCollectionInMap(final Map<T1, Collection<T2>> map,
			final T3 key, final T4 value, final Collection<T2> col) {
		Collection<T2> _temp = map.get(key);

		if (_temp == null) {
			_temp = col;
			map.put(key, _temp);
		}
		return _temp.add(value);
	}

	/**
	 * Puts <code>value</code> into the value of the given key in the given map . If no collection exists against the given
	 * key, the given collection is installed as the value for the given key and <code>value</code> is inserted into it.
	 * 
	 * @param <T1> The type of the keys in the given map.
	 * @param <T2> The type of the values in collection stored as values in the given map.
	 * @param <T3> The type of the given key.
	 * @param <T4> The type of the given value.
	 * @param map to be altered.
	 * @param key is the key in the map that should be altered or populated.
	 * @param value to be added into the collection mapped to <code>key</code>.
	 * @param factory to be used to create a collection if there is no collection mapped to <code>key</code>.
	 * @return <code>true</code> if the value was added; <code>false</code>, otherwise.
	 * @pre map != null and key != null and values != null and factory != null
	 */
	public static <T1, T2, T3 extends T1, T4 extends T2> boolean putIntoCollectionInMapUsingFactory(
			final Map<T1, Collection<T2>> map, final T3 key, final T4 value, final IFactory<? extends Collection<T2>> factory) {
		Collection<T2> _temp = map.get(key);

		if (_temp == null) {
			_temp = factory.create();
			map.put(key, _temp);
		}
		return _temp.add(value);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param map DOCUMENT ME!
	 * @param key DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2, T3 extends T1> T2 queryObject(final Map<T1, T2> map, final T3 key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return null;

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param <T3> DOCUMENT ME!
	 * @param <T4> DOCUMENT ME!
	 * @param map DOCUMENT ME!
	 * @param key DOCUMENT ME!
	 * @param defaultValue DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2, T3 extends T1, T4 extends T2> T2 queryObject(final Map<T1, T2> map, final T3 key,
			final T4 defaultValue) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return defaultValue;

	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param <T1> DOCUMENT ME!
	 * @param <T2> DOCUMENT ME!
	 * @param header DOCUMENT ME!
	 * @param map DOCUMENT ME!
	 * @return DOCUMENT ME!
	 */
	public static <T1, T2> String verbosePrint(final String header, final Map<T1, T2> map) {
		// TODO:

		return "fix me";
	}
}

// End of File