package edu.ksu.cis.bandera.bfa;

import ca.mcgill.sable.soot.BodyRepresentation;
import ca.mcgill.sable.soot.SootMethod;
import ca.mcgill.sable.soot.VoidType;
import ca.mcgill.sable.soot.jimple.Jimple;
import ca.mcgill.sable.soot.jimple.Stmt;
import ca.mcgill.sable.soot.jimple.StmtBody;
import ca.mcgill.sable.soot.jimple.StmtList;
import ca.mcgill.sable.soot.jimple.Value;
import ca.mcgill.sable.util.Iterator;

import org.apache.log4j.Category;

/**
 * MethodVariant.java
 *
 *
 * Created: Tue Jan 22 05:27:59 2002
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @version $Revision$ $Name$
 */

public class MethodVariant implements Variant {

	private static final Category cat = Category.getInstance(MethodVariant.class.getName());

	protected final AbstractStmtSwitch stmt;

	protected final AbstractFGNode[] parameters;

	protected final AbstractFGNode thisVar;

	protected final AbstractFGNode returnVar;

	protected final ASTVariantManager astvm;

	public static final BodyRepresentation bodyrep = Jimple.v();

	public final BFA bfa;

	public final Context context;

	public final SootMethod sm;

	MethodVariant (SootMethod sm, ASTVariantManager astvm, BFA bfa){
		this.sm = sm;
		context = new Context();
		this.bfa = bfa;

		if (!sm.isStatic()) {
			thisVar = bfa.getFGNode();
		} // end of if (!sm.isStatic())
		else {
			thisVar = null;
		} // end of else

		if (!(sm.getReturnType() instanceof VoidType)) {
			returnVar = bfa.getFGNode();
		} // end of if (sm.getReturnType() instanceof VoidType)
		else {
			returnVar = null;
		} // end of else

		if (sm.getParameterCount() > 0) {
			parameters = new AbstractFGNode[sm.getParameterCount() - 1];
			for (int i = 0; i < sm.getParameterCount(); i++) {
				parameters[i] = bfa.getFGNode();
			} // end of for (int i = 0; i < sm.getParameterCount(); i++)
		} // end of if (sm.getParameterCount() > 0)
		else {
			parameters = new AbstractFGNode[0];
		} // end of else

		this.astvm = astvm;

		if (sm.isBodyStored(bodyrep)) {
			stmt = bfa.getStmt(this);
			StmtList list = ((StmtBody)sm.getBody(bodyrep)).getStmtList();
			for (Iterator i = list.iterator(); i.hasNext();) {
				stmt.process((Stmt)i.next());
			} // end of for (Iterator i = list.iterator(); i.hasNext();)
		} else {
			stmt = null;
		} // end of else

	}

	public final AbstractFGNode getASTNode(Value v) {
		return getASTVariant(v).getFGNode();
	}

	public final ASTVariant getASTVariant(Value v) {
		return (ASTVariant)astvm.select(v, context);
	}

	public final ASTVariant getASTVariant(Value v, Context context) {
		return (ASTVariant)astvm.select(v, context);
	}

	public final AbstractFGNode getParameterNode(int index) {
		return parameters[index];
	}

	public final AbstractFGNode getReturnNode() {
		return returnVar;
	}

	public final AbstractFGNode getThisNode() {
		return thisVar;
	}

}// MethodVariant
