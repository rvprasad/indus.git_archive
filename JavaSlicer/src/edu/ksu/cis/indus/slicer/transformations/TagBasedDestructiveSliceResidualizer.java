
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

package edu.ksu.cis.indus.slicer.transformations;

import edu.ksu.cis.indus.common.CollectionsUtilities;
import edu.ksu.cis.indus.common.datastructures.Pair;
import edu.ksu.cis.indus.common.soot.NamedTag;
import edu.ksu.cis.indus.common.soot.Util;

import edu.ksu.cis.indus.processing.AbstractProcessor;
import edu.ksu.cis.indus.processing.Context;
import edu.ksu.cis.indus.processing.Environment;
import edu.ksu.cis.indus.processing.ProcessingController;
import edu.ksu.cis.indus.processing.TagBasedProcessingFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import soot.Body;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.TrapManager;
import soot.Type;
import soot.Value;
import soot.ValueBox;
import soot.VoidType;

import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;

import soot.jimple.toolkits.scalar.ConditionalBranchFolder;
import soot.jimple.toolkits.scalar.LocalCreation;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnconditionalBranchFolder;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;

import soot.tagkit.Host;
import soot.tagkit.Tag;

import soot.toolkits.scalar.UnusedLocalEliminator;

import soot.util.Chain;


/**
 * This class residualizes the slice by destructively updating the system from which the slice was built.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class TagBasedDestructiveSliceResidualizer
  extends AbstractProcessor {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	static final Log LOGGER = LogFactory.getLog(TagBasedDestructiveSliceResidualizer.class);

	/**
	 * This tracks the locals of the current method that should be deleted.
	 */
	final Collection localsToKeep = new HashSet();

	/**
	 * This tracks the methods of the current class that should be deleted.
	 *
	 * @invariant methodsToKill.oclIsKindOf(Collection(SootMethod))
	 */
	final Collection methodsToKill = new HashSet();

	/**
	 * This is the traps of a method that need to be retained.
	 *
	 * @invariant trapsToRetain != null
	 * @invariant trapsToRetain.oclIsKindOf(Set(Trap))
	 */
	final Collection trapsToRetain = new HashSet();

	/**
	 * This is a mapping from classes to it's members that should be removed.
	 *
	 * @invariant class2membersToKill.oclIsKindOf(Map(SootClass, Pair(Collection(SootMethod), Collection(SootField))))
	 */
	final Map class2members = new HashMap();

	/**
	 * This maps statements in the system to new statements that should be included in the slice.
	 *
	 * @invariant oldStmt2newStmt != null
	 * @invariant oldStmt2newStmt.oclIsKindOf(Map(Stmt, Stmt))
	 */
	final Map oldStmt2newStmt = new HashMap();

	/**
	 * This maps a statement to a sequence of statements that need to be inserted before the key statement the statement
	 * sequence of the method body.
	 *
	 * @invariant stmt2predecessors.oclIsKindOf(Map(Stmt, Sequence(Stmt)))
	 */
	final Map stmt2predecessors = new HashMap();

	/**
	 * The system to be residualized.
	 */
	Scene theScene;

	/**
	 * The method being processed.
	 */
	SootMethod currMethod;

	/**
	 * The name of the tag used to identify the parts of the system to be residualized.
	 */
	String tagToResidualize;

	/**
	 * The collection of classes to be removed from the system after residualization.
	 */
	private final Collection classesToKill = new HashSet();

	/**
	 * This tracks the fields of the current class that should be deleted.
	 *
	 * @invariant fieldsToKill.oclIsKindOf(Collection(SootField))
	 */
	private final Collection fieldsToKill = new HashSet();

	/**
	 * This is used to process the statements during residualization.
	 */
	private final StmtResidualizer stmtProcessor = new StmtResidualizer();

	/**
	 * This is the collection of statements which are to be replaced by NOP statements in the residue.
	 *
	 * @invariant stmtsToBeNOPed.oclIsKindOf(Collection(Stmt))
	 */
	private List stmtsToBeNOPed = new ArrayList();

	/**
	 * The class being processed up until now.
	 */
	private SootClass currClass;

	/**
	 * This class residualizes statements.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class StmtResidualizer
	  extends AbstractStmtSwitch {
		/*
		 * THINK: In enter/exit montior, invoke, and conditional statements, there is no meaning in marking the statement as
		 * required and not the containing expression.  (Or can it be?) Hence, for such statements we assume that the
		 * containing expressions will be residualized. If so, then we can leave the expression untouched.  Only in the case
		 * of invocation statement do we need to process the arguments to the call.
		 *
		 * TODO: Depending on the relevant branches of the conditional various branches of the conditional can be sliced to
		 * improve precision.
		 */

		/**
		 * This is the instance to be used to residualize expressions/values.
		 */
		final ValueResidualizer valueProcessor = new ValueResidualizer();

		/**
		 * @see soot.jimple.StmtSwitch#caseAssignStmt(soot.jimple.AssignStmt)
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseIdentityStmt(soot.jimple.IdentityStmt)
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			residualizeDefStmt(stmt);
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseInvokeStmt(soot.jimple.InvokeStmt)
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			valueProcessor.residualize(stmt.getInvokeExprBox());
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseLookupSwitchStmt(soot.jimple.LookupSwitchStmt)
		 */
		public void caseLookupSwitchStmt(final LookupSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseReturnStmt(soot.jimple.ReturnStmt)
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setOp(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseTableSwitchStmt(soot.jimple.TableSwitchStmt)
		 */
		public void caseTableSwitchStmt(final TableSwitchStmt stmt) {
			final ValueBox _vBox = stmt.getKeyBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				stmt.setKey(Util.getDefaultValueFor(_vBox.getValue().getType()));
			}
		}

		/**
		 * @see soot.jimple.StmtSwitch#caseThrowStmt(soot.jimple.ThrowStmt)
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			final ValueBox _vBox = stmt.getOpBox();

			if (!((Host) _vBox).hasTag(tagToResidualize)) {
				final Value _val = stmt.getOp();
				final RefType _type = (RefType) _val.getType();
				final Jimple _jimple = Jimple.v();

				// add a new local to the body
				final LocalCreation _lc = new LocalCreation(currMethod.getActiveBody().getLocals());
				final Local _local = _lc.newLocal(_type);
				localsToKeep.add(_local);

				// create an exception of the thrown type and assign it to the created local.
				final NewExpr _newExpr = _jimple.newNewExpr(_type);
				final AssignStmt _astmt = _jimple.newAssignStmt(_local, _newExpr);
				final Tag _tag = new NamedTag(tagToResidualize);
				_astmt.addTag(_tag);
				_astmt.getLeftOpBox().addTag(_tag);
				_astmt.getRightOpBox().addTag(_tag);

				// find an <init> method on the type and prepare the argument list
				final SootClass _clazz = _type.getSootClass();
				final SootMethod _init = prepareInitIn(_clazz);
				final List _args = new ArrayList(_init.getParameterCount());

				for (int _i = _init.getParameterCount() - 1; _i >= 0; _i--) {
					_args.add(Util.getDefaultValueFor(_init.getParameterType(_i)));
				}

				// invoke <init> on the local
				final InvokeExpr _iexpr = _jimple.newSpecialInvokeExpr(_local, _init, _args);
				final InvokeStmt _istmt = _jimple.newInvokeStmt(_iexpr);
				_istmt.addTag(_tag);
				_istmt.getInvokeExprBox().addTag(_tag);
				((SpecialInvokeExpr) _iexpr).getBaseBox().addTag(_tag);

				// prepare a new list of statements of predecessors to be inserted before the throw statement in the final
				// body of the method.
				final List _stmts = new ArrayList();
				_stmts.add(_astmt);
				_stmts.add(_istmt);
				stmt2predecessors.put(stmt, _stmts);
				stmt.setOp(_local);
			}
		}

		/**
		 * Residualizes the given statement.
		 *
		 * @param stmt to be residualized.
		 *
		 * @return <code>true</code> if the statement should be replaced by the result in the residue; <code>false</code>,
		 * 		   otherwise.
		 *
		 * @pre stmt != null
		 */
		boolean residualize(final Stmt stmt) {
			boolean _result = false;

			if (stmt.hasTag(tagToResidualize)) {
				setResult(null);
				stmt.apply(this);

				final Stmt _newStmt = (Stmt) getResult();

				if (_newStmt != null) {
					oldStmt2newStmt.put(stmt, _newStmt);
				}
				_result = true;
			}
			return _result;
		}

		/**
		 * Sucks in vanilla &lt;init&gt; methods if it exists in the given class.  If not it will insert one.  It will also
		 * massage the class hierarchy branch to be executable.
		 *
		 * @param clazz in which we are searching for the constructor.
		 *
		 * @return the constructor method.
		 *
		 * @pre clazz != null
		 * @post result != null
		 */
		private SootMethod prepareInitIn(final SootClass clazz) {
			SootMethod _superinit = null;

			if (clazz.hasSuperclass()) {
				_superinit = prepareInitIn(clazz.getSuperclass());
			}

			SootMethod _init = clazz.getMethod("<init>", Collections.EMPTY_LIST, VoidType.v());
			final boolean _existsButIsNotIncluded = _init != null ? !_init.hasTag(tagToResidualize)
																  : false;

			if (_init == null || _existsButIsNotIncluded) {
				final Tag _tag = new NamedTag(tagToResidualize);

				if (_existsButIsNotIncluded) {
					clazz.removeMethod(_init);

					final Pair _freshPair = new Pair(new ArrayList(), new ArrayList());
					final Pair _pair = (Pair) CollectionsUtilities.getFromMap(class2members, clazz, _freshPair);
					final Collection _clazzMethodsToKill = (Collection) _pair.getFirst();
					_clazzMethodsToKill.remove(_init);
				}

				// SPECIALIZATION: Specialization from here on.
				_init = new SootMethod("<init>", Collections.EMPTY_LIST, VoidType.v());
				_init.addTag(_tag);

				final Jimple _jimple = Jimple.v();
				final Body _body = _jimple.newBody(_init);

				if (_superinit != null) {
					final RefType _clazzType = RefType.v(clazz);
					final LocalCreation _lc = new LocalCreation(_body.getLocals());
					final Local _this = _lc.newLocal("_this", _clazzType);
					final IdentityStmt _astmt = _jimple.newIdentityStmt(_this, _jimple.newThisRef(_clazzType));
					_astmt.addTag(_tag);
					_astmt.getLeftOpBox().addTag(_tag);
					_astmt.getRightOpBox().addTag(_tag);
					_body.getUnits().add(_astmt);

					final InvokeExpr _iexpr = _jimple.newSpecialInvokeExpr(_this, _superinit);
					final InvokeStmt _istmt = _jimple.newInvokeStmt(_iexpr);
					_istmt.addTag(_tag);
					_istmt.getInvokeExprBox().addTag(_tag);
					((SpecialInvokeExpr) _iexpr).getBaseBox().addTag(_tag);
					_body.getUnits().add(_istmt);
				}

				final Stmt _retStmt = _jimple.newReturnVoidStmt();
				_retStmt.addTag(_tag);
				_body.getUnits().add(_retStmt);
				_init.setActiveBody(_body);
				clazz.addMethod(_init);

				if (!clazz.hasTag(_tag.getName())) {
					clazz.addTag(_tag);
				}
			}

			return _init;
		}

		/**
		 * Residualize the definition statement.
		 *
		 * @param stmt to be residualized.
		 *
		 * @throws IllegalStateException when the rhs is included in the slice and the lhs is not included in the slice.
		 *
		 * @pre stmt != null
		 */
		private void residualizeDefStmt(final DefinitionStmt stmt) {
			if (!stmt.getLeftOpBox().hasTag(tagToResidualize)) {
				final ValueBox _rightOpBox = stmt.getRightOpBox();

				/*
				 * If the definition statement is marked and the lhs is unmarked then the rhs should be a marked invoke expr.
				 */
				if (_rightOpBox.hasTag(tagToResidualize) && !stmt.containsInvokeExpr()) {
					final String _message =
						"Incorrect slice.  "
						+ "How can a def statement and it's non-invoke rhs be marked with the lhs unmarked? ->" + stmt;
					LOGGER.error(_message);
					throw new IllegalStateException(_message);
				}

				final Jimple _jimple = Jimple.v();

				if (stmt.containsInvokeExpr()) {
					final Value _expr = stmt.getRightOp();
					final Stmt _stmt = _jimple.newInvokeStmt(_expr);
					valueProcessor.residualize(stmt.getRightOpBox());
					setResult(_stmt);
				} else {
					setResult(_jimple.newNopStmt());
				}
			} else {
				valueProcessor.residualize(stmt.getRightOpBox());
			}
		}
	}


	/**
	 * This class residualizes expressions and values.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	private final class ValueResidualizer
	  extends AbstractJimpleValueSwitch {
		/**
		 * @see soot.jimple.RefSwitch#defaultCase(java.lang.Object)
		 */
		public void defaultCase(final Object v) {
			final Value _v = (Value) v;

			for (final Iterator _i = _v.getUseBoxes().iterator(); _i.hasNext();) {
				final ValueBox _vBox = (ValueBox) _i.next();
				residualize(_vBox);
			}
		}

		/**
		 * Residualizes the value.
		 *
		 * @param vBox contains <code>value</code>.
		 *
		 * @pre value != null and vBox != null
		 */
		public void residualize(final ValueBox vBox) {
			final Value _value = vBox.getValue();

			if (!((Host) vBox).hasTag(tagToResidualize)) {
				vBox.setValue(Util.getDefaultValueFor(_value.getType()));
			} else {
				_value.apply(this);
			}
		}
	}

	/**
	 * Sets the name of that tag that identifies the parts of the system that need to be residualized.
	 *
	 * @param nameOfTagToResidualize is the name of the tag.
	 *
	 * @pre nameOfTagToResidualize != null
	 */
	public void setTagToResidualize(final String nameOfTagToResidualize) {
		tagToResidualize = nameOfTagToResidualize;
	}

	/**
	 * Called by the processing controller to process a statement.
	 *
	 * @param stmt is the statement to be processed.
	 * @param ctxt <i>ignored</i>.
	 *
	 * @pre stmt != null
	 */
	public void callback(final Stmt stmt, final Context ctxt) {
		if (currMethod != null) {
			pruneLocals(stmt);

			final boolean _flag = stmtProcessor.residualize(stmt);

			if (!_flag) {
				stmtsToBeNOPed.add(stmt);
			} else {
				processHandlers(stmt);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootMethod)
	 */
	public void callback(final SootMethod method) {
		if (currClass != null) {
			consolidateMethod();
			currMethod = method;
			methodsToKill.remove(method);
			localsToKeep.clear();
			trapsToRetain.clear();
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootClass)
	 */
	public void callback(final SootClass clazz) {
		consolidateClass();

		if (clazz.hasTag(tagToResidualize)) {
			currClass = clazz;
			classesToKill.remove(clazz);
			methodsToKill.addAll(clazz.getMethods());
			fieldsToKill.addAll(clazz.getFields());

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized class " + clazz);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#callback(soot.SootField)
	 */
	public void callback(final SootField field) {
		if (currClass != null) {
			fieldsToKill.remove(field);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Residualized field " + field);
			}
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#consolidate()
	 */
	public void consolidate() {
		consolidateClass();

		for (final Iterator _i = class2members.keySet().iterator(); _i.hasNext();) {
			final SootClass _class = (SootClass) _i.next();
			final Pair _members = (Pair) class2members.get(_class);
			final Collection _methods = (Collection) _members.getFirst();
			final Collection _fields = (Collection) _members.getSecond();

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("BEGIN: Finishing class " + _class);
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing methods: " + _methods);
			}

			for (final Iterator _j = _methods.iterator(); _j.hasNext();) {
				_class.removeMethod((SootMethod) _j.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Erasing fields: " + _fields);
			}

			for (final Iterator _j = _fields.iterator(); _j.hasNext();) {
				_class.removeField((SootField) _j.next());
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("END: Finishing class " + _class);
			}
		}
		class2members.clear();
		currClass = null;
		currMethod = null;
		methodsToKill.clear();
		fieldsToKill.clear();
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#hookup(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void hookup(final ProcessingController ppc) {
		ppc.register(this);
		ppc.registerForAllStmts(this);
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#processingBegins()
	 */
	public void processingBegins() {
		currMethod = null;
		currClass = null;
		classesToKill.clear();
		classesToKill.addAll(theScene.getClasses());
		oldStmt2newStmt.clear();
		stmtsToBeNOPed.clear();
		methodsToKill.clear();
	}

	/**
	 * Residualizes the given system.
	 *
	 * @param scene is the system to be residualized.
	 *
	 * @pre scene != null
	 */
	public void residualizeSystem(final Scene scene) {
		theScene = scene;

		final ProcessingController _pc = new ProcessingController();
		_pc.setProcessingFilter(new TagBasedProcessingFilter(tagToResidualize));
		_pc.setEnvironment(new Environment(scene));
		hookup(_pc);
		_pc.process();
		unhook(_pc);
		_pc.reset();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Deleting classes: " + classesToKill);
		}

		for (final Iterator _i = classesToKill.iterator(); _i.hasNext();) {
			scene.removeClass((SootClass) _i.next());
		}
	}

	/**
	 * @see edu.ksu.cis.indus.processing.IProcessor#unhook(edu.ksu.cis.indus.processing.ProcessingController)
	 */
	public void unhook(final ProcessingController ppc) {
		ppc.unregister(this);
		ppc.unregisterForAllStmts(this);
	}

	/**
	 * Consolidate the current class and method.
	 *
	 * @post currClass = null and methodsToKill.size() = 0 and fieldsToKill.size() = 0 and currMethod = null
	 */
	private void consolidateClass() {
		if (currClass != null) {
			consolidateMethod();
			class2members.put(currClass, new Pair(new ArrayList(methodsToKill), new ArrayList(fieldsToKill)));
			methodsToKill.clear();
			fieldsToKill.clear();
			currClass = null;
		}
	}

	/**
	 * Consolidate the current method.
	 *
	 * @post currMethod = null
	 */
	private void consolidateMethod() {
		if (currMethod == null) {
			return;
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Finishing method " + currMethod + "[concrete: " + currMethod.isConcrete() + "]");
		}

		if (currMethod.isConcrete()) {
			// replace statements marked as nop statements with nop statements.
			final JimpleBody _body = (JimpleBody) currMethod.getActiveBody();
			final Chain _ch = _body.getUnits();
			final Jimple _jimple = Jimple.v();

			for (final Iterator _i = stmtsToBeNOPed.iterator(); _i.hasNext();) {
				final Stmt _stmt = (Stmt) _i.next();
				final Object _pred = _ch.getPredOf(_stmt);
				_ch.remove(_stmt);

				final Stmt _newStmt = _jimple.newNopStmt();

				if (_pred == null) {
					_ch.addFirst(_newStmt);
				} else {
					_ch.insertAfter(_newStmt, _pred);
				}
			}
			stmtsToBeNOPed.clear();

			// replace statements with new statements as recorded earlier.
			for (final Iterator _i = oldStmt2newStmt.entrySet().iterator(); _i.hasNext();) {
				final Entry _entry = (Entry) _i.next();
				final Object _oldStmt = _entry.getKey();
				final Object _newStmt = _entry.getValue();
				_ch.insertAfter(_newStmt, _oldStmt);
				_ch.remove(_oldStmt);
			}

			//inject any cooked up predecessors
			for (final Iterator _i = stmt2predecessors.entrySet().iterator(); _i.hasNext();) {
				final Map.Entry _entry = (Map.Entry) _i.next();
				final Object _stmt = _entry.getKey();
				final List _preds = (List) _entry.getValue();
				_ch.insertBefore(_preds, _stmt);
			}

			stmt2predecessors.clear();
			oldStmt2newStmt.clear();

			// prune locals and traps
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Locals " + _body.getLocals());
				LOGGER.debug("Retaining:" + localsToKeep);
			}
			_body.getLocals().retainAll(localsToKeep);
			_body.getTraps().retainAll(trapsToRetain);

			// transformations built into Soot
			NopEliminator.v().transform(_body);
			UnreachableCodeEliminator.v().transform(_body);
			ConditionalBranchFolder.v().transform(_body);
			UnconditionalBranchFolder.v().transform(_body);
			UnusedLocalEliminator.v().transform(_body);
			_body.validateLocals();
			_body.validateTraps();
			_body.validateUnitBoxes();
			_body.validateUses();

			/*
			 * It is possible that some methods are marked but none of their statements are marked.  This can happen in
			 * executable slice with no specialization.  Hence, the body needs to be fixed for the code to be executable.
			 */
			if (_body.getUnits().isEmpty()) {
				// remove all traps 
				_body.getTraps().clear();
				// remove all locals
				_body.getLocals().clear();

				final Chain _temp = _body.getUnits();
				final Type _retType = currMethod.getReturnType();

				if (_retType instanceof VoidType) {
					_temp.add(_jimple.newReturnVoidStmt());
				} else {
					_temp.add(_jimple.newReturnStmt(Util.getDefaultValueFor(_retType)));
				}
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Finishing method " + currMethod);
		}
		currMethod = null;
	}

	/**
	 * Marks the traps to be included in the slice.
	 *
	 * @param stmt will trigger the traps to include.
	 *
	 * @pre method != null and stmt != null
	 */
	private void processHandlers(final Stmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("BEGIN: Collecting handlers " + stmt + "@" + currMethod);
		}

		final Body _body = currMethod.retrieveActiveBody();

		// calculate the relevant traps
		if (stmt.hasTag(tagToResidualize)) {
			trapsToRetain.addAll(TrapManager.getTrapsAt(stmt, _body));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("END: Collecting handlers " + stmt + "@" + currMethod);
		}
	}

	/**
	 * Prunes the locals in the given stmt.
	 *
	 * @param stmt in which to process the locals.
	 *
	 * @pre stmt != null
	 */
	private void pruneLocals(final Stmt stmt) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Pruning locals in " + stmt);
		}

		if (stmt.hasTag(tagToResidualize)) {
			for (final Iterator _k = stmt.getUseAndDefBoxes().iterator(); _k.hasNext();) {
				final ValueBox _vBox = (ValueBox) _k.next();
				final Value _value = _vBox.getValue();

				if (_vBox.hasTag(tagToResidualize) && _value instanceof Local) {
					localsToKeep.add(_value);
				}
			}
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.10  2004/05/21 22:11:48  venku
   - renamed CollectionsModifier as CollectionUtilities.
   - added new specialized methods along with a method to extract
     filtered maps.
   - ripple effect.
   Revision 1.9  2004/04/24 08:25:46  venku
   - methodsToKill belonging to the enclosing class was altered in
     prepareInit() method.  However, this is not true.  FIXED.
   Revision 1.8  2004/04/20 00:43:40  venku
   - The processing during residualization was driven by a graph.  This
     caused errors when the graph did not cover all of the statements.
     Hence, during residualization we will visit all parts of a method.
   Revision 1.7  2004/04/19 19:10:01  venku
   - we cannot delete locals before processing the statements. Likewise,
     we cannot delete methods in a class as it may be referred to in a
     class we process later.
   - batched the deletion of method locals, methods, and fields.
   Revision 1.6  2004/03/29 01:55:08  venku
   - refactoring.
     - history sensitive work list processing is a common pattern.  This
       has been captured in HistoryAwareXXXXWorkBag classes.
   - We rely on views of CFGs to process the body of the method.  Hence, it is
     required to use a particular view CFG consistently.  This requirement resulted
     in a large change.
   - ripple effect of the above changes.
   Revision 1.5  2004/03/04 14:02:44  venku
   - locals should be processed for methods with body. FIXED.
   Revision 1.4  2004/03/03 10:09:42  venku
   - refactored code in ExecutableSlicePostProcessor and TagBasedSliceResidualizer.
   Revision 1.3  2004/02/28 22:45:27  venku
     empty log message
   Revision 1.2  2004/02/27 09:41:29  venku
   - added logic to massage the slice while residualizing to avoid
     idioms such as throw null.
   Revision 1.1  2004/02/25 23:33:40  venku
   - well package naming convention was inconsistent. FIXED.
   Revision 1.20  2004/02/23 09:10:54  venku
   - depending on the unit graph used the body may be inconsistent in
     terms of control/data flow paths as the data analyses are based
     on the unit graph.  Hence, soot transformers are used to bring the
     body into a consistent state and then the validity tests are performed.
   Revision 1.19  2004/02/23 04:43:39  venku
   - jumps were not fixed properly when old statements were
     replaced with new statements.
   Revision 1.18  2004/02/04 04:33:41  venku
   - locals in empty methods need to be removed as well. FIXED.
   Revision 1.17  2004/01/31 01:48:18  venku
   - for odd reasons, various transformers provided in SOOT fail,
     hence, they are not used anymore.
   Revision 1.16  2004/01/30 23:57:11  venku
   - uses various body transformers to optimize the body.
   - uses entry control DA to pick only the required exit
     points while making the slice executable.
   Revision 1.15  2004/01/25 07:50:20  venku
   - changes to accomodate class hierarchy fixup and handling of
     statements which are marked as true but in which none of the
     expressions are marked as true.
   Revision 1.14  2004/01/24 01:43:40  venku
   - moved getDefaultValueFor() to Util.
   Revision 1.13  2004/01/22 01:07:00  venku
   - coding convention.
   Revision 1.12  2004/01/22 01:06:13  venku
   - coding convention.
   Revision 1.11  2004/01/17 23:25:20  venku
   - value was being cast into a Host.  FIXED.
   Revision 1.10  2004/01/14 12:01:02  venku
   - documentation.
   - error was not flagged when incorrect slice is detected. FIXED.
   Revision 1.9  2004/01/13 10:59:42  venku
   - systemTagName is not required by TagBasedDestructiveSliceResidualizer.
     It was deleted.
   - ripple effect.
   Revision 1.8  2004/01/11 03:38:03  venku
   - entire method bodies may be deleted or not included in the
     first place (due to inheritance).  If so, a suitable return
     statement is injected.
   - We now only process methods which are tagged with
     tagToResidualize rather than systemTagName.
   Revision 1.7  2004/01/09 23:15:37  venku
   - chnaged the method,field, and class kill logic.
   - changed method and class finishing logic.
   - removed unnecessary residualization when some
     properties about statements such as enter monitor
     and invoke expr are known.
   Revision 1.6  2004/01/09 07:03:07  venku
   - added annotations for code to be removed.
   Revision 1.5  2003/12/16 12:43:33  venku
   - fixed many errors during destruction of the system.
   Revision 1.4  2003/12/15 16:30:57  venku
   - safety checks and formatting.
   Revision 1.3  2003/12/14 17:00:51  venku
   - enabled nop elimination
   - fixed return statement in methods.
   Revision 1.2  2003/12/14 16:40:30  venku
   - added residualization logic.
   - incorporate the residualizer in the tool.
   Revision 1.1  2003/12/09 11:02:38  venku
   - synchronization forced commit.
 */
