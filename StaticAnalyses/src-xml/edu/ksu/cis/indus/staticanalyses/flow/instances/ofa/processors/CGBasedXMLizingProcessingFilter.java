
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

import edu.ksu.cis.indus.interfaces.ICallGraphInfo;

import edu.ksu.cis.indus.staticanalyses.processing.CGBasedProcessingFilter;

import edu.ksu.cis.indus.xmlizer.XMLizingProcessingFilter;


/**
 * This is a call-graph based xmlizing controller.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public final class CGBasedXMLizingProcessingFilter
  extends CGBasedProcessingFilter {
	/**
	 * Creates an instance of this class.
	 *
	 * @param cgiPrm provides the call graph information required during controlling.
	 *
	 * @pre cgiPrm != null
	 * @post xmlizingController != null
	 */
	public CGBasedXMLizingProcessingFilter(final ICallGraphInfo cgiPrm) {
		super(cgiPrm);
		chain(new XMLizingProcessingFilter());
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.2  2004/02/09 06:49:02  venku
   - deleted dependency xmlization and test classes.
   Revision 1.1  2004/02/08 03:05:46  venku
   - renamed xmlizer packages to be in par with the packages
     that contain the classes whose data is being xmlized.
   Revision 1.5  2003/12/13 02:29:08  venku
   - Refactoring, documentation, coding convention, and
     formatting.
   Revision 1.4  2003/12/08 12:20:44  venku
   - moved some classes from staticanalyses interface to indus interface package
   - ripple effect.
   Revision 1.3  2003/12/02 09:42:39  venku
   - well well well. coding convention and formatting changed
     as a result of embracing checkstyle 3.2
   Revision 1.2  2003/11/30 09:03:23  venku
   - changed filed name to something more appropriate.
   Revision 1.1  2003/11/30 01:17:15  venku
   - renamed CGBasedXMLizingFilter to CGBasedXMLizingProcessingFilter.
   - renamed XMLizingController to XMLizingProcessingFilter.
   - ripple effect.
   Revision 1.1  2003/11/30 00:10:24  venku
   - Major refactoring:
     ProcessingController is more based on the sort it controls.
     The filtering of class is another concern with it's own
     branch in the inheritance tree.  So, the user can tune the
     controller with a filter independent of the sort of processors.
   Revision 1.3  2003/11/17 16:58:15  venku
   - populateDAs() needs to be called from outside the constructor.
   - filterClasses() was called in CGBasedXMLizingController instead of filterMethods. FIXED.
   Revision 1.2  2003/11/17 02:23:56  venku
   - documentation.
   - xmlizers require streams/writers to be provided to them
     rather than they constructing them.
   Revision 1.1  2003/11/12 05:18:54  venku
   - moved xmlizing classes to a different class.
   Revision 1.1  2003/11/12 05:05:45  venku
   - Renamed SootDependentTest to SootBasedDriver.
   - Switched the contents of DependencyXMLizerDriver and DependencyTest.
   - Corrected errors which emitting xml tags.
   - added a scrapbook.
 */