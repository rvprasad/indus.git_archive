
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common;

import java.util.ListIterator;

import org.apache.commons.collections.Predicate;

import org.apache.commons.collections.iterators.AbstractListIteratorDecorator;


/**
 * This class provides filtered iteration of a list. Like <code>FilteredCollection</code>, all operations are filtered.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FilteredListIterator
  extends AbstractListIteratorDecorator
  implements ListIterator {
	/**
	 * The predicate that defines the filtering criterion.
	 *
	 * @invariant predicate != null
	 */
	private final Predicate predicate;

	/**
	 * Creates an instance of this class.
	 *
	 * @param theIterator to be wrapped.
	 * @param thePredicate that defines the filtering criterion.
	 *
	 * @pre theIterator != null and predicate != null
	 */
	public FilteredListIterator(final ListIterator theIterator, final Predicate thePredicate) {
		super(theIterator);
		predicate = thePredicate;
	}

	/**
	 * @see java.util.ListIterator#add(java.lang.Object)
	 */
	public void add(final Object object) {
		if (predicate.evaluate(object)) {
			super.add(object);
		}
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		boolean _result = false;

		while (super.hasNext()) {
			final Object _nextElement = iterator.next();

			if (predicate.evaluate(_nextElement)) {
				_result = true;
				iterator.previous();
				break;
			}
		}
		return _result;
	}

	/**
	 * @see java.util.ListIterator#hasPrevious()
	 */
	public boolean hasPrevious() {
		boolean _result = false;

		while (super.hasPrevious()) {
			final Object _prevElement = iterator.previous();

			if (predicate.evaluate(_prevElement)) {
				_result = true;
				iterator.next();
				break;
			}
		}
		return _result;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		Object _nextElement;

		do {
			_nextElement = super.next();
		} while (!predicate.evaluate(_nextElement));
		return _nextElement;
	}

	/**
	 * @see java.util.ListIterator#previous()
	 */
	public Object previous() {
		Object _prevElement;

		do {
			_prevElement = super.previous();
		} while (!predicate.evaluate(_prevElement));
		return _prevElement;
	}

	/**
	 * @see java.util.ListIterator#set(java.lang.Object)
	 */
	public void set(final Object object) {
		if (predicate.evaluate(object)) {
			super.set(object);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2004/06/28 08:08:27  venku
   - new collections classes for filtered access and update.
 */