
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.graph.AbstractDirectedGraphTest;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.IProcessor;

import edu.ksu.cis.indus.staticanalyses.flow.FATestSetup;
import edu.ksu.cis.indus.staticanalyses.flow.IFATest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.collections.CollectionUtils;

import soot.SootClass;
import soot.SootMethod;


/**
 * This class tests information calculated by
 * <code>edu.ksu.cis.indus.staticanalyses.flow.instances.valueAnalyzer.processors.CallGraph</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * 
 * @deprecated
 */
public final class CallGraphTest
  extends AbstractDirectedGraphTest implements IFATest {
	/** 
	 * The call graph to be tested.
	 */
	private ICallGraphInfo cgi;

	/** 
	 * The object flow analysis used to construct the call graph.
	 */
	private OFAnalyzer ofa;

	/** 
	 * The call graph.
	 */
	private SimpleNodeGraph cg;

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer valueAnalyzer) {
		ofa = (OFAnalyzer) valueAnalyzer;
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFATest#setFATagName(java.lang.String)
	 */
	public void setFATagName(final String tagName) {
	}

	/**
	 * Sets the call graph information instance to be used during test.
	 *
	 * @param processor provides call graph information.
	 *
	 * @pre processor != null
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.flow.IFAProcessorTest#setProcessor(IProcessor)
	 */
	public void setProcessor(final IProcessor processor) {
		final CallGraph _cg = (CallGraph) processor;
		cgi = _cg;
		cg = (SimpleNodeGraph) _cg.getCallGraph();
		dg = cg;
	}

	/**
	 * Tests <code>isReachable</code>.
	 */
	public void localtestIsReachable() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _heads = cgi.getHeads();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();
				assertEquals(cgi.isReachable(_sm), _reachables.contains(_sm));

				if (cgi.isReachable(_sm)) {
					boolean _t = false;

					for (final Iterator _k = _heads.iterator(); _k.hasNext();) {
						_t |= cg.isReachable(cg.getNode(_k.next()), cg.getNode(_sm), true);
					}
					assertTrue(_t || _heads.contains(_sm));
				}
			}
		}
	}

	/**
	 * Tests <code>getCallGraph()</code>.
	 */
	public void testGetCallGraph() {
		if (cgi instanceof CallGraph) {
			assertNotNull(((CallGraph) cgi).getCallGraph());
		}
	}

	/**
	 * Tests <code>getCalles(soot.jimple.InvokeExpr, edu.ksu.cis.indus.processing.Context)</code>.
	 */
	public void testGetCalleesInvokeExprContext() {
		final Context _context = new Context();
		final Collection _calleeMethods = new ArrayList();

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _caller = (SootMethod) _i.next();
			final Collection _callees = cgi.getCallees(_caller);
			_calleeMethods.clear();

			for (final Iterator _j = _callees.iterator(); _j.hasNext();) {
				final CallTriple _ctrp = (CallTriple) _j.next();
				_calleeMethods.add(_ctrp.getMethod());
			}

			for (final Iterator _j = _callees.iterator(); _j.hasNext();) {
				final CallTriple _ctrp1 = (CallTriple) _j.next();
				_context.setStmt(_ctrp1.getStmt());
				_context.setRootMethod(_caller);

				final Collection _newCallees = cgi.getCallees(_ctrp1.getExpr(), _context);

				for (final Iterator _k = _callees.iterator(); _k.hasNext();) {
					final CallTriple _ctrp2 = (CallTriple) _k.next();

					if (_ctrp2.getExpr().equals(_ctrp1.getExpr())) {
						assertTrue(_newCallees.contains(_ctrp2.getMethod()));
					}
				}
				_calleeMethods.removeAll(_newCallees);
			}
			assertTrue(_calleeMethods.isEmpty());
		}
	}

	/**
	 * Tests <code>getCallers(soot.Method)</code> and <code>getCallees(soot.Method)</code>.
	 */
	public void testGetCallersAndGetCallees() {
		final Collection _heads = cgi.getHeads();
		final Collection _tails = cg.getTails();

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _callee = (SootMethod) _i.next();
			final Collection _callers = cgi.getCallers(_callee);
			assertNotNull(_callers);

			if (_callers.isEmpty()) {
				assertTrue(_heads.contains(_callee));
			} else {
				for (final Iterator _j = _callers.iterator(); _j.hasNext();) {
					final CallTriple _ctrp1 = (CallTriple) _j.next();
					final SootMethod _caller = _ctrp1.getMethod();
					assertTrue(cgi.isReachable(_caller));

					final Collection _callees = cgi.getCallees(_caller);

					if (_callees.isEmpty()) {
						assertTrue(_tails.contains(_caller));
					} else {
						boolean _t = false;

						for (final Iterator _k = _callees.iterator(); _k.hasNext();) {
							final CallTriple _ctrp2 = (CallTriple) _k.next();

							if (_ctrp2.getMethod().equals(_callee)) {
								_t = true;
								break;
							}
						}
						assertTrue(_t);
					}
				}
			}
		}
	}

	/**
	 * Tests getNodes() of the graph associated with the call graph.
	 */
	public void testGetNodes() {
		final Collection _reachables = cgi.getReachableMethods();
		final Collection _d = new HashSet();

		for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
			final SimpleNode _node = (SimpleNode) _i.next();
			_d.add(_node.getObject());
		}
		assertTrue(_d.containsAll(_reachables));
		assertTrue(_reachables.containsAll(_d));
	}

	/**
	 * Tests <code>getReachableMethods()</code>.
	 */
	public void testGetReachableMethods() {
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			assertTrue(_i.next() instanceof SootMethod);
		}
	}

	/**
	 * Tests the size() method of the graph associated with the call graph.
	 */
	public void testSize() {
		assertTrue(cgi.getReachableMethods().size() == dg.getNodes().size());
	}

	/**
	 * Tests the tags on the reachable methods based on tags used during object flow analysis.
	 */
	public void testTagsOnReachableMethods() {
		final Context _ctxt = new Context();
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_reachables);

		for (final Iterator _i = _reachables.iterator(); _i.hasNext();) {
			final SootMethod _o = (SootMethod) _i.next();
			assertTrue(_o.hasTag(FATestSetup.TAG_NAME));
			assertTrue(_o.getDeclaringClass().hasTag(FATestSetup.TAG_NAME));

			if (!_o.isStatic()) {
				_ctxt.setRootMethod(_o);
				assertNotNull(ofa.getValuesForThis(_ctxt));
			}
		}

		Collection _methods = new HashSet();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			_methods.addAll(((SootClass) _i.next()).getMethods());
		}
		_methods = CollectionUtils.subtract(_methods, _reachables);

		for (final Iterator _i = _methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (!_sm.isAbstract()) {
				assertFalse(_sm.hasTag(FATestSetup.TAG_NAME));
			}
		}
	}

	/**
	 * Test <code>getHeads()</code> method of the graph associated with the call graph.
	 */
	protected void localtestGetHeads() {
		Collection _heads = new HashSet();

		for (final Iterator _i = dg.getHeads().iterator(); _i.hasNext();) {
			final SimpleNode _sn = (SimpleNode) _i.next();
			_heads.add(_sn.getObject());
		}

		assertTrue(_heads.containsAll(cgi.getHeads()));
		assertTrue(cgi.getHeads().containsAll(_heads));

		_heads = cgi.getHeads();
		assertNotNull(_heads);

		for (final Iterator _i = _heads.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();
			assertTrue(cgi.getCallers(_sm).isEmpty());
		}
		assertTrue(cgi.getReachableMethods().containsAll(_heads));
	}

	/**
	 * Tests <code>getSCCs()</code>.
	 */
	protected void localtestGetSCCs() {
		final Collection _sccs = cgi.getSCCs(true);
		final Collection _reachables = cgi.getReachableMethods();
		assertNotNull(_sccs);

		for (final Iterator _i = _sccs.iterator(); _i.hasNext();) {
			final Collection _scc1 = (Collection) _i.next();
			assertNotNull(_scc1);
			assertTrue(_reachables.containsAll(_scc1));

			for (final Iterator _j = _sccs.iterator(); _j.hasNext();) {
				final Collection _scc2 = (Collection) _j.next();

				if (_scc1 != _scc2) {
					assertTrue(CollectionUtils.intersection(_scc1, _scc2).isEmpty());
				}
			}
		}
	}

	/**
	 * Test  <code>getTails()</code> method of the graph associated with the call graph.
	 */
	protected void localtestGraphGetTails() {
		for (final Iterator _i = dg.getTails().iterator(); _i.hasNext();) {
			final SimpleNode _node = (SimpleNode) _i.next();
			assertTrue(cgi.getCallees((SootMethod) _node.getObject()).isEmpty());
		}

		for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (cgi.getCallees(_sm).isEmpty()) {
				assertTrue(cg.getNode(_sm).getSuccsOf().isEmpty());
			}
		}
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown()
	  throws Exception {
		cg = null;
		cgi = null;
		ofa = null;
		super.tearDown();
	}
}

// End of File
