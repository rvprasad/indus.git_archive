
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

package edu.ksu.cis.indus.common.soot;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;

import soot.jimple.JimpleBody;

import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;


/**
 * This class provides <code>soot.toolkits.graph.TrapUnitGraph</code>s.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TrapStmtGraphFactory
  extends AbstractStmtGraphFactory {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TrapStmtGraphFactory.class);

	/**
	 * @see edu.ksu.cis.indus.common.soot.AbstractStmtGraphFactory#getUnitGraphForBody(soot.Body)
	 */
	protected UnitGraph getUnitGraphForBody(final JimpleBody body) {
		return new TrapUnitGraph(body);
	}

	/**
	 * {@inheritDoc}
	 */
	protected UnitGraph getUnitGraphForMethod(final SootMethod method) {
		UnitGraph _result = null;

		if (method.isConcrete()) {
			_result = new TrapUnitGraph(method.retrieveActiveBody());
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Method " + method + " is not concrete.");
			}
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2004/03/04 11:56:48  venku
   - renamed a method.
   - added a valid empty body into native methods.
   Revision 1.8  2004/01/28 22:41:08  venku
   - added a new method to extract default bodies.
   Revision 1.7  2003/12/31 10:01:05  venku
   - coding convention.
   Revision 1.6  2003/12/31 09:52:30  venku
   - clover directives.
   Revision 1.5  2003/12/31 09:21:32  venku
   - logging level.
   Revision 1.4  2003/12/15 02:07:08  venku
   - logging.
   Revision 1.3  2003/12/13 02:28:53  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.2  2003/12/09 04:42:42  venku
   - unit graph factories are responsible to construct empty
     bodies for methods not BasicBlockGraphMgr.  FIXED.
   Revision 1.1  2003/12/09 04:22:03  venku
   - refactoring.  Separated classes into separate packages.
   - ripple effect.
   Revision 1.9  2003/12/08 10:16:26  venku
   - refactored classes such that the subclasses only provide the
     unit graphs whereas the parent class does the bookkeeping.
   Revision 1.8  2003/12/08 10:03:29  venku
   - changed the logic to obtain the reference, do the check on it,
     and then reinstall it if it had gone bad.
   - formatting.
   Revision 1.7  2003/12/02 09:42:25  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/12/02 01:30:59  venku
   - coding conventions and formatting.
   Revision 1.5  2003/11/28 22:00:20  venku
   - logging.
   Revision 1.4  2003/11/26 06:26:25  venku
   - coding convention.
   Revision 1.3  2003/11/01 23:51:57  venku
   - documentation.
   Revision 1.2  2003/09/28 23:14:03  venku
   - documentation
   Revision 1.1  2003/09/28 11:36:27  venku
   - Added a TrapUnitGraph factory.
 */