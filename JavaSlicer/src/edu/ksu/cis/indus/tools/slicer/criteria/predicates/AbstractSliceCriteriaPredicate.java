/*******************************************************************************
 * Indus, a program analysis and transformation toolkit for Java.
 * Copyright (c) 2001, 2007 Venkatesh Prasad Ranganath
 * 
 * All rights reserved.  This program and the accompanying materials are made 
 * available under the terms of the Eclipse Public License v1.0 which accompanies 
 * the distribution containing this program, and is available at 
 * http://www.opensource.org/licenses/eclipse-1.0.php.
 * 
 * For questions about the license, copyright, and software, contact 
 * 	Venkatesh Prasad Ranganath at venkateshprasad.ranganath@gmail.com
 *                                 
 * This software was developed by Venkatesh Prasad Ranganath in SAnToS Laboratory 
 * at Kansas State University.
 *******************************************************************************/

package edu.ksu.cis.indus.tools.slicer.criteria.predicates;

import edu.ksu.cis.indus.tools.slicer.SlicerTool;

/**
 * This is an abstract implementation of slice criteria filter.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 * @param <T> is the type of the input objects to the predicate.
 */
public abstract class AbstractSliceCriteriaPredicate<T>
		implements ISliceCriteriaPredicate<T> {

	/**
	 * This is the slicer tool provides the context in which filtering occurs.
	 */
	private SlicerTool<?> slicerTool;

	/**
	 * {@inheritDoc}
	 */
	public void setSlicerTool(final SlicerTool<?> slicer) {
		slicerTool = slicer;
	}

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 * 
	 * @return the value in <code>slicerTool</code>.
	 */
	protected final SlicerTool<?> getSlicerTool() {
		return slicerTool;
	}
}

// End of File
