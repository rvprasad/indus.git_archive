
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

package edu.ksu.cis.indus.staticanalyses.support;

import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;


/**
 * This class tests directed graph and simple node graph implementations with a different graph instance.  All the local
 * tests are overloaded suitably.
 * 
 * <p>
 * This represents the CFG as generated by <b>jikes</b> for the following snippet.
 * </p>
 * <pre>
 *  public static void main(String[] s) {
 *       int i = 0;
 *       do {
 *           while(i > 0) {
 *               i--;
 *           };
 *           i++;
 *       } while (i < 10);
 *       System.out.println("Hi");
 *   }
 * </pre>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DirectedAndSimpleNodeGraphTest2
  extends DirectedAndSimpleNodeGraphTest1 {
	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest1#testAddEdgeFromTo()
	 */
	public void localtestAddEdgeFromTo() {
		// we do nothing here as we do not want to change the graph.
	}

	/**
	 * We construct a graph with cycles enclosed in cycles.
	 *
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		dg = new SimpleNodeGraph();
		name2node.put("a", dg.getNode("a"));
		name2node.put("b", dg.getNode("b"));
		name2node.put("c", dg.getNode("c"));
		name2node.put("d", dg.getNode("d"));
		name2node.put("e", dg.getNode("e"));
		name2node.put("f", dg.getNode("f"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("a"), (SimpleNode) name2node.get("b"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("b"), (SimpleNode) name2node.get("c"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("d"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("c"), (SimpleNode) name2node.get("e"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("f"));
		// add loop edges
		dg.addEdgeFromTo((SimpleNode) name2node.get("d"), (SimpleNode) name2node.get("c"));
		dg.addEdgeFromTo((SimpleNode) name2node.get("e"), (SimpleNode) name2node.get("b"));
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest1#localtestGetHeads()
	 */
	protected void localtestGetHeads() {
		assertFalse(dg.getHeads().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest1#localtestGraphGetTails()
	 */
	protected void localtestGraphGetTails() {
		assertFalse(dg.getTails().isEmpty());
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest1#localtestIsAncestorOf()
	 */
	protected void localtestIsAncestorOf() {
		assertTrue(dg.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("a")));
		assertTrue(dg.isAncestorOf((INode) name2node.get("a"), (INode) name2node.get("f")));
		assertFalse(dg.isAncestorOf((INode) name2node.get("e"), (INode) name2node.get("b")));
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.support.DirectedAndSimpleNodeGraphTest1#localtestIsReachable()
	 */
	protected void localtestIsReachable() {
		assertTrue(dg.isReachable((INode) name2node.get("b"), (INode) name2node.get("d"), false));
		assertTrue(dg.isReachable((INode) name2node.get("b"), (INode) name2node.get("d"), true));
		assertTrue(dg.isReachable((INode) name2node.get("a"), (INode) name2node.get("f"), true));
		assertTrue(dg.isReachable((INode) name2node.get("f"), (INode) name2node.get("a"), false));
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.3  2003/09/11 12:31:00  venku
   - made ancestral relationship antisymmetric
   - added testcases to test the relationship.
   Revision 1.2  2003/09/11 02:37:12  venku
   - added a test case for javac compilation of Divergent04 test.
   - created test suite to test directed and simple node graph.
   Revision 1.1  2003/09/11 01:52:07  venku
   - prenum, postnum, and back edges support has been added.
   - added test case to test the above addition.
   - corrected subtle bugs in test1
   - refactored test1 so that setup local testing can be added by subclasses.
 */
