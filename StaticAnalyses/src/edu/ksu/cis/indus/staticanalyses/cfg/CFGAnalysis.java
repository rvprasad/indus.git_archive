
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

package edu.ksu.cis.indus.staticanalyses.cfg;

import edu.ksu.cis.indus.common.graph.IDirectedGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph;
import edu.ksu.cis.indus.common.soot.BasicBlockGraph.BasicBlock;
import edu.ksu.cis.indus.common.soot.BasicBlockGraphMgr;
import edu.ksu.cis.indus.common.soot.SootPredicatesAndTransformers;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.interfaces.IThreadGraphInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.IteratorUtils;

import soot.SootMethod;

import soot.jimple.Stmt;


/**
 * This class performs generic control flow analysis.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class CFGAnalysis {
	/** 
	 * This manages the basic block graphs of the methods being analyzed.
	 */
	private final BasicBlockGraphMgr bbm;

	/** 
	 * This provides call-graph information.
	 */
	private final ICallGraphInfo cgi;

	/** 
	 * This is a cache of the collection of method invocation statements in methods.
	 *
	 * @invariant method2EnclosingInvokingStmtsCache.oclIsKindOf(Map(SootMethod, Collection(Stmt)))
	 * @invariant method2EnclosingInvokingStmtsCache.oclIsKindOf.values()->forall(o | o->forall(p | p.containsInvokeExpr()))
	 */
	private Map method2EnclosingInvokingStmtsCache = new HashMap();

	/**
	 * Creates a new CFGAnalysis object.
	 *
	 * @param cgiParam provides the call-graph information.
	 * @param bbmParam manages the basic block graphs of the methods being analyzed.
	 *
	 * @pre cgiParam != null and bbmParam != null
	 */
	public CFGAnalysis(final ICallGraphInfo cgiParam, final BasicBlockGraphMgr bbmParam) {
		this.cgi = cgiParam;
		this.bbm = bbmParam;
	}

	/**
	 * Checks if the destination site is reachable from the source site via an interprocedural control flow path.
	 *
	 * @param srcMethod contains the def site.
	 * @param srcStmt is the def site.
	 * @param destMethod contains the use site.
	 * @param destStmt is the use site.
	 * @param tgi is the thread graph.
	 *
	 * @return <code>true</code> if such a path exists; <code>false</code>, otherwise.
	 *
	 * @pre srdMethod != null and srcStmt != null and destMethod != null and destStmt != null and tgi != null
	 */
	public boolean isReachableViaInterProceduralControlFlow(final SootMethod srcMethod, final Stmt srcStmt,
		final SootMethod destMethod, final Stmt destStmt, final IThreadGraphInfo tgi) {
		boolean _result = tgi == null || !tgi.mustOccurInDifferentThread(srcMethod, destMethod);

		if (_result) {
			_result = false;

			/*
			 * Check if the dest method is reachable from the src method. If so, check the dest method is reachable via
			 * any of the invocation statement that succeed srcStmt in srcMethod.
			 */
			if (cgi.isCalleeReachableFromCaller(destMethod, srcMethod)) {
				_result = doesControlFlowPathExistsBetween(srcMethod, srcStmt, destMethod, true, false);
			}

			/*
			 * Check if the src method is reachable from the dest method. If so, check the src method is reachable via
			 * any of the invocation statements that precede destStmt in destMethod.
			 */
			if (!_result && cgi.isCalleeReachableFromCaller(srcMethod, destMethod)) {
				_result = doesControlFlowPathExistsBetween(destMethod, destStmt, srcMethod, false, false);
			}

			/*
			 * Check if the control can reach from the src method to the dest method via some common ancestor in the
			 * call-graph.  We cannot assume that the previous two conditions need to be false to evaluate this block.  The
			 * reason being that there may be a call chain from the srcMethod to the destMethod but the invocation site in
			 * srcMethod may occur prior to the srcStmt.  The same holds for condition two.
			 */
			if (!_result) {
				_result = doesControlPathExistsFromTo(srcMethod, destMethod);
			}
		} else {
			_result = tgi == null || tgi.containsClassInitThread(tgi.getExecutionThreads(srcMethod));
		}
		return _result;
	}

	/**
	 * Checks if the given new expression is enclosed in a loop.
	 *
	 * @param newStmt is the statement of the allocation site.
	 * @param method in which<code>newStmt</code> occurs.
	 *
	 * @return <code>true</code> if the given allocation site is loop enclosed; <code>false</code>, otherwise.
	 *
	 * @pre ne != null and context != null and context.getCurrentMethod() != null
	 */
	public boolean checkForLoopEnclosedNewExpr(final Stmt newStmt, final SootMethod method) {
		boolean _result = false;

		final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(method);

		if (occursInCycle(_bbg, _bbg.getEnclosingBlock(newStmt))) {
			_result = true;
		}
		return _result;
	}

	/**
	 * Checks if there is path for control between <code>targetmethod</code> and <code>stmt</code> in <code>method</code> via
	 * intra-procedural control flow and call chains.
	 *
	 * @param method in which control starts.
	 * @param stmt at which control starts.
	 * @param targetMethod which the control should reach.
	 * @param forward <code>true</code> indicates to check if the the control reaches the <code>targetmethod</code> from  the
	 * 		  <code>stmt</code> in <code>method</code>; <code>false</code>, if the the control reaches <code>stmt</code> in
	 * 		  <code>method</code> from <code>targetmethod</code>.
	 * @param exclusive <code>true</code> indicates that <code>stmt</code> should not be considered during existence check;
	 * 		  <code>false</code>, otherwise.
	 *
	 * @return <code>true</code> if there is control flow path between the given points; <code>false</code>, otherwise.
	 */
	public boolean doesControlFlowPathExistsBetween(final SootMethod method, final Stmt stmt, final SootMethod targetMethod,
		final boolean forward, final boolean exclusive) {
		boolean _result = false;
		final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(method);
		final BasicBlock _bbDef = _bbg.getEnclosingBlock(stmt);
		final List _bbDefStmts;

		if (forward) {
			_bbDefStmts = _bbDef.getStmtsFrom(stmt);

			if (exclusive) {
				_bbDefStmts.remove(0);
			}
		} else {
			_bbDefStmts = _bbDef.getStmtsFromTo(_bbDef.getLeaderStmt(), stmt);

			if (exclusive) {
				_bbDefStmts.remove(_bbDefStmts.size() - 1);
			}
		}

		for (final Iterator _j =
				IteratorUtils.filteredIterator(_bbDefStmts.iterator(), SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
			  _j.hasNext() && !_result;) {
			final Stmt _stmt = (Stmt) _j.next();
			_result = cgi.isCalleeReachableFromCallSite(targetMethod, _stmt, method);
		}

		for (final Iterator _i = _bbg.getReachablesFrom(_bbDef, forward).iterator(); _i.hasNext() && !_result;) {
			final BasicBlock _bb = (BasicBlock) _i.next();

			for (final Iterator _j =
					IteratorUtils.filteredIterator(_bb.getStmtsOf().iterator(),
						SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE); _j.hasNext() && !_result;) {
				final Stmt _stmt = (Stmt) _j.next();
				_result = cgi.isCalleeReachableFromCallSite(targetMethod, _stmt, method);
			}
		}
		return _result;
	}

	/**
	 * Checks if there is a control path between the <code>srcMethod</code> and <code>destMethod</code> through control flow
	 * path from src-method invoking site to dest-method invoking site in a method that is ancestor of both src and dest
	 * methods.
	 *
	 * @param srcMethod is the source method.
	 * @param destMethod is the destination method.
	 *
	 * @return <code>true</code> if there is a control path; <code>false</code>, otherwise.
	 *
	 * @pre srcMethod != null and destMethod != null
	 */
	public boolean doesControlPathExistsFromTo(final SootMethod srcMethod, final SootMethod destMethod) {
		boolean _result = false;
		final Collection _commonAncestors = cgi.getCommonMethodsReachableFrom(srcMethod, false, destMethod, false);

		for (final Iterator _i = _commonAncestors.iterator(); _i.hasNext() && !_result;) {
			final SootMethod _sm = (SootMethod) _i.next();
			_result = doesMethodLiesOnTheDataFlowPathBetween(_sm, srcMethod, destMethod);
		}
		return _result;
	}

	/**
	 * Checks if <code>method</code> has calls to <code>srcMethod</code> and <code>destMethod</code> and that the callsite to
	 * the dest site is reachable from the call site to the src method.  We shall refer to such methods as interprocedural
	 * data flow join points.
	 *
	 * @param method that connects the interprocedural data flow paths.
	 * @param srcMethod is the source method.
	 * @param destMethod is the destination method.
	 *
	 * @return <code>true</code> if <code>method</code> in indeed an interprocedural data-flow join point;
	 * 		   <code>false</code>, otherwise.
	 *
	 * @pre method != null and srcMethod != null and destMethod != null
	 */
	public boolean doesMethodLiesOnTheDataFlowPathBetween(final SootMethod method, final SootMethod srcMethod,
		final SootMethod destMethod) {
		boolean _result = false;
		final Iterator _invokingStmtIterator = getInvokingStmtIteratorFor(method);

		for (final Iterator _j = _invokingStmtIterator; _j.hasNext() && !_result;) {
			final Stmt _stmt = (Stmt) _j.next();

			if (cgi.isCalleeReachableFromCallSite(srcMethod, _stmt, method)) {
				_result = doesControlFlowPathExistsBetween(method, _stmt, destMethod, true, true);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given statement is executed multiple times as a result of being loop-enclosed or occuring in a  method
	 * that is executed multiple times.
	 *
	 * @param stmt is the statement.
	 * @param caller is the method in which <code>stmt</code> occurs.
	 *
	 * @return <code>true</code> if <code>stmt</code> is executed multiple times; <code>false</code>, otherwise.
	 *
	 * @pre stmt != null and caller != null
	 */
	public boolean executedMultipleTimes(final Stmt stmt, final SootMethod caller) {
		final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(caller);
		return occursInCycle(_bbg, _bbg.getEnclosingBlock(stmt)) || executedMultipleTimes(caller);
	}

	/**
	 * Checks if the given soot method is executed multiple times in the system.  It may be due to loop enclosed call-sites,
	 * multiple call sites, or call-sites in call graph SCCs (with more than one element).
	 *
	 * @param method is the method.
	 *
	 * @return <code>true</code> if the given method or any of it's ancestors in the call tree have multiple or
	 * 		   multiply-executed call sites; <code>false</code>, otherwise.
	 *
	 * @pre method != null
	 */
	public boolean executedMultipleTimes(final SootMethod method) {
		boolean _result = false;
		final Collection _callers = cgi.getCallers(method);

		if (_callers.size() > 1) {
			_result = true;
		} else if (_callers.size() == 1) {
			for (final Iterator _i = cgi.getSCCs(true).iterator(); _i.hasNext() && !_result;) {
				final Collection _scc = (Collection) _i.next();

				if (_scc.contains(method) && _scc.size() > 1) {
					_result = true;
				}
			}

			if (!_result) {
				final CallTriple _ctrp = (CallTriple) _callers.iterator().next();
				final SootMethod _caller = _ctrp.getMethod();
				final BasicBlockGraph _bbg = bbm.getBasicBlockGraph(_caller);

				if (occursInCycle(_bbg, _bbg.getEnclosingBlock(_ctrp.getStmt()))) {
					_result = true;
				} else {
					_result = executedMultipleTimes(_caller);
				}
			}
		}
		return _result;
	}

	/**
	 * Checks if the given methods occur in the same SCC in the call graph of the system.
	 *
	 * @param m is one of the methods.
	 * @param p is another method.
	 *
	 * @return <code>true</code> if the given methods occur in the same SCC; <code>false</code>, otherwise.
	 *
	 * @pre m != null and p != null
	 */
	public boolean notInSameSCC(final SootMethod m, final SootMethod p) {
		boolean _result = true;

		for (final Iterator _i = cgi.getSCCs(true).iterator(); _i.hasNext() && _result;) {
			final Collection _scc = (Collection) _i.next();

			if (_scc.contains(m)) {
				_result = !_scc.contains(p);
			}
		}
		return _result;
	}

	/**
	 * Checks if the given node occurs in a cycle in the graph.  This may be sufficient in some cases rather than capturing
	 * the cycle itself.
	 *
	 * @param graph in which <code>node</code> occurs.
	 * @param node which may occur in a cycle.
	 *
	 * @return <code>true</code> if <code>node</code> occurs in cycle; <code>false</code>, otherwise.
	 */
	public boolean occursInCycle(final IDirectedGraph graph, final BasicBlock node) {
		return graph.isReachable(node, node, true);
	}

	/**
	 * Resets internal datastructures.
	 */
	public void reset() {
		method2EnclosingInvokingStmtsCache.clear();
	}

	/**
	 * Retrieves the iterator on the collection of statements in the method that invoke methods.
	 *
	 * @param method of interest.
	 *
	 * @return an iterator.
	 *
	 * @pre method != null
	 * @post result != null
	 * @post all returned values are of type Stmt and contain an invoke expression.
	 */
	private Iterator getInvokingStmtIteratorFor(final SootMethod method) {
		final Collection _temp = (Collection) method2EnclosingInvokingStmtsCache.get(method);
		final Iterator _result;

		if (_temp == null) {
			final Iterator _stmts = bbm.getBasicBlockGraph(method).getStmtGraph().iterator();
			_result = IteratorUtils.filteredIterator(_stmts, SootPredicatesAndTransformers.INVOKING_STMT_PREDICATE);
			method2EnclosingInvokingStmtsCache.put(method, IteratorUtils.toList(_result));
		} else {
			_result = _temp.iterator();
		}
		return _result;
	}
}

// End of File
