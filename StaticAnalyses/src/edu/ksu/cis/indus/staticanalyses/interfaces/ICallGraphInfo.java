
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

package edu.ksu.cis.indus.staticanalyses.interfaces;

import soot.SootMethod;

import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Stmt;

import edu.ksu.cis.indus.staticanalyses.Context;
import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph;
import edu.ksu.cis.indus.staticanalyses.support.Triple;

import java.util.Collection;


/**
 * This interface provides call graph information pertaining to the analyzed system.   It is adviced that any post processor
 * which provides Call graph information should provide it via this interface.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public interface ICallGraphInfo {
	/**
	 * The id of this interface.
	 */
	String ID = "Callgraph Information";

	/**
	 * This class captures in the information pertaining to a call relation.  It provides the expression, statement, and the
	 * method in which the call occurs.  However, depending on the call relation context, the method may indicate the method
	 * called at an expression and a statement.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$
	 */
	final class CallTriple
	  extends Triple {
		/**
		 * Creates a new CallTriple object.
		 *
		 * @param method is the caller or the callee in the relation.
		 * @param stmt in which the call occurs.
		 * @param expr is the call.
		 */
		public CallTriple(SootMethod method, Stmt stmt, InvokeExpr expr) {
			super(expr, stmt, method);
		}

		/**
		 * Returns the call expression.
		 *
		 * @return the call expression.
		 */
		public InvokeExpr getExpr() {
			return (InvokeExpr) getFirst();
		}

		/**
		 * Returns the caller or the callee.
		 *
		 * @return the caller/callee.
		 */
		public SootMethod getMethod() {
			return (SootMethod) getThird();
		}

		/**
		 * Returns the statement in which the call occurs.
		 *
		 * @return the statement containing the call.
		 */
		public Stmt getStmt() {
			return (Stmt) getSecond();
		}

		/**
		 * Provides a stringized representation of this object.
		 *
		 * @return stringized representation of this object.
		 */
		public String toString() {
			return getSecond() + "@" + getMethod();
		}
	}

	/**
	 * Returns a call graph as an instance of a traversable graph.
	 *
	 * @return SimpleNodeGraph
	 *
	 * @post result->getNodes()->forall(o.oclType = SimpleNodeGraph.SimpleNode and o.object.oclType = SootMethod)
	 */
	SimpleNodeGraph getCallGraph();

	/**
	 * Returns the methods that are reachable from the given invocation point.
	 *
	 * @param stmt in which the method invocation occurs.
	 * @param root in which the method invocation occurs.
	 *
	 * @return a collection of reachable methods.
     * @pre stmt != null and root != null
     * @post result != null and result.oclIsKindOf(Collection(SootMethod))
	 */
	Collection getMethodsReachableFrom(InvokeStmt stmt, SootMethod root);

	/**
	 * Returns the set of methods called in <code>caller</code>.
	 *
	 * @param caller of interest.
	 *
	 * @return a collection of <code>CallTriple</code>s.
	 *
	 * @post result != null and result->forall(o | o.isOclKindOf(CallTriple))
	 */
	Collection getCallees(SootMethod caller);

	/**
	 * Returns the set of methods called in the given <code>expr</code> in the given <code>context</code>.
	 *
	 * @param expr that is the call.
	 * @param context in which the expr occurs.  The calling method should occurs in the call string of the context as the
	 * 		  current method.
	 *
	 * @return a collection of <code>SootMethod</code>s.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 */
	Collection getCallees(InvokeExpr expr, Context context);

	/**
	 * Returns the set of methods that call <code>callee</code>.
	 *
	 * @param callee is the method invoked.
	 *
	 * @return a colleciton of <code>CallTriple</code>s.
	 *
	 * @post result != null and result->forall(o | o.isOclKindOf(CallTriple))
	 */
	Collection getCallers(SootMethod callee);

	/**
	 * Returns a collection of method lists.  The methods in the list form cycles.
	 *
	 * @return a collection of <code>List</code>s of <code>SootMethod</code>s.
	 *
	 * @post result->forall(o | o.oclType = java.util.List(SootMethod))
	 */
	Collection getCycles();

	/**
	 * Returns the methods from which the system starts.
	 *
	 * @return a colleciton of <code>SootMethod</code>s.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 */
	Collection getHeads();

	/**
	 * Checks if the <code>method</code> is reachable in the analyzed system.
	 *
	 * @param method to be checked for reachability.
	 *
	 * @return <code>true</code> if <code>method</code> can be reached in the analyzed system; <code>false</code>, otherwise.
	 */
	boolean isReachable(SootMethod method);

	/**
	 * Returns a collection of methods that can be reached in the analyzed system.
	 *
	 * @return a collection of <code>SootMethod</code>.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 */
	Collection getReachableMethods();

	/**
	 * Returns a collection of methods that are recursion roots in the analyzed system.
	 *
	 * @return a collection of <code>SootMethod</code>.
	 *
	 * @post result != null and result->forall(o | o.oclType = SootMethod)
	 */
	Collection getRecursionRoots();

	/**
	 * Returns a collection of strongly connected components in the given call graph.
	 *
	 * @return a collection of <code>Collection</code> of <code>SootMethod</code>s.
	 *
	 * @post result != null and result->forall(o | o.oclType = Collection(soot.SootMethod))
	 */
	Collection getSCCs();
}

/*****
 ChangeLog:

$Log$

*****/
