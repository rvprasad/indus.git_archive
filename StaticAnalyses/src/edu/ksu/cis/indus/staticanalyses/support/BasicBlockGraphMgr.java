
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import soot.SootMethod;

import soot.jimple.JimpleBody;

import soot.toolkits.graph.UnitGraph;

import java.lang.ref.WeakReference;

import java.util.HashMap;
import java.util.Map;


/**
 * This class manages a set of basic block graphs.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public class BasicBlockGraphMgr {
	/**
	 * This maps methods to basic block graphs.
     * @invariant method2graph.oclIsKindOf(Map(SootMethod, BasicBlockGraph)) 
	 */
	private final Map method2graph = new HashMap();

	/**
	 * Provides the basic block graph corresponding to the given control flow graph.  It creates one if none exists.
	 *
	 * @param stmtGraph is the control flow graph of interest.
	 *
	 * @return the basic block graph corresonding to <code>stmtGraph</code>.
	 *
	 * @post result != null
	 */
	public BasicBlockGraph getBasicBlockGraph(final UnitGraph stmtGraph) {
		SootMethod method = ((JimpleBody) stmtGraph.getBody()).getMethod();
		WeakReference ref = (WeakReference) method2graph.get(method);

		if (ref == null || ref.get() == null) {
			ref = new WeakReference(new BasicBlockGraph(stmtGraph));
			method2graph.put(method, ref);
		}
		return (BasicBlockGraph) ref.get();
	}

	/**
	 * Retrieves the basic block graph corresponding to the given method.
	 *
	 * @param sm is the method for which the graph is requested.
	 *
	 * @return the basic block graph corresponding to <code>sm</code>, if one exists.  <code>null</code> is returned
	 * 		   otherwise.
	 */
	public BasicBlockGraph getBasicBlockGraph(final SootMethod sm) {
		WeakReference ref = (WeakReference) method2graph.get(sm);
		BasicBlockGraph result = null;

		if (ref != null) {
			result = (BasicBlockGraph) ref.get();
		}
		return result;
	}
}

/*
   ChangeLog:
   
   $Log$
   
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
    
   Revision 1.5  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
