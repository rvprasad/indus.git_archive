
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

package edu.ksu.cis.indus;

import junit.framework.TestCase;

/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class ErringTestCase
  extends TestCase {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	final String msg;

	/**
	 * Creates a new ErringTestCase object.
	 *
	 * @param message DOCUMENT ME!
	 */
	public ErringTestCase(final String message) {
		msg = message;
	}

	/**
	 * DOCUMENT ME! <p></p>
	 */
	public void testFail() {
		throw new IllegalStateException(msg);
	}
}

/*
   ChangeLog:
   $Log$
 */
