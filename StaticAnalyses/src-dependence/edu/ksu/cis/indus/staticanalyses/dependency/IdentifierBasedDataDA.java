
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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.common.datastructures.Pair;

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.InitializationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Local;
import soot.SootMethod;
import soot.Value;
import soot.ValueBox;

import soot.jimple.DefinitionStmt;
import soot.jimple.Stmt;

import soot.toolkits.graph.UnitGraph;

import soot.toolkits.scalar.SimpleLocalDefs;
import soot.toolkits.scalar.SimpleLocalUses;
import soot.toolkits.scalar.UnitValueBoxPair;


/**
 * This class provides data dependency information based on identifiers.  Local variables in a method enable such dependence.
 * Given a def site, the use site is tracked based on the id being defined and used. Hence, information about field/array
 * access via primaries which are local variables is inaccurate in such a setting, hence, it is not  provided by this class.
 * Please refer to {@link ReferenceBasedDataDA ReferenceBasedDataDA} for such information.
 * 
 * <p>
 * This implementation is based on <code>soot.toolkits.scalar.SimpleLocalDefs</code> and
 * <code>soot.toolkits.scalar.SimpleLocalUses</code> classes .
 * </p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 *
 * @invariant dependee2dependent.oclIsKindOf(Map(SootMethod,Sequence(Set(Stmt)))
 * @invariant dependee2dependent.values()->forall(o | o.getValue().size =
 * 			  o.getKey().getBody(Jimple.v()).getStmtList().size())
 * @invariant dependent2dependee.oclIsKindOf(Map(SootMethod, Sequence(Map(Local, Set(Stmt)))))
 * @invariant dependent2dependee.entrySet()->forall(o | o.getValue().size() =
 * 			  o.getKey().getBody(Jimple.v()).getStmtList().size())
 */
public class IdentifierBasedDataDA
  extends AbstractDependencyAnalysis {
	/** 
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(IdentifierBasedDataDA.class);

	/** 
	 * This provides call graph information.
	 */
	private ICallGraphInfo callgraph;

	/**
	 * Returns  the statements on which <code>o</code>, depends in the given <code>method</code>.
	 *
	 * @param programPoint is the program point at which a local occurs in the statement.  If it is a statement, then
	 * 		  information about all the locals in the statement is provided.  If it is a pair of statement and program point
	 * 		  in it, then only  information about the local at that program point is provided.
	 * @param method in which <code>programPoint</code> occurs.
	 *
	 * @return a collection of statements on which <code>programPoint</code> depends.
	 *
	 * @pre programPoint.oclIsKindOf(Pair(Stmt, Local)) implies programPoint.oclTypeOf(Pair).getFirst() != null and
	 * 		programPoint.oclTypeOf(Pair).getSecond() != null
	 * @pre programPoint.oclIsKindOf(Stmt) or programPoint.oclIsKindOf(Pair(Stmt, Local))
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(DefinitionStmt))
	 */
	public Collection getDependees(final Object programPoint, final Object method) {
		Collection _result = Collections.EMPTY_LIST;

		if (programPoint instanceof Stmt) {
			_result = new HashSet();

			final Stmt _stmt = (Stmt) programPoint;
			final SootMethod _m = (SootMethod) method;
			final List _dependees = (List) dependent2dependee.get(method);

			if (_dependees != null) {
				final Map _local2defs = (Map) _dependees.get(getStmtList(_m).indexOf(_stmt));

				for (final Iterator _i = _stmt.getUseBoxes().iterator(); _i.hasNext();) {
					final Value _o = ((ValueBox) _i.next()).getValue();
					final Collection _c = (Collection) _local2defs.get(_o);

					if (_c != null) {
						_result.addAll(_c);
					}
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("No dependence information available for " + _stmt + " in " + method);
				}
			}
		} else if (programPoint instanceof Pair) {
			final Pair _pair = (Pair) programPoint;
			final Stmt _stmt = (Stmt) _pair.getFirst();
			final Local _local = (Local) _pair.getSecond();
			final SootMethod _m = (SootMethod) method;
			final List _dependees = (List) dependent2dependee.get(method);
			final Map _local2defs = (Map) _dependees.get(getStmtList(_m).indexOf(_stmt));
			final Collection _c = (Collection) _local2defs.get(_local);

			if (_c != null) {
				_result = Collections.unmodifiableCollection(_c);
			}
		}
		return _result;
	}

	/**
	 * Returns the statement and the program point in it which depends on <code>stmt</code> in the given
	 * <code>context</code>. The context is the method in which the o occurs.
	 *
	 * @param stmt is a definition statement.
	 * @param method is the method in which <code>stmt</code> occurs.
	 *
	 * @return a collection of statement and program points in them which depend on the definition in <code>stmt</code>.
	 *
	 * @pre stmt.isOclKindOf(Stmt)
	 * @pre method.oclIsTypeOf(SootMethod)
	 * @post result->forall(o | o.isOclKindOf(Stmt))
	 */
	public Collection getDependents(final Object stmt, final Object method) {
		final SootMethod _sm = (SootMethod) method;
		final List _dependents = (List) dependee2dependent.get(_sm);
		Collection _result = Collections.EMPTY_LIST;

		if (_dependents != null) {
			final Collection _temp = (Collection) _dependents.get(getStmtList(_sm).indexOf(stmt));

			if (_temp != null) {
				_result = Collections.unmodifiableCollection(_temp);
			}
		}
		return _result;
	}

	/**
	 * {@inheritDoc}  This implementation is bi-directional.
	 */
	public Object getDirection() {
		return BI_DIRECTIONAL;
	}

	/*
	 * The dependent information is stored as follows: For each method, a list of length equal to the number of statements in
	 * the methods is maintained. In case of dependent information, at each location corresponding to the statement a set of
	 * dependent statements is maintained in the list.  In case of dependee information, at each location corresponding to the
	 * statement a map is maintained in the list.  The map maps a value box in the statement to a collection of dependee
	 * statements.
	 *
	 * The rational for the way the information is maintained is only one local can be defined in a statement.  Also, if the
	 * definition of a local reaches a statement, then all occurrences of that local at that statement must be dependent on
	 * the same reaching def.
	 */

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#getIds()
	 */
	public Collection getIds() {
		return Collections.singleton(IDependencyAnalysis.IDENTIFIER_BASED_DATA_DA);
	}

	/**
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.IDependencyAnalysis#getIndirectVersionOfDependence()
	 */
	public IDependencyAnalysis getIndirectVersionOfDependence() {
		return new IndirectDependenceAnalysis(this, IDependenceRetriever.STMT_DEP_RETRIEVER);
	}

	/**
	 * Calculates the dependency information for locals in the methods provided during initialization.
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.dependency.AbstractDependencyAnalysis#analyze()
	 */
	public void analyze() {
		unstable();

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("BEGIN: Identifier Based Data Dependence processing");
		}

		for (final Iterator _i = callgraph.getReachableMethods().iterator(); _i.hasNext();) {
			final SootMethod _currMethod = (SootMethod) _i.next();
			final UnitGraph _unitGraph = getUnitGraph(_currMethod);

			if (_unitGraph == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Method " + _currMethod.getSignature() + " does not have a unit graph.");
				}
				continue;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Processing " + _currMethod.getSignature());
			}

			calculateDAForMethod(_currMethod, _unitGraph);
		}
		stable();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("analyze() - " + toString());
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("END:  Identifier Based Data Dependence processing");
		}
	}

	///CLOVER:OFF

	/**
	 * Returns a stringized representation of this analysis.  The representation includes the results of the analysis.
	 *
	 * @return a stringized representation of this object.
	 */
	public String toString() {
		final StringBuffer _result =
			new StringBuffer("Statistics for Identifier-based Data dependence as calculated by " + this.getClass().getName()
				+ "\n");
		int _localEdgeCount = 0;
		int _edgeCount = 0;

		final StringBuffer _temp = new StringBuffer();

		for (final Iterator _i = dependee2dependent.entrySet().iterator(); _i.hasNext();) {
			final Map.Entry _entry = (Map.Entry) _i.next();
			_localEdgeCount = 0;

			final List _stmts = getStmtList((SootMethod) _entry.getKey());
			int _count = 0;

			for (final Iterator _j = ((Collection) _entry.getValue()).iterator(); _j.hasNext();) {
				final Collection _c = (Collection) _j.next();
				final Stmt _stmt = (Stmt) _stmts.get(_count++);

				for (final Iterator _k = _c.iterator(); _k.hasNext();) {
					_temp.append("\t\t" + _stmt + " <-- " + _k.next() + "\n");
				}
				_localEdgeCount += _c.size();
			}
			_result.append("\tFor " + _entry.getKey() + " there are " + _localEdgeCount
				+ " Identifier-based Data dependence edges.\n");
			_result.append(_temp);
			_temp.delete(0, _temp.length());
			_edgeCount += _localEdgeCount;
		}
		_result.append("A total of " + _edgeCount + " Identifier-based Data dependence edges exist.");
		return _result.toString();
	}

	///CLOVER:ON

	/**
	 * Sets up internal data structures.
	 *
	 * @throws InitializationException when call graph service is not provided.
	 *
	 * @pre info.get(ICallGraphInfo.ID) != null and info.get(ICallGraphInfo.ID).oclIsTypeOf(ICallGraphInfo)
	 *
	 * @see edu.ksu.cis.indus.staticanalyses.interfaces.AbstractAnalysis#setup()
	 */
	protected void setup()
	  throws InitializationException {
		super.setup();
		callgraph = (ICallGraphInfo) info.get(ICallGraphInfo.ID);

		if (callgraph == null) {
			throw new InitializationException(ICallGraphInfo.ID + " was not provided.");
		}
	}

	/**
	 * Calculates dependence info for the given method.
	 *
	 * @param method for which to calculate dependence.
	 * @param unitGraph of <code>method</code>.
	 *
	 * @pre method != null and unitGraph != null
	 */
	private void calculateDAForMethod(final SootMethod method, final UnitGraph unitGraph) {
		final SimpleLocalDefs _defs = new SimpleLocalDefs(unitGraph);
		final SimpleLocalUses _uses = new SimpleLocalUses(unitGraph, _defs);
		final Collection _t = getStmtList(method);
		final List _dependees = new ArrayList(_t.size());
		final List _dependents = new ArrayList(_t.size());

		for (final Iterator _j = _t.iterator(); _j.hasNext();) {
			final Stmt _currStmt = (Stmt) _j.next();
			Collection _currUses = Collections.EMPTY_LIST;

			if (_currStmt instanceof DefinitionStmt) {
				final Collection _temp = _uses.getUsesOf(_currStmt);

				if (_temp.size() != 0) {
					_currUses = new ArrayList();

					for (final Iterator _k = _temp.iterator(); _k.hasNext();) {
						final UnitValueBoxPair _p = (UnitValueBoxPair) _k.next();
						_currUses.add(_p.getUnit());
					}
				}
			}
			_dependents.add(_currUses);

			Map _currDefs = Collections.EMPTY_MAP;

			if (_currStmt.getUseBoxes().size() > 0) {
				_currDefs = new HashMap();

				for (final Iterator _k = _currStmt.getUseBoxes().iterator(); _k.hasNext();) {
					final ValueBox _currValueBox = (ValueBox) _k.next();
					final Value _value = _currValueBox.getValue();

					if (_value instanceof Local && !_currDefs.containsKey(_value)) {
						_currDefs.put(_value, _defs.getDefsOfAt((Local) _value, _currStmt));
					}
				}
			}
			_dependees.add(_currDefs);
		}
		dependee2dependent.put(method, _dependents);
		dependent2dependee.put(method, _dependees);
	}
}

// End of File
