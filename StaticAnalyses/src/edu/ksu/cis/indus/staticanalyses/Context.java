
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

package edu.ksu.cis.indus.staticanalyses;

import soot.SootMethod;
import soot.ValueBox;

import soot.jimple.Stmt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.EmptyStackException;
import java.util.Stack;


/**
 * The context information is encapsulated in this class.  It can support flow-sensitive, allocation-site-sensitive, and
 * call-stack sensitive context information.    Created: Tue Jan 22 05:29:22 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$
 */
public class Context
  implements Cloneable {
	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(Context.class);

	/**
	 * The call-stack sensitive component of the context.  This is relevant in call-site sensitive mode of analysis.
	 */
	protected Stack callString;

	/**
	 * The statement component of the context.  This component can be used when the entity associated with the context is an
	 * expression.
	 */
	protected Stmt stmt;

	/**
	 * The program point component of the context.  This is relevant in the flow-sensitive mode of analysis.
	 */
	protected ValueBox progPoint;

	/**
	 * Creates a new <code>Context</code> instance with an emtpy call stack and <code>null</code> for program point and
	 * allocation site.
	 */
	public Context() {
		callString = new Stack();
	}

	/**
	 * Returns the call stack of this context.
	 *
	 * @return the call stack of the this context.  Any operation on this object affects the call stack of this context.
	 */
	public Stack getCallString() {
		Stack temp = new Stack();
		temp.addAll(callString);
		return temp;
	}

	/**
	 * Returns the current method in this context.
	 *
	 * @return the current method in this context.
	 */
	public SootMethod getCurrentMethod() {
		SootMethod result = null;

		try {
			result = (SootMethod) callString.peek();
		} catch (EmptyStackException e) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("There are no methods in the call stack.", e);
			}
		}
		return result;
	}

	/**
	 * Sets the program point in this context.
	 *
	 * @param pp the program point in this context.
	 *
	 * @return the program point previously represented by this context.
	 */
	public ValueBox setProgramPoint(final ValueBox pp) {
		ValueBox temp = progPoint;
		progPoint = pp;

		return temp;
	}

	/**
	 * Returns the program point in this context.
	 *
	 * @return the program point in this context.
	 */
	public ValueBox getProgramPoint() {
		return progPoint;
	}

	/**
	 * Updates the call stack to reflect that the given method is the first method call in this context.  It empties the call
	 * stack and installs the given method as the current method.
	 *
	 * @param sm the method to be installed as the current method and the only method on the call stack in this context.
	 */
	public void setRootMethod(final SootMethod sm) {
		callString.removeAllElements();
		callString.push(sm);
	}

	/**
	 * Sets the given statement as the statement in this context.
	 *
	 * @param stmtParam to be set as the statement in this context.
	 *
	 * @return the previous statement in this context.
	 */
	public Stmt setStmt(final Stmt stmtParam) {
		Stmt result = this.stmt;
		this.stmt = stmtParam;
		return result;
	}

	/**
	 * Returns the statement in this context.
	 *
	 * @return the statement in this context.
	 */
	public Stmt getStmt() {
		return stmt;
	}

	/**
	 * Updates the call stack to reflect a new method call in the current context.
	 *
	 * @param sm the method being called in the current context.  This cannot be <code>null</code>.
	 */
	public void callNewMethod(final SootMethod sm) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding method " + sm);
		}

		callString.push(sm);
	}

	/**
	 * Clones the current object.  The objects representing the call stacks are deep cloned.
	 *
	 * @return the clone of the current context.
	 */
	public Object clone() {
		Context temp = null;

		try {
			temp = (Context) super.clone();
			temp.callString = (Stack) callString.clone();
		} catch (CloneNotSupportedException e) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("This should not happen.", e);
			}
		} finally {
			return temp;
		}
	}

	/**
	 * Checks if the given context and this context represent the same context.
	 *
	 * @param o the context to be compared for equality with this context.
	 *
	 * @return <code>true</code> if <code>c</code> and this context represent the same context; <code>false</code> otherwise.
	 */
	public boolean equals(final Object o) {
		boolean ret = false;

		if (o != null && o instanceof Context) {
			Context c = (Context) o;

			if (progPoint != null) {
				ret = progPoint.equals(c.progPoint);
			} else {
				ret = progPoint == c.progPoint;
			}

			if (ret) {
				if (stmt != null) {
					ret = stmt.equals(c.stmt);
				} else {
					ret = stmt == c.stmt;
				}

				if (ret) {
					if (callString != null) {
						ret &= callString.equals(c.callString);
					} else {
						ret &= callString == c.callString;
					}
				}
			}
		}
		return ret;
	}

	/**
	 * Returns the hash code of this object.  It is derived from the call stack and the program point.
	 *
	 * @return the hash code of this object.
	 */
	public int hashCode() {
		int result = 17;

		if (progPoint != null) {
			result = 37 * result + progPoint.hashCode();
		}

		if (stmt != null) {
			result = 37 * result + stmt.hashCode();
		}

		result = 37 * result + callString.hashCode();
		return result;
	}

	/**
	 * Updates the call stack to reflect the return from the current method in this context.
	 *
	 * @return the method returned from.
	 */
	public SootMethod returnFromCurrentMethod() {
		return (SootMethod) callString.pop();
	}

	/**
	 * Returns the stringized representation of this context.
	 *
	 * @return the stringized representation of this context.
	 */
	public String toString() {
		return "Context:\n\tProgram Point: " + progPoint + "\n\tStmt: " + stmt + "\n\tCallStack: " + callString + "\n";
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/07 06:42:16  venku
   Major:
    - Moved the package under indus umbrella.
    - Renamed isEmpty() to hasWork() in WorkBag.
   Revision 0.10  2003/05/22 22:18:31  venku
   All the interfaces were renamed to start with an "I".
   Optimizing changes related Strings were made.
 */
