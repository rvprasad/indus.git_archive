
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

package edu.ksu.cis.indus.transformations.common;

import edu.ksu.cis.indus.interfaces.ISystemInfo;
import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Type;
import soot.Value;

import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.Stmt;

import soot.util.Chain;

import java.util.Iterator;


/**
 * This class is responsible for cloning of parts of the system during program transformation.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class Cloner
  implements ASTCloner.IASTClonerHelper {
	/**
	 * This instance is used to clone Jimple AST chunks.
	 *
	 * @invariant astCloner != null
	 */
	private final ASTCloner astCloner = new ASTCloner(this);

	/**
	 * This is a reference to the jimple body representation.
	 *
	 * @invariant jimple != null
	 */
	private final Jimple jimple = Jimple.v();

	/**
	 * This provides information about the system such as statement graphs and such that are common to analyses and
	 * transformations.
	 */
	private ISystemInfo sysInfo;

	/**
	 * The class manager which manages clonee classes.
	 */
	private Scene clazzManager;

	/**
	 * The class manager which manages clone classes.
	 */
	private Scene cloneClazzManager;

	/**
	 * Clones of the given class if one does not exists.  If one exists, that is returned.
	 *
	 * @param clazz to be cloned.
	 *
	 * @return the clone of <code>clazz</code>.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	public SootClass getCloneOf(final SootClass clazz) {
		String clazzName = clazz.getName();
		boolean declares = cloneClazzManager.containsClass(clazzName);
		SootClass result;

		if (declares) {
			result = cloneClazzManager.getSootClass(clazzName);
		} else {
			result = clone(clazz);
		}
		return result;
	}

	/**
	 * Clones of the given field if one does not exists.  If one exists, that is returned.
	 *
	 * @param field to be cloned.
	 *
	 * @return the cone of <code>field</code>.
	 *
	 * @pre field != null
	 * @post result != null
	 */
	public SootField getCloneOf(final SootField field) {
		SootClass clazz = getCloneOf(field.getDeclaringClass());
		String name = field.getName();
		Type type = field.getType();

		if (!clazz.declaresField(name, type)) {
			clazz.addField(new SootField(name, type, field.getModifiers()));
		}
		return clazz.getField(name, type);
	}

	/**
	 * Clones the given method if one does not exists.  If one exists, that is returned.  The statement list of the method
	 * body of the clone is equal in length to that of the given method but it only contains <code>NopStmt</code>s.
	 *
	 * @param cloneeMethod is the method to be cloned.
	 *
	 * @return the clone of <code>cloneMethod</code>.
	 *
	 * @pre cloneeMethod != null
	 * @post result != null
	 */
	public SootMethod getCloneOf(final SootMethod cloneeMethod) {
		SootClass sc = getCloneOf(cloneeMethod.getDeclaringClass());
		boolean declares =
			sc.declaresMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		SootMethod result;

		if (declares) {
			result = sc.getMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType());
		} else {
			result =
				new SootMethod(cloneeMethod.getName(), cloneeMethod.getParameterTypes(), cloneeMethod.getReturnType(),
					cloneeMethod.getModifiers());

			for (Iterator i = cloneeMethod.getExceptions().iterator(); i.hasNext();) {
				SootClass exception = (SootClass) i.next();
				result.addException(exception);
			}

			JimpleBody jb = jimple.newBody(result);
			Chain sl = jb.getUnits();
			Stmt nop = jimple.newNopStmt();

			for (int i = sysInfo.getStmtGraph(cloneeMethod).getBody().getUnits().size() - 1; i >= 0; i--) {
				sl.addLast(nop);
			}
			result.setActiveBody(jb);
		}
		return result;
	}

	/**
	 * Returns the clone of the class named by <code>clazz</code> if one exists.
	 *
	 * @param clazz is the name of the class whose clone is requested.
	 *
	 * @return the clone of the requested class if it exists; <code>null</code> is returned otherwise.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	public SootClass getCloneOf(final String clazz) {
		SootClass sc = clazzManager.getSootClass(clazz);
		SootClass result = null;

		if (sc != null) {
			result = getCloneOf(sc);
		}

		return result;
	}

	/**
	 * Retrieves the jimple entity associated with the given local in the given method.
	 *
	 * @param name of the local.
	 * @param method in which the local occurs.
	 *
	 * @return the jimple entity corresponding to the requested local.
	 *
	 * @pre name != null and method != null
	 */
	public Local getLocal(final String name, final SootMethod method) {
		SootMethod sliceMethod = getCloneOf(method);
		Body body = sliceMethod.getActiveBody();
		Local result = null;

		for (Iterator i = body.getLocals().iterator(); i.hasNext();) {
			Local local = (Local) i.next();

			if (local.getName().equals(name)) {
				result = local;
				break;
			}
		}
		return result;
	}

	/**
	 * Clones a given Jimple statement that occurs in the given method.
	 *
	 * @param stmt is the Jimple statement to be cloned.
	 * @param cloneeMethod in which <code>stmt</code> occurs.
	 *
	 * @return the clone of <code>stmt</code>.
	 *
	 * @pre stmt != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Stmt cloneASTFragment(final Stmt stmt, final SootMethod cloneeMethod) {
		return astCloner.cloneASTFragment(stmt, cloneeMethod);
	}

	/**
	 * Clones a given Jimple value that occurs in the given method.
	 *
	 * @param value is the Jimple value to be cloned.
	 * @param cloneeMethod in which <code>value</code> occurs.
	 *
	 * @return the clone of <code>value</code>.
	 *
	 * @pre value != null and cloneeMethod != null
	 * @post result != null and result.oclIsTypeOf(stmt.evaluationType())
	 */
	public Value cloneASTFragment(final Value value, final SootMethod cloneeMethod) {
		return astCloner.cloneASTFragment(value, cloneeMethod);
	}

	/**
	 * Initializes the data structures.
	 *
	 * @param theSystem is the classes that form the system to be clone.
	 * @param cloneSystem is the system after slicing.
	 * @param systemInfo that provides information about the system.
	 *
	 * @pre cloneeSystem != null and cloneSystem != null and theController != null
	 */
	public void initialize(final Scene theSystem, final Scene cloneSystem, final ISystemInfo systemInfo) {
		clazzManager = theSystem;
		cloneClazzManager = cloneSystem;
		sysInfo = systemInfo;
	}

	/**
	 * Resets the internal data structures.  For safe and meaningful operation after call to this method,
	 * <code>initialize()</code> should be called before calling any other methods.
	 */
	public void reset() {
		sysInfo = null;
		clazzManager = null;
	}

	/**
	 * Clones <code>clazz</code> in terms of inheritence and modifiers only.  The clone class has an empty body.
	 *
	 * @param clazz to clone
	 *
	 * @return the clone of <code>clazz</code>.
	 *
	 * @pre clazz != null
	 * @post result != null
	 */
	private SootClass clone(final SootClass clazz) {
		SootClass result = new SootClass(clazz.getName(), clazz.getModifiers());

		if (clazz.hasSuperclass()) {
			SootClass superClass = getCloneOf(clazz.getSuperclass());
			result.setSuperclass(superClass);
		}

		for (Iterator i = clazz.getInterfaces().iterator(); i.hasNext();) {
			SootClass cloneeInterface = (SootClass) i.next();
			SootClass cloneInterface = getCloneOf(cloneeInterface);
			result.addInterface(cloneInterface);
		}
		return result;
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.1  2003/08/18 04:01:52  venku
   Major changes:
    - Teased apart cloning logic in the slicer.  Made it transformation independent.
    - Moved it under transformation common location under indus.

 */
