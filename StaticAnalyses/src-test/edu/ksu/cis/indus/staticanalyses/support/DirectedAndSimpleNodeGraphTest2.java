
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

import junit.framework.TestSuite;

import edu.ksu.cis.indus.staticanalyses.support.SimpleNodeGraph.SimpleNode;


/**
 * This class tests directed graph and simple node graph implementations with a different graph instance.  All the local
 * tests are overloaded suitably.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DirectedAndSimpleNodeGraphTest2
  extends DirectedAndSimpleNodeGraphTest1 {
	/**
	 * Returns a suite of tests defined in this class.
	 *
	 * @return the suite of tests.
	 */
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("suite of tests in DirectedAndSimpleNodeGraphTest2");
		suite.addTestSuite(DirectedAndSimpleNodeGraphTest2.class);
		return suite;
	}

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
 */
