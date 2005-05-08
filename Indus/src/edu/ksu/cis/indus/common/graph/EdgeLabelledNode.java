
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

package edu.ksu.cis.indus.common.graph;

import edu.ksu.cis.indus.common.graph.IEdgeLabelledDirectedGraph.IEdgeLabelledNode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;


/**
 * This is an implementation of edge-labelled nodes.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class EdgeLabelledNode
  extends Node
  implements IEdgeLabelledNode {
	/** 
	 * This maps nodes to maps that map labels to nodes reachable from the key node via an edge with the key label.
	 *
	 * @pre node2inEdges.oclIsKindOf(Map(INode, Map(IEdgeLabel, Collection(INode))))
	 */
	final Map label2inNodes = new HashMap();

	/** 
	 * This maps nodes to maps that map labels to nodes that can reachable the key node via an edge with the key label.
	 *
	 * @pre node2inEdges.oclIsKindOf(Map(INode, Map(IEdgeLabel, Collection(INode))))
	 */
	final Map label2outNodes = new HashMap();

	/**
	 * Creates an instance of this class.
	 *
     * @param preds is the reference to the collection of predecessors.
     * @param succs is the reference to the collection of successors.
     *
     * @pre preds != null and succs != null
	 */
	protected EdgeLabelledNode(final Collection preds, final Collection succs) {
		super(preds, succs);
	}

	/**
	 * @see IEdgeLabelledNode#getIncomingEdgeLabels()
	 */
	public Collection getIncomingEdgeLabels() {
		return Collections.unmodifiableCollection(label2inNodes.keySet());
	}

	/**
	 * @see IEdgeLabelledNode#getOutGoingEdgeLabels()
	 */
	public Collection getOutGoingEdgeLabels() {
		return Collections.unmodifiableCollection(label2outNodes.keySet());
	}

	/**
	 * @see IEdgeLabelledNode#getPredsViaEdgesLabelled(IEdgeLabel)
	 */
	public Collection getPredsViaEdgesLabelled(final IEdgeLabel label) {
		return (Collection) MapUtils.getObject(label2inNodes, label, Collections.EMPTY_SET);
	}

	/**
	 * @see IEdgeLabelledNode#getSuccsViaEdgesLabelled(IEdgeLabel)
	 */
	public Collection getSuccsViaEdgesLabelled(final IEdgeLabel label) {
		return (Collection) MapUtils.getObject(label2outNodes, label, Collections.EMPTY_SET);
	}

	/**
	 * @see IEdgeLabelledNode#hasIncomingEdgeLabelled(IEdgeLabel)
	 */
	public boolean hasIncomingEdgeLabelled(final IEdgeLabel label) {
		return label2inNodes.containsKey(label);
	}

	/**
	 * @see IEdgeLabelledNode#hasOutgoingEdgeLabelled(IEdgeLabel)
	 */
	public boolean hasOutgoingEdgeLabelled(final IEdgeLabel label) {
		return label2outNodes.containsKey(label);
	}
}

// End of File