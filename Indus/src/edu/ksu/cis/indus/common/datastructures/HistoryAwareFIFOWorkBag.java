
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

package edu.ksu.cis.indus.common.datastructures;

import java.util.Collection;


/**
 * This is a First-in-First-out implementation of the workbag that can remember previous work pieces put into it.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class HistoryAwareFIFOWorkBag
  extends HistoryAwareAbstractWorkBag {
	/**
	 * Creates a new FIFOWorkBag object.
	 *
	 * @param processed is the collection to be used to remember work pieces put into the bag.  Refer to
	 * 		  <code>HistoryAwareAbstractWorkBag#HistoryAwareAbstractWorkBag(Collection)</code>.
	 */
	public HistoryAwareFIFOWorkBag(final Collection processed) {
		super(processed);
	}

	/**
	 * @see edu.ksu.cis.indus.common.datastructures.HistoryAwareAbstractWorkBag#subAddWork(java.lang.Object)
	 */
	protected void subAddWork(Object o) {
		container.add(o);
	}
}

/*
   ChangeLog:
   $Log$
 */