
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

package edu.ksu.cis.indus.tools.slicer.criteria.generators;

import edu.ksu.cis.indus.processing.Context;

import edu.ksu.cis.indus.tools.slicer.ISliceCriteriaContextualizer;
import edu.ksu.cis.indus.tools.slicer.SlicerTool;
import edu.ksu.cis.indus.tools.slicer.criteria.filters.ISliceCriteriaFilter;

import java.util.Collection;

import soot.SootMethod;


/**
 * This is an abstract implementation of <code>ISliceCriteriaGenerator</code>.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public abstract class AbstractSliceCriteriaGenerator
  implements ISliceCriteriaGenerator {
	/** 
	 * The contextualizer to use.
	 */
	private ISliceCriteriaContextualizer contextualizer;

	/** 
	 * The filter to use.
	 */
	private ISliceCriteriaFilter criteriaFilter;

	/** 
	 * The slicer that defines the context in which generator functions.
	 */
	private SlicerTool slicerTool;

	/**
	 * @see edu.ksu.cis.indus.tools.slicer.criteria.generators.ISliceCriteriaGenerator#getCriteria(edu.ksu.cis.indus.tools.slicer.SlicerTool)
	 */
	public final Collection getCriteria(final SlicerTool slicer) {
		slicerTool = slicer;

		if (contextualizer != null) {
			contextualizer.setSlicerTool(slicerTool);
		}

		if (criteriaFilter != null) {
			criteriaFilter.setSlicerTool(slicerTool);
		}
		return getCriteriaTemplateMethod();
	}

	/**
	 * @see ISliceCriteriaGenerator#setCriteriaContextualizer(ISliceCriteriaContextualizer)
	 */
	public final void setCriteriaContextualizer(final ISliceCriteriaContextualizer theContextualizer) {
		contextualizer = theContextualizer;
	}

	/**
	 * @see ISliceCriteriaGenerator#setCriteriaFilter(ISliceCriteriaFilter)
	 */
	public final void setCriteriaFilter(final ISliceCriteriaFilter theCriteriaFilter) {
		criteriaFilter = theCriteriaFilter;
	}

	/**
	 * Contextualizes the given criteria.
	 *
	 * @param context in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized.  This collection will be modified upon return
	 * 		  with contextualized criteria.
	 *
	 * @pre context != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 * @post baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected final void contextualizeCriteriaBasedOnProgramPoint(final Context context, final Collection baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnProgramPoint(context, baseCriteria);
		}
	}

	/**
	 * Contextualizes the given criteria.
	 *
	 * @param method in which <code>baseCriteria</code> were generated.
	 * @param baseCriteria is the collection of criteria to be contextualized.  This collection will be modified upon return
	 * 		  with contextualized criteria.
	 *
	 * @pre context != null and baseCriteria != null
	 * @pre baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 * @post baseCriteria.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected final void contextualizeCriteriaBasedOnThis(final SootMethod method, final Collection baseCriteria) {
		if (contextualizer != null) {
			contextualizer.processCriteriaBasedOnThis(method, baseCriteria);
		}
	}

	/**
	 * This is a template method that the subclasses should implement to generate the criteria.
	 *
	 * @return a collection of criteria.
	 *
	 * @post result != null and result.oclIsKindOf(Collection(ISliceCriterion))
	 */
	protected abstract Collection getCriteriaTemplateMethod();

	/**
	 * Retrieves the value in <code>slicerTool</code>.
	 *
	 * @return the value in <code>slicerTool</code>.
	 */
	protected SlicerTool getSlicerTool() {
		return slicerTool;
	}

	/**
	 * Checks if  criteria should be generated based on the given entity.
	 *
	 * @param entity forms the base for the criteria.
	 *
	 * @return <code>true</code>
	 *
	 * @pre entity != null and slicer != null
	 */
	protected final boolean shouldGenerateCriteriaFrom(final Object entity) {
		final boolean _result;

		if (criteriaFilter != null) {
			_result = criteriaFilter.shouldGenerateCriteriaFrom(entity);
		} else {
			_result = true;
		}
		return _result;
	}
}

// End of File