
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

package edu.ksu.cis.indus.slicer;

import soot.SootMethod;

import edu.ksu.cis.indus.staticanalyses.support.BasicBlockGraph.BasicBlock;


/**
 * DOCUMENT ME!
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class CompleteSliceGotoProcessor
  implements IGotoProcessor {
	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	IGotoProcessor backwardProcessor;

	/** 
	 * <p>DOCUMENT ME! </p>
	 */
	IGotoProcessor forwardProcessor;

	/**
	 * Creates a new CompleteSliceGotoProcessor object.
	 *
	 * @param collector DOCUMENT ME!
	 */
	CompleteSliceGotoProcessor(final TaggingBasedSliceCollector collector) {
		backwardProcessor = new SliceGotoProcessor(collector, true);
		forwardProcessor = new SliceGotoProcessor(collector, false);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#postprocess()
	 */
	public void postprocess() {
		backwardProcessor.postprocess();
		forwardProcessor.postprocess();
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#preprocess(soot.SootMethod)
	 */
	public void preprocess(final SootMethod method) {
		backwardProcessor.preprocess(method);
		forwardProcessor.preprocess(method);
	}

	/**
	 * @see edu.ksu.cis.indus.slicer.IGotoProcessor#process(BasicBlock)
	 */
	public void process(final BasicBlock bbg) {
		backwardProcessor.process(bbg);
		forwardProcessor.process(bbg);
	}
}

/*
   ChangeLog:
   $Log$
 */
