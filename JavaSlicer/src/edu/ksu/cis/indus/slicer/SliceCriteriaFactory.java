
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;


/**
 * This class hides the work involved in the creation of slice criteria from the environment.
 * 
 * <p>
 * The interesting issue while  creating slicing criteria is the inclusion information.  Please refer to {@link SlicingEngine
 * SlicingEngine}  for the sort of slices we discuss here.  In case of expression-level slice criterion, inclusion would
 * mean  that the entire statement containing the expression needs to be included independent of the slice type, i.e.,
 * backward or complete.  The same applies to statement-level slice criterion.
 * </p>
 * 
 * <p>
 * On the otherhand, non-inclusion in the case complete slicing does not make sense.  However, in case of backward slicing
 * considering expresison-level slice criterion would mean that the expression should not be included in the sliced system
 * which only makes sense, but this introduces a dependency between the expression and the containing statement.  In
 * particular, the expression is control dependent on the statement, and so the statement should be included for capturing
 * control dependency.  This is exactly what  our implementation does.
 * </p>
 * 
 * <p>
 * On mentioning a statement as a non-inclusive slice criterion, we interpret that as all artifacts that affect the reaching
 * of that statement. On mentioning  an expression as a non-inclusive slice criterion, we interpret that as all artifacts
 * leading to the value of the expression in the statement  it occurs in, hence we will include the statement containing the
 * expression in a non-inclusive manner to capture control dependency leading to that statement.
 * </p>
 * 
 * <p>
 * As we are interested in meaningful slices, inclusive expression-level slice criterion is the same as inclusive
 * statement-level slice criterion which  refers to the statement that contains the expression.
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public final class SliceCriteriaFactory {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SliceCriteriaFactory.class);

	/**
	 * Creates slice criteria from the given value.  Every syntactic constructs related to storage at the program point are
	 * considered as slice criterion.  Note that the user should take care that the transformation phase based on slicing
	 * handles criteria generated by including all subexpressions.  Cloning based transformations may have issues handling
	 * such criteria.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt in which the criterion occurs.
	 * @param expression is the criterion.
	 *
	 * @return a collection of slice criterion objects corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null and expression != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Stmt stmt, final ValueBox expression) {
		final Collection _result = new HashSet();
		final SliceExpr _exprCriterion = SliceExpr.getSliceExpr();
		_exprCriterion.initialize(method, stmt, expression);
		_result.add(_exprCriterion);

		final SliceStmt _stmtCriterion = SliceStmt.getSliceStmt();
		_stmtCriterion.initialize(method, stmt);
		_result.add(_stmtCriterion);

		return _result;
	}

	/**
	 * Creates slice criteria from the given statement.  Every syntactic constructs related to storage in the statement are
	 * considered as slice criterion if <code>considerAll</code> is <code>true</code>.  If one is interested in the slice of
	 * control reaching the given statement, <code>considerAll</code> should be <code>false</code>.
	 *
	 * @param method in which the criterion occurs.
	 * @param stmt is the criterion.
	 *
	 * @return a collection of slice criterion corresponding to the given criterion.
	 *
	 * @pre method != null and stmt != null
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Stmt stmt) {
		final Collection _result = new HashSet();

		final SliceStmt _stmtCriterion = SliceStmt.getSliceStmt();
		_stmtCriterion.initialize(method, stmt);
		_result.add(_stmtCriterion);
		return _result;
	}

	/**
	 * Returns a collection of criteria which include all occurrences of the given local in the given method.
	 *
	 * @param method in which the <code>local</code> occurs.
	 * @param local is the local variable whose all occurrences in <code>method</code> should be captured as slice criterion
	 *
	 * @return a collection of slice criteria.
	 *
	 * @post result.oclIsKindOf(Collection(AbstractSliceCriterion))
	 */
	public Collection getCriterion(final SootMethod method, final Local local) {
		Collection _result = Collections.EMPTY_LIST;

		final Body _body = method.getActiveBody();

		if (_body != null) {
			_result = new HashSet();

			for (final Iterator _i = _body.getUnits().iterator(); _i.hasNext();) {
				final  Stmt _stmt = (Stmt) _i.next();

				for (final Iterator _j = _stmt.getUseAndDefBoxes().iterator(); _j.hasNext();) {
					final ValueBox _vBox = (ValueBox) _j.next();

					if (_vBox.getValue().equals(local)) {
						final SliceExpr _exprCriterion = SliceExpr.getSliceExpr();
						_exprCriterion.initialize(method, _stmt, _vBox);
						_result.add(_exprCriterion);
					}
				}
			}
		} else {
			LOGGER.error(method.getSignature() + " does not have a body.");
		}

		return _result;
	}

	/**
	 * Checks if the given object is a slicing criterion.
	 *
	 * @param o to be checked.
	 *
	 * @return <code>true</code> if <code>o</code> is a slicing criterion; <code>false</code>, otherwise.
	 */
	public static boolean isSlicingCriterion(final Object o) {
		boolean _result = false;

		if (o != null) {
			_result = o instanceof ISliceCriterion;
		}
		return _result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.5  2003/12/02 19:20:50  venku
   - coding convention and formatting.

   Revision 1.4  2003/12/02 09:42:18  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2

   Revision 1.3  2003/12/01 12:18:25  venku
   - as criteria creation is only the function of the factory
     and as setConsider..() method is available, the
     responsibility to tweak the purpose of the criteria
     is pushed out of the factory.
   Revision 1.2  2003/11/05 08:31:33  venku
   - we only support before and after type of criteria.  This
     includes the cases of inclusion in slice.
   - the factory only creates criteria at a coarse level as
     specified by the expression/statement.  It relies on
     the slicing algorithm to drill down into the expr/stmt
     for more criteria.
   Revision 1.1  2003/10/13 00:58:04  venku
 *** empty log message ***
                 Revision 1.11  2003/09/27 22:38:30  venku
                 - package documentation.
                 - formatting.
                 Revision 1.10  2003/09/15 08:09:17  venku
                 - fixed param dependency.  However, this needs to be addressed
                   in a generic setting.  Also, the theoretics concerned to inclusion
                   should be dealt appropriately.
                 Revision 1.9  2003/08/21 09:31:52  venku
                 If the SliceExpr was created based on a Def Box, it would not have
                 included the statement.  This was fixed.
                 Revision 1.8  2003/08/20 18:31:22  venku
                 Documentation errors fixed.
                 Revision 1.7  2003/08/18 12:14:13  venku
                 Well, to start with the slicer implementation is complete.
                 Although not necessarily bug free, hoping to stabilize it quickly.
                 Revision 1.6  2003/08/18 05:01:45  venku
                 Committing package name change in source after they were moved.
                 Revision 1.5  2003/08/18 04:56:47  venku
                 Spruced up Documentation and specification.
                 But committing before moving slicer under transformation umbrella of Indus.
                 Revision 1.4  2003/05/22 22:23:49  venku
                 Changed interface names to start with a "I".
                 Formatting.
 */
