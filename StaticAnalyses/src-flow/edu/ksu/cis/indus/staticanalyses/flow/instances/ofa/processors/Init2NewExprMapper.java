
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

import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.ProcessingController;

import edu.ksu.cis.indus.staticanalyses.interfaces.IValueAnalyzer;
import edu.ksu.cis.indus.staticanalyses.processing.AbstractValueAnalyzerBasedProcessor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.NewExpr;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;


/**
 * This class maps a new instance creation expression to the invocation site that calls the constructor on the created
 * instance. In Jimple, one can pick the new expression and pick the immediate following statement with
 * <code>&lt;init&gt;</code> invocation expression.  Note that both these statements should occur in the same method.
 * However, this can be incorrect in some cases.  A more sound approach is to this approach and only pair the new expression
 * and the invocation expression only when the object created at the new expression flows into the primary at the
 * invocation site. We use object flow analysis for this purpose.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Init2NewExprMapper
  extends AbstractValueAnalyzerBasedProcessor {
	/**
	 * This is a cache of the context.
	 */
	private Context contextCache = new Context();

	/**
	 * This is the object flow information to be used to improve precision.
	 */
	private IValueAnalyzer ofa;

	/**
	 * This maps methods to a map from new expression occurring statement to init invocation expression occurring statement.
	 *
	 * @invariant method2map != null
	 * @invariant method2map.oclIsKindOf(Map(SootMethod, Map(NewExpr, Stmt)))
	 * @invariant method2map.keySet()->forall(o | method2map.get(o)->forall(p | (p.getValue().containsInvokeExpr() &&
	 * 			  p.getValue().getInvokeExpr().oclIsKindOf(SpecialInvokeExpr))))
	 */
	private final Map method2map = new HashMap();

	/**
	 * @see IValueAnalyzerBasedProcessor#setAnalyzer(IValueAnalyzer)
	 */
	public void setAnalyzer(final IValueAnalyzer analyzer) {
		ofa = analyzer;
	}

	/**
	 * Retrieves the init invocation expression containing statement corresponding to the given new expression containing
	 * statement.
	 *
	 * @param newExprStmt is the statement with the new expression.
	 * @param method in which <code>newExprStmt</code> occurs.
	 *
	 * @return the statement in which the corresponding init invocation expression occurring statement
	 *
	 * @post result != null and result.contains(InvokeExpr) and result.getInvokeExpr().oclIsKindOf(SpecialInvokeExpr)
	 */
	public Stmt getInitCallStmtForNewExprStmt(final Stmt newExprStmt, final SootMethod method) {
		Map ne2init = (Map) method2map.get(method);
		Stmt result = null;

		if (ne2init != null) {
			result = (Stmt) ne2init.get(newExprStmt);
		}
		return result;
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.ValueBox, edu.ksu.cis.indus.processing.Context)
	 */
	public void callback(final ValueBox vBox, final Context context) {
		Value value = vBox.getValue();

		if (value instanceof NewExpr) {
			Stmt stmt = context.getStmt();
			SootMethod method = context.getCurrentMethod();

			Map ne2init = getMapFor(method);
			ne2init.put(value, stmt);
		} else if (value instanceof SpecialInvokeExpr) {
			Stmt stmt = context.getStmt();
			SootMethod method = context.getCurrentMethod();
			SpecialInvokeExpr expr = (SpecialInvokeExpr) value;
			SootMethod sm = expr.getMethod();

			if (sm.getName().equals("<init>")) {
				Map ne2init = getMapFor(method);
				contextCache.setRootMethod(method);
				contextCache.setStmt(stmt);
				contextCache.setProgramPoint(expr.getBaseBox());

				Collection values = ofa.getValues(expr.getBase(), contextCache);

				for (Iterator i = values.iterator(); i.hasNext();) {
					NewExpr e = (NewExpr) i.next();
					Stmt newStmt = (Stmt) ne2init.remove(e);
					ne2init.put(newStmt, stmt);
				}
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(NewExpr.class, this);
		ppc.register(SpecialInvokeExpr.class, this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		method2map.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(NewExpr.class, this);
		ppc.unregister(SpecialInvokeExpr.class, this);
	}

	/**
	 * Retrieves the new expression to init call map for the given method.
	 *
	 * @param method for which the map is requested.
	 *
	 * @return the map corresponding to the given method.
	 *
	 * @pre method != null
	 * @post result != null
	 * @post result->forall(o | result.get(o).containsInvokeExpr() &&
	 * 		 result.get(o).getInvokeExpr().oclIsKindOf(SpecialInvokeExpr))))
	 */
	private Map getMapFor(final SootMethod method) {
		Map result = (Map) method2map.get(method);

		if (result == null) {
			result = new HashMap();
			method2map.put(method, result);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2003/12/02 09:42:38  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.1  2003/11/22 00:42:22  venku
   - renamed InitResolved to Init2NewExprMapper.
   - added logic to realize the functionality.
   Revision 1.1  2003/11/20 08:22:33  venku
   - added support to include calls to <init> based on new expressions.
   - need to implement the class that provides this information.
 */
