
/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (C) 2003, 2004, 2005
 * Venkatesh Prasad Ranganath (rvprasad@cis.ksu.edu)
 * All rights reserved.
 *
 * This work was done as a project in the SAnToS Laboratory,
 * Department of Computing and Information Sciences, Kansas State
 * University, USA (http://indus.projects.cis.ksu.edu/).
 * It is understood that any modification not identified as such is
 * not covered by the preceding statement.
 *
 * This work is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this toolkit; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 *
 * Java is a trademark of Sun Microsystems, Inc.
 *
 * To submit a bug report, send a comment, or get the latest news on
 * this project and other SAnToS projects, please visit the web-site
 *                http://indus.projects.cis.ksu.edu/
 */

package edu.ksu.cis.indus.staticanalyses.support;

import soot.ArrayType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.io.PrintStream;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * This class provides the basic infrastructure to drive and time various static analyses.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class Driver {
	/**
	 * This is the type of <code>String[]</code> in Soot type system.
	 */
	public static final ArrayType STR_ARRAY_TYPE = ArrayType.v(RefType.v("java.lang.String"), 1);

	/**
	 * This is the set of methods which serve as the entry point into the system being analyzed.
	 *
	 * @invariant rootMethods.oclIsTypeOf(SootMethod)
	 */
	protected Collection rootMethods = new HashSet();

	/**
	 * This is used to maintain the execution time of each analysis/transformation.  This timing information is printed via
	 * <code>printTimingStats</code>.
	 *
	 * @invariant times.oclIsTypeOf(Map(String, Long))
	 */
	private final Map times = new LinkedHashMap();

	/**
	 * Adds an entry into the time log of this driver.  The subclasses should use this method to add time logs corresponding
	 * to each analysis they drive.
	 *
	 * @param name of the analysis for which the timing log is being created.
	 * @param milliseconds taken by the analysis.
	 *
	 * @pre name != null
	 */
	protected void addTimeLog(String name, long milliseconds) {
		times.put(getClass().getName() + ":" + name, new Long(milliseconds));
	}

	/**
	 * Loads up the given classes and also collects the possible entry points into the system being analyzed.  All
	 * <code>public static void main()</code> methods defined in <code>public</code> classes that are named via
	 * <code>args</code>are considered as entry points.
	 *
	 * @param args is the names of the classes to be loaded for analysis.
	 *
	 * @return a soot scene that provides the classes to be analyzed.
	 *
	 * @pre args != null
	 */
	protected final Scene loadupClassesAndCollectMains(String[] args) {
		Scene result = Scene.v();
		boolean flag = false;
		Collection classNames = Arrays.asList(args);

		for (int i = 0; i < args.length; i++) {
			result.loadClassAndSupport(args[i]);
		}

		Collection mc = new HashSet();
		mc.addAll(result.getClasses());

		for (Iterator i = mc.iterator(); i.hasNext();) {
			SootClass sc = (SootClass) i.next();

			if (Util.implementsInterface(sc, "java.lang.Runnable")) {
				flag = true;
			}

			if (considerClassForEntryPoint(sc, classNames)) {
				Collection methods = sc.getMethods();

				for (Iterator j = methods.iterator(); j.hasNext();) {
					SootMethod sm = (SootMethod) j.next();
					trapRootMethods(sm);
				}
			}
		}

		if (flag) {
			SootClass sc = result.getSootClass("java.lang.Thread");
			SootMethod sm = sc.getMethodByName("start");
			Util.setThreadStartBody(sm);
		}
		return result;
	}

	/**
	 * Checks if the methods of the given class should be explored for entry points.
	 *
	 * @param sc is the class that may provide entry points.
	 * @param classNames is the names of the class that form the system to be analyzed.
	 *
	 * @return <code>true</code> if <code>sc</code> should be explored; <code>false</code>, otherwise.
	 *
	 * @pre sc != null and classNames != null and classNames.oclIsKindOf(Collection(String))
	 */
	protected boolean considerClassForEntryPoint(SootClass sc, Collection classNames) {
		return classNames.contains(sc.getName()) && sc.isPublic();
	}

	/**
	 * The analyses need to be driven in this method.  Concrete drivers should implement this method.
	 */
	protected abstract void execute();

	/**
	 * Prints the timing statistics into the given stream.
	 *
	 * @param stream into which the timing statistics should be written to.
	 *
	 * @pre stream != null
	 */
	protected void printTimingStats(PrintStream stream) {
		stream.println("Timing statistics:");

		for (Iterator i = times.keySet().iterator(); i.hasNext();) {
			Object e = i.next();
			stream.println(e + " => " + times.get(e) + "ms");
		}
	}

	/**
	 * Records methods that should be considered as entry points into the system being analyzed.
	 *
	 * @param sm is the method that may be an entry point into the system.
	 *
	 * @post rootMethods->includes(sm) or not rootMethods->includes(sm)
	 * @pre sm != null
	 */
	protected void trapRootMethods(SootMethod sm) {
		if (sm.getName().equals("main")
			  && sm.isStatic()
			  && sm.getParameterCount() == 1
			  && sm.getParameterType(0).equals(STR_ARRAY_TYPE)) {
			rootMethods.add(sm);
		}
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
 */
