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

package edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.flow;

import edu.ksu.cis.indus.interfaces.IPrototype;
import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.staticanalyses.flow.AbstractIndexManager;
import edu.ksu.cis.indus.staticanalyses.flow.modes.sensitive.OneContextInfoIndex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ValueBox;

/**
 * This class manages indices associated with entities in flow sensitive mode. In reality, it provides the implementation to
 * create new indices.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 * @param <E> is the type of the entity that has been indexed.

 */
public class FlowSensitiveIndexManager<E>
		extends AbstractIndexManager<OneContextInfoIndex<E, ValueBox>, E>
		implements IPrototype<FlowSensitiveIndexManager<E>> {

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(FlowSensitiveIndexManager.class);

	/**
	 * Returns an index corresponding to the given entity and context. The index is dependent on the program point stored in
	 * the context.
	 *
	 * @param o the entity for which the index in required.
	 * @param c the context which captures program point needed to generate the index.
	 * @return the index that uniquely identifies <code>o</code> at the program point captured in <code>c</code>.
	 * @pre o != null and c != null
	 */
	@Override protected OneContextInfoIndex<E, ValueBox> createIndex(final E o, final Context c) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Getting index for " + o + " in " + c);
		}

		return new OneContextInfoIndex<E, ValueBox>(o, c.getProgramPoint());
	}

	/**
	 * @see edu.ksu.cis.indus.interfaces.IPrototype#getClone(java.lang.Object[])
	 */
	public FlowSensitiveIndexManager<E> getClone(Object... o) {
		return new FlowSensitiveIndexManager<E>();
	}
}

// End of File
