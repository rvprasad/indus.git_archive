
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

import soot.SootMethod;

import soot.toolkits.graph.TrapUnitGraph;
import soot.toolkits.graph.UnitGraph;

import edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.ref.WeakReference;


/**
 * This class provides <code>soot.toolkits.graph.TrapUnitGraph</code>s.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class TrapUnitGraphFactory
  extends AbstractUnitGraphFactory {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(TrapUnitGraphFactory.class);

	/**
	 * @post method.isConcrete() implies result != null and result.oclIsKindOf(TrapUnitGraph)
	 *
	 * @see edu.ksu.cis.indus.interfaces.AbstractUnitGraphFactory#getUnitGraph(soot.SootMethod)
	 */
	public UnitGraph getUnitGraph(final SootMethod method) {
		WeakReference ref = (WeakReference) method2UnitGraph.get(method);
		UnitGraph result = null;

		if (ref == null || ref.get() == null) {
			if (method.isConcrete()) {
				result = new TrapUnitGraph(method.retrieveActiveBody());
				method2UnitGraph.put(method, new WeakReference(result));
			} else {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Method " + method + " is not concrete.");
				}
			}
		} else if (ref != null) {
			result = (TrapUnitGraph) ref.get();
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
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
