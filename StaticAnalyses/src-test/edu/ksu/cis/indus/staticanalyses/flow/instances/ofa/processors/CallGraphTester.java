
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

package edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors;

import edu.ksu.cis.indus.common.graph.SimpleNodeGraph;
import edu.ksu.cis.indus.common.graph.SimpleNodeGraph.SimpleNode;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.ValueAnalyzerBasedProcessingController;

import edu.ksu.cis.indus.common.graph.DirectedAndSimpleNodeGraphTest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import junit.extensions.TestSetup;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import org.apache.commons.collections.CollectionUtils;

import soot.ArrayType;
import soot.G;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.VoidType;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CallGraphTester
  extends TestCase {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static CallGraph cgi;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static OFAnalyzer ofa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static Scene scene;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static String classes;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	static final String tagName = "CallGraphTester:FA";

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	public static final class GraphTest
	  extends DirectedAndSimpleNodeGraphTest {
		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void testGetNodes() {
			Collection reachables = cgi.getReachableMethods();
			Collection d = new HashSet();

			for (final Iterator _i = dg.getNodes().iterator(); _i.hasNext();) {
				SimpleNode node = (SimpleNode) _i.next();
				d.add(node._object);
			}
			assertTrue(d.containsAll(reachables));
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		public void testSize() {
			assertTrue(cgi.getReachableMethods().size() == dg.getNodes().size());
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void setUp() {
			dg = (SimpleNodeGraph) cgi.getCallGraph();
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void localtestAddEdgeFromTo() {
			// we do nothing as we are dealing with an immutable graph
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void localtestGetHeads() {
			assertTrue(dg.getHeads().containsAll(cgi.getHeads()));
			assertTrue(cgi.getHeads().containsAll(dg.getHeads()));
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void localtestGraphGetTails() {
			for (final Iterator _i = dg.getTails().iterator(); _i.hasNext();) {
				SimpleNode node = (SimpleNode) _i.next();
				assertTrue(cgi.getCallees((SootMethod) node._object).isEmpty());
			}

			for (final Iterator _i = cgi.getReachableMethods().iterator(); _i.hasNext();) {
				SootMethod sm = (SootMethod) _i.next();

				if (cgi.getCallees(sm).isEmpty()) {
					assertTrue(dg.getNode(sm).getSuccsOf().isEmpty());
				}
			}
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void localtestIsAncestorOf() {
			// we cannot know in advance what is the ancestor relationship in a graph.  Do nothing.
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * <p></p>
		 */
		protected void localtestIsReachable() {
			// we cannot know in advance what is the calle relationship in a graph.  Do nothing.
		}
	}


	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private static final class CallGraphTestSetup
	  extends TestSetup {
		/**
		 * Creates a new CallGraphTestSetup object.
		 *
		 * @param test DOCUMENT ME!
		 */
		CallGraphTestSetup(final Test test) {
			super(test);
		}

		/**
		 * @see TestCase#setUp()
		 */
		protected final void setUp()
		  throws Exception {
			ofa = OFAnalyzer.getFSOSAnalyzer(tagName);
			scene = Scene.v();

			if (classes == null) {
				classes = System.getProperty("callgraphtester.classes");
			}

			if (classes == null || classes.length() == 0) {
				throw new RuntimeException("callgraphtester.classes property was empty.  Aborting.");
			}

			StringBuffer sb = new StringBuffer(classes);
			String[] j = sb.toString().split(" ");
			Collection rootMethods = new ArrayList();

			for (int i = j.length - 1; i >= 0; i--) {
				SootClass sc = scene.loadClassAndSupport(j[i]);

				if (sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						  VoidType.v())) {
					SootMethod sm =
						sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
							VoidType.v());

					if (sm.isPublic() && sm.isConcrete()) {
						rootMethods.add(sm);
					}
				}
			}

			ofa.analyze(scene, rootMethods);

			CallGraph cgiImpl = new CallGraph();

			ValueAnalyzerBasedProcessingController pc = new ValueAnalyzerBasedProcessingController();
			pc.setAnalyzer(ofa);
			pc.setEnvironment(ofa.getEnvironment());
			pc.setProcessingFilter(new TagBasedProcessingFilter(tagName));
			cgiImpl.hookup(pc);
			pc.process();
			cgiImpl.unhook(pc);
			cgi = cgiImpl;
			System.out.println(cgiImpl.dumpGraph());
		}

		/**
		 * @see TestCase#tearDown()
		 */
		protected final void tearDown()
		  throws Exception {
			ofa.reset();
			ofa = null;
			cgi = null;
			scene = null;
			G.reset();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(final String[] args) {
		StringBuffer sb = new StringBuffer();

		for (int i = args.length - 1; i >= 0; i--) {
			sb.append(args[i] + " ");
		}
		classes = sb.toString();

		TestRunner runner = new TestRunner();
		runner.setLoading(false);
		runner.start(new String[0]);
		runner.startTest(suite());
		runner.runSuite();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.processors.CallGraph");

		//$JUnit-BEGIN$
		suite.addTestSuite(CallGraphTester.class);
		suite.addTestSuite(GraphTest.class);
		//$JUnit-END$
		return new CallGraphTestSetup(suite);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @throws Throwable DOCUMENT ME!
	 */
	public final void testGetCallGraph()
	  throws Throwable {
		assertNotNull(cgi.getCallGraph());
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetCalleesInvokeExprContext() {
		Context context = new Context();
		Collection calleeMethods = new ArrayList();

		for (Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod caller = (SootMethod) i.next();
			Collection callees = cgi.getCallees(caller);
			calleeMethods.clear();

			for (Iterator j = callees.iterator(); j.hasNext();) {
				CallTriple ctrp = (CallTriple) j.next();
				calleeMethods.add(ctrp.getMethod());
			}

			for (Iterator j = callees.iterator(); j.hasNext();) {
				CallTriple ctrp1 = (CallTriple) j.next();
				context.setStmt(ctrp1.getStmt());
				context.setRootMethod(caller);

				Collection newCallees = cgi.getCallees(ctrp1.getExpr(), context);

				for (Iterator k = callees.iterator(); k.hasNext();) {
					CallTriple ctrp2 = (CallTriple) k.next();

					if (ctrp2.getExpr().equals(ctrp1.getExpr())) {
						assertTrue(newCallees.contains(ctrp2.getMethod()));
					}
				}
				calleeMethods.removeAll(newCallees);
			}
			assertTrue(calleeMethods.isEmpty());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetCallersAndGetCallees() {
		Collection heads = cgi.getHeads();
		Collection tails = cgi.getCallGraph().getTails();

		for (Iterator i = cgi.getReachableMethods().iterator(); i.hasNext();) {
			SootMethod callee = (SootMethod) i.next();
			Collection callers = cgi.getCallers(callee);
			assertNotNull(callers);

			if (callers.isEmpty()) {
				assertTrue(heads.contains(callee));
			} else {
				for (Iterator j = callers.iterator(); j.hasNext();) {
					CallTriple ctrp1 = (CallTriple) j.next();
					SootMethod caller = ctrp1.getMethod();
					assertTrue(cgi.isReachable(caller));

					Collection callees = cgi.getCallees(caller);

					if (callees.isEmpty()) {
						assertTrue(tails.contains(caller));
					} else {
						boolean t = false;

						for (Iterator k = callees.iterator(); k.hasNext();) {
							CallTriple ctrp2 = (CallTriple) k.next();

							if (ctrp2.getMethod().equals(callee)) {
								t = true;
								break;
							}
						}
						assertTrue(t);
					}
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetHeads() {
		Collection heads = cgi.getHeads();
		assertNotNull(heads);

		for (Iterator i = heads.iterator(); i.hasNext();) {
			SootMethod sm = (SootMethod) i.next();
			assertTrue(cgi.getCallers(sm).isEmpty());
		}
		assertTrue(cgi.getReachableMethods().containsAll(heads));
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetReachableMethods() {
		Collection reachables = cgi.getReachableMethods();
		assertNotNull(reachables);

		for (Iterator i = reachables.iterator(); i.hasNext();) {
			assertTrue(i.next() instanceof SootMethod);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testGetSCCs() {
		Collection sccs = cgi.getSCCs(true);
		Collection reachables = cgi.getReachableMethods();
		assertNotNull(sccs);

		for (Iterator i = sccs.iterator(); i.hasNext();) {
			Collection scc1 = (Collection) i.next();
			assertNotNull(scc1);
			assertTrue(reachables.containsAll(scc1));

			for (Iterator j = sccs.iterator(); j.hasNext();) {
				Collection scc2 = (Collection) j.next();

				if (scc1 != scc2) {
					assertTrue(CollectionUtils.intersection(scc1, scc2).isEmpty());
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testIsReachable() {
		Collection reachables = cgi.getReachableMethods();
		Collection heads = cgi.getHeads();
		SimpleNodeGraph cg = (SimpleNodeGraph) cgi.getCallGraph();

		for (Iterator i = scene.getClasses().iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			for (Iterator j = sc.getMethods().iterator(); j.hasNext();) {
				SootMethod sm = (SootMethod) j.next();
				assertEquals(cgi.isReachable(sm), reachables.contains(sm));

				if (cgi.isReachable(sm)) {
					boolean t = false;

					for (Iterator k = heads.iterator(); k.hasNext();) {
						t |= cg.isReachable(cg.getNode(k.next()), cg.getNode(sm), true);
					}
					assertTrue(t || heads.contains(sm));
				}
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public final void testTagsOnReachableMethods() {
		Context ctxt = new Context();
		Collection reachables = cgi.getReachableMethods();
		assertNotNull(reachables);

		for (Iterator i = reachables.iterator(); i.hasNext();) {
			SootMethod o = (SootMethod) i.next();
			assertTrue(o.hasTag(tagName));
			assertTrue(o.getDeclaringClass().hasTag(tagName));

			if (!o.isStatic()) {
				ctxt.setRootMethod(o);
				assertNotNull(ofa.getValuesForThis(ctxt));
			}
		}

		Collection methods = new HashSet();

		for (final Iterator _i = ofa.getEnvironment().getClasses().iterator(); _i.hasNext();) {
			methods.addAll(((SootClass) _i.next()).getMethods());
		}
		methods = CollectionUtils.subtract(methods, reachables);

		for (final Iterator _i = methods.iterator(); _i.hasNext();) {
			final SootMethod _sm = (SootMethod) _i.next();

			if (!_sm.isAbstract()) {
				assertFalse(_sm.hasTag(tagName));
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.15  2003/12/09 03:35:48  venku
   - formatting and removal of stdouts.

   Revision 1.14  2003/12/08 13:31:49  venku
   - used JUnit defined assert functions.
   Revision 1.13  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.12  2003/12/08 12:15:58  venku
   - moved support package from StaticAnalyses to Indus project.
   - ripple effect.
   - Enabled call graph xmlization.
   Revision 1.11  2003/12/07 14:04:43  venku
   - made FATester command-line compatible.
   - made use of DirectedAndSimpleNodeGraphTest in
     CallGraphTester to test the constructed call graphs.
   Revision 1.10  2003/12/05 21:34:01  venku
   - formatting.
   - more tests.
   Revision 1.9  2003/12/05 15:28:12  venku
   - added test case for trivial tagging test in FA.
   Revision 1.8  2003/12/05 11:48:19  venku
   - added one more check while testing SCC.
   Revision 1.7  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.6  2003/11/30 01:38:52  venku
   - incorporated tag based filtering during CG construction.
   Revision 1.5  2003/11/30 01:07:57  venku
   - added name tagging support in FA to enable faster
     post processing based on filtering.
   - ripple effect.
   Revision 1.4  2003/11/30 00:21:48  venku
   - coding convention.
   Revision 1.3  2003/11/29 09:48:14  venku
   - 2 SCC should be disjoint.  intersection should be used
     instead of subtract.  FIXED.
   Revision 1.2  2003/11/29 09:44:20  venku
   - changed the check for getCallees(InvokeExpr,..).
   Revision 1.1  2003/11/29 09:35:44  venku
   - added test support for processors.  CallGraph, in particular.
 */
