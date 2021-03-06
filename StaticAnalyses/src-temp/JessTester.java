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
import edu.ksu.cis.indus.common.datastructures.HistoryAwareFIFOWorkBag;
import edu.ksu.cis.indus.common.datastructures.IWorkBag;
import edu.ksu.cis.indus.common.soot.Util;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAXMLizerCLI;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

import jess.Context;
import jess.Fact;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Value;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import soot.ArrayType;
import soot.Local;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.VoidType;
import soot.jimple.AbstractJimpleValueSwitch;
import soot.jimple.AbstractStmtSwitch;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CaughtExceptionRef;
import soot.jimple.IdentityStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.InterfaceInvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.ParameterRef;
import soot.jimple.ReturnStmt;
import soot.jimple.SpecialInvokeExpr;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.VirtualInvokeExpr;

public class JessTester {

	private class JimpleStmtSwitch
			extends AbstractStmtSwitch {

		/**
		 * {@inheritDoc}
		 */
		public void caseAssignStmt(final AssignStmt stmt) {
			final soot.Value _rightOp = stmt.getRightOp();
			if (_rightOp.getType() instanceof RefType) {

				try {
					final Fact _fact;
					if (_rightOp instanceof NewExpr || _rightOp instanceof NewArrayExpr
							|| _rightOp instanceof NewMultiArrayExpr || _rightOp instanceof StringConstant) {
						_fact = new Fact(rete.findDeftemplate("pointsTo"));
						_fact.setSlotValue("object", new Value(_rightOp));
						stmt.getLeftOp().apply(vSwitch);
						_fact.setSlotValue("reference", value);
					} else {
						_fact = new Fact(rete.findDeftemplate("assignTo"));
						_rightOp.apply(vSwitch);
						_fact.setSlotValue("rhs", value);
						stmt.getLeftOp().apply(vSwitch);
						_fact.setSlotValue("lhs", value);
						if (stmt.containsInvokeExpr()) {
							_fact.setSlotValue("invocationSite", new Value(stmt.getInvokeExpr()));
						}
					}
					_fact.setSlotValue("context", new Value(sm));
					rete.assertFact(_fact);
				} catch (final JessException _e) {
					_e.printStackTrace();
					throw new RuntimeException(_e);
				}
			} else if (stmt.containsInvokeExpr()) {
				stmt.getRightOp().apply(vSwitch);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseIdentityStmt(final IdentityStmt stmt) {
			if (stmt.getLeftOp().getType() instanceof RefType && !(stmt.getRightOp() instanceof CaughtExceptionRef)) {
				try {
					final Fact fact = new Fact(rete.findDeftemplate("assignTo"));
					stmt.getLeftOp().apply(vSwitch);
					fact.setSlotValue("lhs", value);
					stmt.getRightOp().apply(vSwitch);
					fact.setSlotValue("rhs", value);
					fact.setSlotValue("context", new Value(sm));
					rete.assertFact(fact);
				} catch (final JessException _e) {
					_e.printStackTrace();
					throw new RuntimeException(_e);
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseInvokeStmt(final InvokeStmt stmt) {
			stmt.getInvokeExpr().apply(vSwitch);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseReturnStmt(final ReturnStmt stmt) {
			try {
				final Fact fact = new Fact(rete.findDeftemplate("assignTo"));
				fact.setSlotValue("lhs", new Value(sm.getSignature() + "+returnCallee"));
				stmt.getOp().apply(vSwitch);
				fact.setSlotValue("rhs", value);
				fact.setSlotValue("index", new Value(-2, RU.INTEGER));
				fact.setSlotValue("context", new Value(sm));
				rete.assertFact(fact);
			} catch (final JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseThrowStmt(final ThrowStmt stmt) {
			try {
				final Fact fact = new Fact(rete.findDeftemplate("assignTo"));
				fact.setSlotValue("lhs", new Value(sm.getSignature() + "+throwCallee"));
				stmt.getOp().apply(vSwitch);
				fact.setSlotValue("rhs", value);
				fact.setSlotValue("index", new Value(-3, RU.INTEGER));
				fact.setSlotValue("context", new Value(sm));
				rete.assertFact(fact);
			} catch (final JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}
	}

	private class JimpleValueSwitch
			extends AbstractJimpleValueSwitch {

		/**
		 * {@inheritDoc}
		 */
		public void caseArrayRef(final ArrayRef v) {
			value = new Value(v.getType());
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseCastExpr(final CastExpr v) {
			v.getOp().apply(this);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseInstanceFieldRef(final InstanceFieldRef v) {
			if (v.getField().getType() instanceof RefType) {
				value = new Value(v.getField());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseInterfaceInvokeExpr(final InterfaceInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseLocal(final Local l) {
			addTypeForProcessing(l.getType());
			value = new Value(l);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseNewArrayExpr(final NewArrayExpr v) {
			addTypeForProcessing(((ArrayType) v.getType()).getElementType());
			value = new Value(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseNewExpr(final NewExpr v) {
			addTypeForProcessing(v.getType());
			value = new Value(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseNewMultiArrayExpr(final NewMultiArrayExpr v) {
			addTypeForProcessing(((ArrayType) v.getType()).getElementType());
			value = new Value(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseNullConstant(final NullConstant v) {
			value = new Value(NULL);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseParameterRef(final ParameterRef v) {
			if (v.getType() instanceof RefType) {
				value = new Value(sm.getSignature() + "+" + v.getIndex());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseSpecialInvokeExpr(final SpecialInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseStaticFieldRef(final StaticFieldRef v) {
			value = new Value(v.getField());
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseStaticInvokeExpr(final StaticInvokeExpr v) {
			try {
				final SootMethod _callee = v.getMethod();

				addClassForProcessing(_callee.getDeclaringClass());
				addMethodForProcessing(_callee);

				for (int _i = 0; _i < _callee.getParameterCount(); _i++) {
					if (v.getArg(_i).getType() instanceof RefType) {
						addTypeForProcessing(v.getArg(_i).getType());

						final Fact _fact = new Fact(rete.findDeftemplate("assignTo"));
						_fact.setSlotValue("lhs", new Value(_callee.getSignature() + "+" + _i));
						v.getArg(_i).apply(this);
						_fact.setSlotValue("rhs", value);
						_fact.setSlotValue("invocationSite", new Value(v));
						_fact.setSlotValue("context", new Value(sm));
						_fact.setSlotValue("index", new Value(_i, RU.INTEGER));
						rete.assertFact(_fact);
					}
				}

				if (_callee.getReturnType() instanceof RefType) {
					addTypeForProcessing(_callee.getReturnType());
					value = new Value(_callee.getSignature() + "+returnCaller");
					final Fact _fact = new Fact(rete.findDeftemplate("assignTo"));
					_fact.setSlotValue("lhs", value);
					_fact.setSlotValue("rhs", new Value(_callee.getSignature() + "+returnCallee"));
					_fact.setSlotValue("invocationSite", new Value(v));
					_fact.setSlotValue("context", new Value(sm));
					_fact.setSlotValue("index", new Value(-2, RU.INTEGER));
					rete.assertFact(_fact);
				}
			} catch (final JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}

		}

		/**
		 * {@inheritDoc}
		 */
		public void caseStringConstant(final StringConstant v) {
			addTypeForProcessing(v.getType());
			value = new Value(v);
		}

		/**
		 * {@inheritDoc}
		 */
		public void caseThisRef(final ThisRef v) {
			value = new Value(sm.getSignature() + "+thisRefCallee");
		}

		/**
		 * @see soot.jimple.ExprSwitch#caseVirtualInvokeExpr(soot.jimple.VirtualInvokeExpr)
		 */
		public void caseVirtualInvokeExpr(final VirtualInvokeExpr v) {
			caseInstanceInvokeExpr(v);
		}

		/**
		 * DOCUMENT ME!
		 * 
		 * @param v
		 */
		private void caseInstanceInvokeExpr(final InstanceInvokeExpr v) {
			try {
				final SootMethod _invokedMethod = v.getMethod();

				addClassForProcessing(_invokedMethod.getDeclaringClass());

				for (int _i = 0; _i < _invokedMethod.getParameterCount(); _i++) {
					if (v.getArg(_i).getType() instanceof RefType) {
						addTypeForProcessing(v.getArg(_i).getType());

						final Fact _fact = new Fact(rete.findDeftemplate("assignTo"));
						_fact.setSlotValue("lhs", new Value(_invokedMethod.getSignature() + "+" + _i));
						v.getArg(_i).apply(this);
						_fact.setSlotValue("rhs", value);
						_fact.setSlotValue("invocationSite", new Value(v));
						_fact.setSlotValue("index", new Value(_i, RU.INTEGER));
						_fact.setSlotValue("context", new Value(sm));
						rete.assertFact(_fact);
					}
				}

				final Fact _fact = new Fact(rete.findDeftemplate("assignTo"));
				_fact.setSlotValue("lhs", new Value(_invokedMethod.getSignature() + "+thisRefCaller"));
				v.getBase().apply(this);
				_fact.setSlotValue("rhs", value);
				_fact.setSlotValue("invocationSite", new Value(v));
				_fact.setSlotValue("context", new Value(sm));
				_fact.setSlotValue("index", new Value(-1, RU.INTEGER));
				rete.assertFact(_fact);

				if (_invokedMethod.getReturnType() instanceof RefType) {
					addTypeForProcessing(_invokedMethod.getReturnType());
					value = new Value(_invokedMethod.getSignature() + "+returnCaller");
				}
			} catch (final JessException _e) {
				_e.printStackTrace();
				throw new RuntimeException(_e);
			}
		}
	}

	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(JessTester.class);

	private static final NullConstant NULL = NullConstant.v();

	Rete rete = new Rete();

	SootClass sc;

	Scene scene;

	SootMethod sm;

	Value value;

	private int methodCount = 0;

	final IWorkBag<SootMethod> methodsWorkBag = new HistoryAwareFIFOWorkBag<SootMethod>(new HashSet<SootMethod>());

	final IWorkBag<SootClass> typesWorkBag = new HistoryAwareFIFOWorkBag<SootClass>(new HashSet<SootClass>());

	private final JimpleStmtSwitch sSwitch = new JimpleStmtSwitch();

	final JimpleValueSwitch vSwitch = new JimpleValueSwitch();

	private int classCount = 0;

	/**
	 * DOCUMENT ME!
	 * 
	 * @param args
	 */
	public static void main(final String[] args) {
		final Options _options = new Options();
		Option _option = new Option("o", "output", true, "Directory into which xml files will be written into.");
		_option.setArgs(1);
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("p", "soot-classpath", true, "Prepend this to soot class path.");
		_option.setArgs(1);
		_option.setArgName("classpath");
		_option.setOptionalArg(false);
		_options.addOption(_option);
		_option = new Option("l", "pre-load-classes", false, "Preload the system before starting OFA.");
		_options.addOption(_option);

		final PosixParser _parser = new PosixParser();

		try {
			final CommandLine _cl = _parser.parse(_options, args);

			if (_cl.hasOption("h")) {
				final String _cmdLineSyn = "java " + OFAXMLizerCLI.class.getName() + " <options> <classnames>";
				(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", _options, "");
				System.exit(1);
			}

			String _outputDir = _cl.getOptionValue('o');

			if (_outputDir == null) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Defaulting to current directory for output.");
				}
				_outputDir = ".";
			}

			final Scene _scene = Scene.v();
			soot.options.Options.v().parse(Util.getSootOptions());

			if (_cl.hasOption('p')) {
				_scene.setSootClassPath(_cl.getOptionValue('p'));
			}

			for (final String _cls : _cl.getArgs()) {
				_scene.loadClassAndSupport(_cls);
			}

			if (_cl.hasOption('l')) {
				for (final Iterator<SootClass> _i = _scene.getClasses().iterator(); _i.hasNext();) {
					final SootClass _sc = _i.next();
					for (final Iterator<SootMethod> _j = _sc.getMethods().iterator(); _j.hasNext();) {
						final SootMethod _sm = _j.next();
						if (_sm.isConcrete()) {
							_sm.retrieveActiveBody();
						}
					}
				}
			}

			final JessTester _jt = new JessTester();
			_jt.scene = _scene;
			_jt.processScene(_cl.getArgList());
		} catch (final ParseException _e) {
			LOGGER.error("Error while parsing command line.", _e);
			final String _cmdLineSyn = "java " + OFAXMLizerCLI.class.getName() + " <options> <classnames>";
			(new HelpFormatter()).printHelp(_cmdLineSyn, "Options are: ", _options, "");
		} catch (final Throwable _e) {
			LOGGER.error("Beyond our control. May day! May day!", _e);
			throw new RuntimeException(_e);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param type DOCUMENT ME!
	 */
	void addTypeForProcessing(final Type type) {
		if (type instanceof RefType) {
			typesWorkBag.addWorkNoDuplicates(((RefType) type).getSootClass());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param clazz DOCUMENT ME!
	 */
	void addClassForProcessing(final SootClass clazz) {
		typesWorkBag.addWorkNoDuplicates(clazz);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param method DOCUMENT ME!
	 */
	void addMethodForProcessing(final SootMethod method) {
		methodsWorkBag.addWorkNoDuplicates(method);
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param clazz DOCUMENT ME!
	 */
	private void processClassInitializer(final SootClass clazz) {
		if (clazz.declaresMethodByName("<clinit>")) {
			processMethod(clazz.getMethodByName("<clinit>"));
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param method DOCUMENT ME!
	 */
	private void processMethod(final SootMethod method) {
		sm = method;
		if (method.isConcrete()) {
			System.out.println("MProcessing " + ++methodCount + ") " + method.getSignature());
			for (final Object _except : method.getExceptions()) {
				addClassForProcessing((SootClass) _except);
			}

			if (Util.isStartMethod(method)) {
				Util.fixupThreadStartBody(scene);
			}

			final Collection<Stmt> _stmts = sm.retrieveActiveBody().getUnits();
			for (final Iterator<Stmt> _k = _stmts.iterator(); _k.hasNext();) {
				final Stmt _stmt = _k.next();
				_stmt.apply(sSwitch);
			}
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param clazz DOCUMENT ME!
	 */
	private void processClass(final SootClass clazz) {
		classCount++;
		System.out.println("CProcessing " + classCount + ") " + clazz);
		processClassInitializer(clazz);
		for (final Object _super : clazz.getInterfaces()) {
			addClassForProcessing((SootClass) _super);
		}
		if (clazz.hasSuperclass()) {
			addClassForProcessing(clazz.getSuperclass());
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param v DOCUMENT ME!
	 * @param e DOCUMENT ME!
	 */
	public void resolveInvocation(final Object v, final InstanceInvokeExpr e, final SootMethod ctxt) {
		final SootClass _sc;
		if (v instanceof NewExpr) {
			_sc = ((NewExpr) v).getBaseType().getSootClass();
		} else if (v instanceof NewArrayExpr || v instanceof NewMultiArrayExpr) {
			_sc = scene.getSootClass("java.lang.Object");
		} else if (v instanceof StringConstant) {
			_sc = scene.getSootClass("java.lang.String");
		} else {
			throw new RuntimeException("Inappropriate type at receiver site - " + e + " -- " + v + " -- " + v.getClass());
		}
		try {
			final SootMethod _invokedMethod = e.getMethod();
			final SootMethod _resolvedMethod = Util.findDeclaringMethod(_sc, _invokedMethod);

			final Fact _fact = new Fact(rete.findDeftemplate("assignTo"));
			_fact.setSlotValue("lhs", new Value(_resolvedMethod.getSignature() + "+thisRefCallee"));
			_fact.setSlotValue("rhs", new Value(_invokedMethod.getSignature() + "+thisRefCaller"));
			_fact.setSlotValue("invocationSite", new Value(e));
			_fact.setSlotValue("context", new Value(ctxt));
			_fact.setSlotValue("index", new Value(-1, RU.INTEGER));
			rete.assertFact(_fact);

			addClassForProcessing(_resolvedMethod.getDeclaringClass());
			addMethodForProcessing(_resolvedMethod);

			for (int _i = 0; _i < _resolvedMethod.getParameterCount(); _i++) {
				if (_resolvedMethod.getParameterType(_i) instanceof RefType) {
					final Fact _pFact = new Fact(rete.findDeftemplate("assignTo"));
					_pFact.setSlotValue("lhs", new Value(_resolvedMethod.getSignature() + "+" + _i));
					_pFact.setSlotValue("rhs", new Value(_invokedMethod.getSignature() + "+" + _i));
					_pFact.setSlotValue("invocationSite", new Value(e));
					_pFact.setSlotValue("context", new Value(ctxt));
					_pFact.setSlotValue("index", new Value(_i, RU.INTEGER));
					rete.assertFact(_pFact);
				}
			}

			if (_resolvedMethod.getReturnType() instanceof RefType) {
				addTypeForProcessing(_resolvedMethod.getReturnType());
				final Fact _rFact = new Fact(rete.findDeftemplate("assignTo"));
				_rFact.setSlotValue("lhs", new Value(_invokedMethod.getSignature() + "+returnCaller"));
				_rFact.setSlotValue("rhs", new Value(_resolvedMethod.getSignature() + "+returnCallee"));
				_rFact.setSlotValue("invocationSite", new Value(e));
				_rFact.setSlotValue("context", new Value(ctxt));
				_rFact.setSlotValue("index", new Value(-2, RU.INTEGER));
				rete.assertFact(_rFact);
			}

		} catch (final IllegalStateException _e) {
			System.out.println(":-( Failed!! " + e + " -- " + v);
			_e.printStackTrace();
		} catch (final JessException _e) {
			System.out.println(":-( Failed!! " + e + " -- " + v);
			_e.printStackTrace();
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param v DOCUMENT ME!
	 * @param obj DOCUMENT ME!
	 */
	public boolean isCompatible(final Object v, final Object obj) {
		if (v instanceof soot.Value) {
			final SootClass _sootClass = ((RefType) ((soot.Value) v).getType()).getSootClass();
			if (obj instanceof StringConstant) {
				return _sootClass.getName().equals("java.lang.String");
			} else if (obj instanceof soot.Value) {
				final SootClass _sootClass2 = ((RefType) ((soot.Value) obj).getType()).getSootClass();
				return _sootClass2.equals(_sootClass) || Util.isDescendentOf(_sootClass2, _sootClass);
			}
		}
		return true;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param classes DOCUMENT ME!
	 */
	private void processScene(final Collection<String> classes) {
		try {
			rete.store("Engine", this);
			// rete.executeCommand("(watch rules)");
			// rete.executeCommand("(watch activations)");
			rete.executeCommand("(import soot.jimple.*)");
			rete.executeCommand("(deftemplate assignTo (slot lhs) (slot rhs) (slot invocationSite (default nil)) "
					+ "(slot index (default -4)) (slot context (default nil)))");
			rete.executeCommand("(deftemplate pointsTo (slot reference) (slot object) (slot context))");
			rete.executeCommand("(defrule expansionRule "
					+ "(and (assignTo (lhs ?a) (rhs ?) (invocationSite ?c&~nil) (index -1) (context ?d)) "
					+ "(pointsTo (reference ?a) (object ?o) (context ?d)) "
					+ ") => (call (fetch Engine) resolveInvocation ?o ?c ?d))");
			rete.executeCommand("(defrule pointsToRule "
					+ "(and (assignTo (lhs ?c) (rhs ?a&:(neq ?a ?c)) (invocationSite ?) (index ?) (context ?ctxt))"
					+ "(pointsTo (reference ?a) (object ?o) (context ?)))"
					+ "=> (if (and (call (fetch Engine) isCompatible ?c ?o) (call (fetch Engine) isCompatible ?a ?c)) "
					+ "then (assert (pointsTo (reference ?c) (object ?o) (context ?ctxt)))))");
			rete.executeCommand("(defquery findParamFactsFor \"Finds facts for Parameters at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(assignTo (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(> ?i -1)) (context ?)))");
			rete.executeCommand("(defquery findReturnFactsFor \"Finds facts for returned values at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(assignTo (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(= ?i -2)) (context ?)))");
			rete.executeCommand("(defquery findThrowFactsFor \"Finds facts for Thrown exceptions at given invocation site\""
					+ "(declare (variables ?callSite)) "
					+ "(assignTo (lhs ?a) (rhs ?b) (invocationSite ?callSite) (index ?i&:(= ?i -3)) (context ?)))");
			// rete.executeCommand("(set-strategy breadth)");

			final ArrayType _stringArrayType = ArrayType.v(scene.getSootClass("java.lang.String").getType(), 1);
			final VoidType _voidType = VoidType.v();
			for (final String _className : classes) {
				sc = scene.getSootClass(_className);
				addClassForProcessing(sc);
				for (final Iterator<SootMethod> _j = sc.getMethods().iterator(); _j.hasNext();) {
					final SootMethod _m = _j.next();
					if (_m.getName().equals("main") && _m.getReturnType().equals(_voidType) && _m.getParameterCount() == 1
							&& _m.getParameterType(0).equals(_stringArrayType)) {
						addMethodForProcessing(_m);
					}
				}
			}

			while (typesWorkBag.hasWork() || methodsWorkBag.hasWork()) {
				while (typesWorkBag.hasWork()) {
					processClass(typesWorkBag.getWork());
				}
				while (methodsWorkBag.hasWork()) {
					processMethod(methodsWorkBag.getWork());
				}
				rete.run();
			}

			final StringWriter _sw = new StringWriter();
			extractFactsAsString(_sw);
			System.out.println(_sw.toString());
		} catch (final JessException _e) {
			_e.printStackTrace();
			throw new RuntimeException(_e);
		}
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * @param writer DOCUMENT ME!
	 * @throws JessException DOCUMENT ME!
	 */
	private void extractFactsAsString(final StringWriter writer) throws JessException {
		final Context _context = rete.getGlobalContext();
		for (final Iterator<Fact> _i = rete.listFacts(); _i.hasNext();) {
			final Fact _fact = _i.next();
			if (_fact.getName().equals("MAIN::assignTo")) {
				writer.append("(assignTo\n\t(lhs ");
				writer.append(_fact.getSlotValue("lhs").javaObjectValue(_context).toString());
				writer.append(")\n\t(rhs ");
				writer.append(_fact.getSlotValue("rhs").javaObjectValue(_context).toString());
				writer.append(")\n\t(invocationSite ");
				final Object _javaObjectValue1 = _fact.getSlotValue("invocationSite").javaObjectValue(_context);
				if (_javaObjectValue1 == null) {
					writer.append("null");
				} else {
					writer.append(_javaObjectValue1.toString());
				}

				writer.append(")\n\t(context ");
				final Object _javaObjectValue2 = _fact.getSlotValue("context").javaObjectValue(_context);
				if (_javaObjectValue2 == null) {
					writer.append("null");
				} else {
					writer.append(_javaObjectValue2.toString());
				}
				writer.append(")\n\t(index ");
				writer.append(String.valueOf(_fact.getSlotValue("index").intValue(_context)));
				writer.append("))\n");
			} else if (_fact.getName().equals("MAIN::pointsTo")) {
				writer.append("(pointsTo\n\t(reference ");
				writer.append(_fact.getSlotValue("reference").javaObjectValue(_context).toString());
				writer.append(")\n\t(object ");
				writer.append(_fact.getSlotValue("object").javaObjectValue(_context).toString());
				writer.append(")\n\t(context ");
				writer.append(_fact.getSlotValue("context").javaObjectValue(_context).toString());
				writer.append("))\n");
			}
		}
	}
}

// End of File
