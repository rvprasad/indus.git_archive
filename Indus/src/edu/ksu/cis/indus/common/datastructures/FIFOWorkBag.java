
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

/**
 * This is a First-in-First-out implementation of the workbag.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class FIFOWorkBag
  extends AbstractWorkBag {
	/**
	 * @see edu.ksu.cis.indus.common.datastructures.IWorkBag#addWork(java.lang.Object)
	 */
	public void addWork(final Object o) {
		container.add(o);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.

 */