
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

package edu.ksu.cis.indus.staticanalyses.tokens;

import edu.ksu.cis.indus.interfaces.AbstractPrototype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This class realizes a token manager that represents tokens asis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CollectionTokenManager
  extends AbstractTokenManager {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(CollectionTokenManager.class);

	/** 
	 * The mapping between types to the type based filter.
	 *
	 * @invariant type2filter.oclIsKindOf(Map(IType, ITokenFilter))
	 */
	private final Map type2filter = new HashMap();

	/**
	 * Creates an instacne of this class.
	 *
	 * @param typeManager to be used.
	 *
	 * @pre typeManager != null
	 */
	public CollectionTokenManager(final ITypeManager typeManager) {
		super(typeManager);
	}

	/**
	 * This class represents a token filter based on collection of tokens.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CollectionTokenFilter
	  implements ITokenFilter {
		/** 
		 * The type associated with the filter.
		 */
		private final IType filterType;

		/**
		 * Creates an instance of this class.
		 *
		 * @param type is the type used to filter.
		 *
		 * @pre type != null
		 */
		CollectionTokenFilter(final IType type) {
			filterType = type;
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenFilter#filter(ITokens)
		 */
		public ITokens filter(final ITokens tokens) {
			final Collection _filterate = new ArrayList();

			for (final Iterator _i = tokens.getValues().iterator(); _i.hasNext();) {
				final Object _value = _i.next();

				if (typeMgr.getAllTypes(_value).contains(filterType)) {
					_filterate.add(_value);
				}
			}
			return getTokens(_filterate);
		}
	}


	/**
	 * This class represents a collection of tokens represented asis.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private class CollectionTokens
	  extends AbstractPrototype
	  implements ITokens {
		/** 
		 * The collection of values.
		 *
		 * @invariant values != null
		 */
		private Collection values;

		/**
		 * Creates a new instance of this class.
		 *
		 * @param initValues are the values to be put into this instnace.
		 *
		 * @pre initValues != null
		 */
		CollectionTokens(final Collection initValues) {
			values = new HashSet(initValues);
		}

		/**
		 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone()
		 */
		public Object getClone() {
			return new CollectionTokens(values);
		}

		/**
		 * @see ITokens#isEmpty()
		 */
		public boolean isEmpty() {
			return values.isEmpty();
		}

		/**
		 * @see ITokens#getValues()
		 */
		public Collection getValues() {
			return Collections.unmodifiableCollection(values);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#addTokens(ITokens)
		 */
		public void addTokens(final ITokens newTokens) {
			values.addAll(((CollectionTokens) newTokens).values);
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#clear()
		 */
		public void clear() {
			values.clear();
		}

		/**
		 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokens#diffTokens(ITokens)
		 */
		public ITokens diffTokens(final ITokens tokens) {
			return new CollectionTokens(CollectionUtils.subtract(values, ((CollectionTokens) tokens).values));
		}
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getNewTokenSet()
	 */
	public ITokens getNewTokenSet() {
		return new CollectionTokens(Collections.EMPTY_LIST);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTokens(java.util.Collection)
	 */
	public ITokens getTokens(final Collection values) {
		return new CollectionTokens(values);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#getTypeBasedFilter(IType)
	 */
	public ITokenFilter getTypeBasedFilter(final IType type) {
		ITokenFilter _result = (ITokenFilter) type2filter.get(type);

		if (_result == null) {
			_result = new CollectionTokenFilter(type);
			type2filter.put(type, _result);
		}

		return _result;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.tokens.ITokenManager#reset()
	 */
	public void reset() {
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/05/20 07:29:41  venku
   - optimized the token set to be optimal when created.
   - added new method to retrieve empty token sets (getNewTokenSet()).
   Revision 1.1  2004/04/16 20:10:39  venku
   - refactoring
    - enabled bit-encoding support in indus.
    - ripple effect.
    - moved classes to related packages.
 */
