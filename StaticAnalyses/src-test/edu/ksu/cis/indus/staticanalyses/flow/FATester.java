
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

package edu.ksu.cis.indus.staticanalyses.flow;

import edu.ksu.cis.indus.interfaces.IEnvironment;

import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.OFAnalyzer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.swingui.TestRunner;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.VoidType;


/**
 * DOCUMENT ME!
 * 
 * <p></p>
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class FATester
  extends TestCase {
	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private static final String TAG_NAME = "FATester:TestTag";

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	String classes;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private FA fa;

	/**
	 * <p>
	 * DOCUMENT ME!
	 * </p>
	 */
	private IEnvironment env;

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @param args DOCUMENT ME!
	 */
	public static void main(String[] args) {
		StringBuffer sb = new StringBuffer();

		for (int i = args.length - 1; i >= 0; i--) {
			sb.append(args[i] + " ");
		}

		FATester tester = new FATester();
		tester.classes = sb.toString();

		TestRunner runner = new TestRunner();
		runner.setLoading(false);
		runner.start(new String[0]);
		runner.startTest(tester);
		runner.runSuite();
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 *
	 * @return DOCUMENT ME!
	 */
	public static TestSuite suite() {
		TestSuite suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.flow.FA");

		//$JUnit-BEGIN$
		suite.addTestSuite(FATester.class);
		//$JUnit-END$
		return suite;
	}

	/**
	 * DOCUMENT ME!
	 * 
	 * <p></p>
	 */
	public void testGetClasses() {
		for (final Iterator _i = env.getClasses().iterator(); _i.hasNext();) {
			final SootClass _sc = (SootClass) _i.next();
			boolean flag = false;

			for (final Iterator _j = _sc.getFields().iterator(); _j.hasNext();) {
				final SootField _sf = (SootField) _j.next();
				flag |= _sf.hasTag(TAG_NAME);
			}

			for (final Iterator _j = _sc.getMethods().iterator(); _j.hasNext();) {
				final SootMethod _sm = (SootMethod) _j.next();

				if (fa.queryMethodVariant(_sm) != null) {
					assertTrue(_sm.hasTag(TAG_NAME));
				}

				if (_sm.hasTag(TAG_NAME)) {
					assertTrue(fa.queryMethodVariant(_sm) != null);
					flag = true;
				}
			}

			if (flag) {
				assertTrue(_sc.hasTag(TAG_NAME));
			}
		}
	}

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp()
	  throws Exception {
		AbstractAnalyzer ofa = OFAnalyzer.getFSOSAnalyzer(TAG_NAME);
		Scene scene = Scene.v();

		if (classes == null) {
			classes = System.getProperty("fatester.classes");
		}

		if (classes == null || classes.length() == 0) {
			throw new RuntimeException("fatester.classes property was empty.  Aborting.");
		}

		StringBuffer sb = new StringBuffer(classes);
		String[] j = sb.toString().split(" ");
		Collection rootMethods = new ArrayList();

		for (int i = j.length - 1; i >= 0; i--) {
			SootClass sc = scene.loadClassAndSupport(j[i]);

			if (sc.declaresMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
					  VoidType.v())) {
				SootMethod sm =
					sc.getMethod("main", Collections.singletonList(ArrayType.v(RefType.v("java.lang.String"), 1)),
						VoidType.v());

				if (sm.isPublic() && sm.isConcrete()) {
					rootMethods.add(sm);
				}
			}
		}

		ofa.analyze(scene, rootMethods);
		env = ofa.getEnvironment();
		fa = ofa.fa;
	}
}

/*
   ChangeLog:
   $Log$
 */
