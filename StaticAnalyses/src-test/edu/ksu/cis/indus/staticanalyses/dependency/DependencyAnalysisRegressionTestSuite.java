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

package edu.ksu.cis.indus.staticanalyses.dependency;

import edu.ksu.cis.indus.ErringTestCase;
import edu.ksu.cis.indus.IXMLBasedTest;
import edu.ksu.cis.indus.TestHelper;

import edu.ksu.cis.indus.common.soot.CompleteStmtGraphFactory;
import edu.ksu.cis.indus.common.soot.IStmtGraphFactory;

import edu.ksu.cis.indus.staticanalyses.callgraphs.OFABasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.callgraphs.XMLBasedCallGraphTest;
import edu.ksu.cis.indus.staticanalyses.flow.FATest;
import edu.ksu.cis.indus.staticanalyses.flow.instances.ofa.XMLBasedOFATest;

import edu.ksu.cis.indus.xmlizer.UniqueJimpleIDGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the test suite used to run dependency analyses related tests using JUnit's text interface to the runner.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class DependencyAnalysisRegressionTestSuite
		extends TestCase {

	/**
	 * This is the property via which the ofa test accepts input. Refer to DepedencyAnalysisTest.properties for format.
	 */
	public static final String DEPENDENCY_ANALYSIS_TEST_PROPERTIES_FILE = "indus.staticanalyses.dependency.DependencyAnalysisTest.properties.file";

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DependencyAnalysisRegressionTestSuite.class);

	/**
	 * The property that maps a dependency class to the class that should be used to test its output.
	 */
	private static final Properties TEST_CLASSES_PROPERTIES = new Properties();

	static {
		String _propFileName = System.getProperty("indus.dependencyxmlizer.properties.file");

		if (_propFileName == null) {
			_propFileName = "edu/ksu/cis/indus/staticanalyses/dependency/DependencyAnalysisTestClasses.properties";
		}

		final InputStream _stream = ClassLoader.getSystemResourceAsStream(_propFileName);

		try {
			TEST_CLASSES_PROPERTIES.load(_stream);
		} catch (IOException _e) {
			System.err.println("Well, error loading property file.  Bailing.");
			throw new RuntimeException(_e);
		}
	}

	// /CLOVER:OFF

	/**
	 * This is the entry point via command-line.
	 * 
	 * @param args are ignored.
	 */
	public static void main(final String[] args) {
		final String[] _suiteName = {DependencyAnalysisRegressionTestSuite.class.getName()};
		TestRunner.main(_suiteName);
	}

	// /CLOVER:ON

	/**
	 * Provides the suite of tests in junit-style. This sets up the tests based on the file specified via
	 * <code>DEPENDENCY_ANALYSIS_TEST_PROPERTIES_FILE</code> system property. Refer to
	 * <code>edu.ksu.cis.indus.staticanalyses.dependency.DependencyAnalysisTest.properties</code> for the format of the
	 * file.
	 * 
	 * @return the suite of tests.
	 * @throws RuntimeException when <code>DEPENDENCY_ANALYSIS_TEST_PROPERTIES_FILE</code> property is unspecified.
	 */
	public static Test suite() {
		final TestSuite _suite = new TestSuite("Test for edu.ksu.cis.indus.staticanalyses.dependency");
		final String _propFileName = System.getProperty(DEPENDENCY_ANALYSIS_TEST_PROPERTIES_FILE);

		if (_propFileName == null) {
			throw new RuntimeException("Please provide a property file like DependencyAnalysisTest.properties via " + "-D"
					+ DEPENDENCY_ANALYSIS_TEST_PROPERTIES_FILE);
		}
		setupTests(_propFileName, _suite);
		return _suite;
	}

	/**
	 * Retrieves the test instance to test the given analysis instance.
	 * 
	 * @param analysis is the instance whose output should be tested.
	 * @return the test instance.
	 * @pre analysis != null
	 */
	private static Test getDATestFor(final IDependencyAnalysis analysis) {
		Test _result = null;

		final String _daClassName = analysis.getClass().getName();
		final String _daTestClassName = TEST_CLASSES_PROPERTIES.getProperty(_daClassName);

		if (_daTestClassName == null) {
			LOGGER.error("How can the test class for a dependency analysis be undefined? " + _daClassName);
		} else {
			try {
				final Class _daTestClass = Class.forName(_daTestClassName);
				final IDependencyAnalysisTest _daTest = (AbstractDependencyAnalysisTest) _daTestClass.newInstance();
				((AbstractDependencyAnalysisTest) _daTest).setDA(analysis);
				_result = _daTest;
			} catch (final InstantiationException _e1) {
				LOGGER.error("Error while creating an object of type. " + _daTestClassName, _e1);
			} catch (final IllegalAccessException _e1) {
				LOGGER.error("Error while accessing the constructor of type. " + _daTestClassName, _e1);
			} catch (final ClassNotFoundException _e1) {
				LOGGER.error("Error while finding type. " + _daTestClassName, _e1);
			} catch (final SecurityException _e1) {
				LOGGER.error("Security violation while accessing the constructor of type. " + _daTestClassName, _e1);
			}
		}
		return _result;
	}

	/**
	 * Sets up the test fixture.
	 * 
	 * @param propFileName is the name of the file with the data to setup the test fixture.
	 * @param suite will contain new tests based on the fixture data (upon return).
	 * @throws IllegalArgumentException when the fixture data is invalid.
	 * @pre propFileName != null and suite != null
	 */
	private static void setupTests(final String propFileName, final TestSuite suite) {
		final Properties _props = new Properties();

		try {
			_props.load(new FileInputStream(new File(propFileName)));

			final String[] _configs = _props.getProperty("configs").split(" ");
			final Collection _das = new ArrayList();
			final IStmtGraphFactory _stmtGraphFactory = new CompleteStmtGraphFactory();

			for (int _i = 0; _i < _configs.length; _i++) {
				final String _config = _configs[_i];
				final String _classNames = _props.getProperty(_config + ".classNames");
				final String _xmlTestDir = _props.getProperty(_config + IXMLBasedTest.XML_TEST_DIR_PROP_SUFFIX);
				final String _xmlControlDir = _props.getProperty(_config + IXMLBasedTest.XML_CONTROL_DIR_PROP_SUFFIX);
				final String _classpath = _props.getProperty(_config + ".classpath");
				final String _jimpleXMLDumpDir = _props.getProperty(_config + ".jimpleXMLDumpDir");
				final String _str = TestHelper.checkXMLBasedTestExecutability(_config, _xmlTestDir, _xmlControlDir);

				try {
					final TestSuite _temp = new TestSuite(_config);

					if (_str.length() > 0) {
						_temp.addTest(new ErringTestCase(_str));
						TestHelper.appendSuiteNameToTestsIn(_temp, true);
						suite.addTest(_temp);
					} else {
						_das.clear();
						_das.add(DivergenceDA.getDivergenceDA(IDependencyAnalysis.Direction.FORWARD_DIRECTION));
						_das.add(DivergenceDA.getDivergenceDA(IDependencyAnalysis.Direction.BACKWARD_DIRECTION));
						_das
								.add(InterProceduralDivergenceDA
										.getDivergenceDA(IDependencyAnalysis.Direction.FORWARD_DIRECTION));
						_das.add(InterProceduralDivergenceDA
								.getDivergenceDA(IDependencyAnalysis.Direction.BACKWARD_DIRECTION));
						_das.add(new NonTerminationSensitiveEntryControlDA());
						_das.add(new NonTerminationInsensitiveEntryControlDA());
						_das.add(new ExitControlDA());
						_das.add(new IdentifierBasedDataDA());
						_das.add(new InterferenceDAv1());
						_das.add(new InterferenceDAv2());
						_das.add(new InterferenceDAv3());
						_das.add(ReadyDAv1.getForwardReadyDA());
						_das.add(ReadyDAv1.getBackwardReadyDA());
						_das.add(ReadyDAv2.getForwardReadyDA());
						_das.add(ReadyDAv2.getBackwardReadyDA());
						_das.add(ReadyDAv3.getForwardReadyDA());
						_das.add(ReadyDAv3.getBackwardReadyDA());
						_das.add(new ReferenceBasedDataDA());
						_das.add(new SynchronizationDA());

						String _ignoreDARegex = _props.getProperty("ignore.das");

						if (_ignoreDARegex == null) {
							_ignoreDARegex = "^$";
						}

						for (final Iterator _j = _das.iterator(); _j.hasNext();) {
							final Object _da = _j.next();

							if (!Pattern.matches(_ignoreDARegex, _da.getClass().getName())) {
								final Test _test = getDATestFor((IDependencyAnalysis) _da);

								if (_test != null) {
									_temp.addTest(_test);
								}
							}
						}

						final DependencyXMLizer _xmlizer = new DependencyXMLizer();

						for (final Iterator _j = _das.iterator(); _j.hasNext();) {
							final Object _da = _j.next();

							if (!Pattern.matches(_ignoreDARegex, _da.getClass().getName())) {
								final XMLBasedDependencyAnalysisTest _xmlTest = new XMLBasedDependencyAnalysisTest(
										(IDependencyAnalysis) _da, _xmlizer);
								_temp.addTest(_xmlTest);
							}
						}
						_temp.addTestSuite(XMLBasedCallGraphTest.class);
						_temp.addTestSuite(OFABasedCallGraphTest.class);
						_temp.addTestSuite(XMLBasedOFATest.class);
						_temp.addTestSuite(FATest.class);
						TestHelper.appendSuiteNameToTestsIn(_temp, true);

						final DependencyAnalysisTestSetup _test = new DependencyAnalysisTestSetup(_temp, _classNames,
								_classpath);
						_test.setIdGenerator(new UniqueJimpleIDGenerator());
						_test.setJimpleXMLDumpLocation(_jimpleXMLDumpDir);
						_test.setStmtGraphFactory(_stmtGraphFactory);
						_test.setXMLTestDir(_xmlTestDir);
						_test.setXMLControlDir(_xmlControlDir);
						suite.addTest(_test);
					}
				} catch (final IllegalArgumentException _e) {
					_e.printStackTrace();
				}
			}
		} catch (final IOException _e) {
			throw new IllegalArgumentException("Specified property file does not exist.");
		}
	}
}

// End of File
