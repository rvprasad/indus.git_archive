
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

package edu.ksu.cis.indus.staticanalyses.concurrency.escape;

import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.processing.Context;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.SootMethod;
import soot.Value;

import soot.jimple.ArrayRef;
import soot.jimple.InstanceFieldRef;
import soot.jimple.MonitorStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.Stmt;
import soot.jimple.VirtualInvokeExpr;


/**
 * This implementation facilitates the extraction of calling-contexts based on multithread data sharing (more precise than
 * escape information).
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class ThreadEscapeInfoBasedCallingContextRetrieverV2
  extends ThreadEscapeInfoBasedCallingContextRetriever {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(ThreadEscapeInfoBasedCallingContextRetrieverV2.class);

	/**
	 * Creates an instance of this instance.
	 *
	 * @param callContextLenLimit <i>refer to the constructor of the super class</i>.
	 */
	public ThreadEscapeInfoBasedCallingContextRetrieverV2(final int callContextLenLimit) {
		super(callContextLenLimit);
	}

	/**
	 * @see ThreadEscapeInfoBasedCallingContextRetriever#considerProgramPoint(edu.ksu.cis.indus.processing.Context)
	 */
	protected boolean considerProgramPoint(final Context context) {
		final Value _value = context.getProgramPoint().getValue();
		final Stmt _stmt = context.getStmt();
		final SootMethod _currentMethod = context.getCurrentMethod();
		boolean _result = false;

		if (_value instanceof InstanceFieldRef) {
			_result = escapesInfo.fieldAccessShared(((InstanceFieldRef) _value).getBase(), _currentMethod);
		} else if (_value instanceof ArrayRef) {
			_result = escapesInfo.fieldAccessShared(((ArrayRef) _value).getBase(), _currentMethod);
		} else if (_value instanceof StaticFieldRef) {
			_result = escapesInfo.fieldAccessShared(_value, _currentMethod);
		} else if (_stmt instanceof MonitorStmt) {
			_result = escapesInfo.lockUnlockShared(((MonitorStmt) _stmt).getOp(), _currentMethod);
		} else if (_value instanceof VirtualInvokeExpr
			  && (Util.isWaitMethod(((VirtualInvokeExpr) _value).getMethod())
			  || Util.isNotifyMethod(((VirtualInvokeExpr) _value).getMethod()))) {
			final Value _base = ((VirtualInvokeExpr) _value).getBase();

			if (_base == _value) {
				_result = escapesInfo.waitNotifyShared(_value, _currentMethod);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerProgramPoint() - result =" + _result);
		}

		return _result;
	}

	/**
	 * @see ThreadEscapeInfoBasedCallingContextRetriever#considerThis(Context)
	 */
	protected boolean considerThis(final Context methodContext) {
		final SootMethod _method = methodContext.getCurrentMethod();
		final boolean _result =
			escapesInfo.thisFieldAccessShared(_method) || escapesInfo.thisWaitNotifyShared(_method)
			  || escapesInfo.thisLockUnlockShared(_method);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("considerThis() -  : _result = " + _result);
		}

		return _result;
	}
}

// End of File
