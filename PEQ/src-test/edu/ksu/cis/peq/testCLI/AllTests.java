/*
 *
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
 
package edu.ksu.cis.peq.testCLI;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author ganeshan
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class AllTests {

    public static Test suite() {
        TestSuite suite = new TestSuite("Test for PEQ");
        //$JUnit-BEGIN$
        suite.addTest(new UQueryPassJunitTest() {
            protected void runTest() {
                testExecutePass();
            }
        });
        suite.addTest(new UQueryFailJunitTest() {
            protected void runTest() {
                testExecutePass();
            }
        });
        //$JUnit-END$
        return suite;
    }
}
