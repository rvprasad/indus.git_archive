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
 
package edu.ksu.cis.indus.kaveri.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.internal.corext.callhierarchy.CallLocation;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;
import org.eclipse.jdt.internal.ui.callhierarchy.CallHierarchyViewPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import soot.SootMethod;
import soot.jimple.InvokeExpr;
import soot.jimple.Stmt;
import edu.ksu.cis.indus.common.datastructures.Triple;
import edu.ksu.cis.indus.interfaces.ICallGraphInfo.CallTriple;
import edu.ksu.cis.indus.kaveri.KaveriPlugin;

/**
 * @author ganeshan
 *
 * 
 * 
 */
public class AddToContext implements IViewActionDelegate {

    private IStructuredSelection sSel;
    private CallHierarchyViewPart callViewPart;

    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {        
        this.callViewPart = (CallHierarchyViewPart) view;        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        if (sSel != null) {
            if (sSel.getFirstElement() instanceof MethodWrapper) {
                System.out.println();
                MethodWrapper _mw = (MethodWrapper) (sSel.getFirstElement());
                final boolean _result = validateForCalleeView(_mw, _mw.getParent());
                if (!_result) {
                    MessageDialog.openError(null, "Please Switch", "Switch to callee view before using this function");
                } else {
                    final Collection _coll = new ArrayList();
                    MethodCallContext _context = null;
                    generateCallStacks(_coll, _mw, new Stack());
                    for (Iterator iter = _coll.iterator(); iter.hasNext();) {
                        final Stack _stk = (Stack) iter.next();
                        if (_context == null) {
                            final Triple _botTriple = (Triple)  _stk.get(0);
                            final MethodWrapper _mStart = (MethodWrapper) _botTriple.getFirst();
                            final Triple _ltTriple = (Triple) _stk.peek();
                            final MethodWrapper _mEnd = (MethodWrapper) _ltTriple.getSecond();
                            
                            final IMethod _srcMethod = (IMethod) _mStart.getMember();
                            final IMethod _destMethod = (IMethod) _mEnd.getMember();
                            
                            _context = new MethodCallContext(_srcMethod, _destMethod);                                
                        }
                        _context.addContext(_stk);                        
                    }
                    if (_context != null) {
                        KaveriPlugin.getDefault().getIndusConfiguration().addContext(_context);                        
                    }
                }
            }
        }

    }

    /**
     * Generate all the call stack traces from the given method wrapper, using DFS.
     * @param stkcoll The collection of call stacks.
     * @param mw The method wrapper.
     * @param pathStack The stack on the individual call trace.
     */
    private void generateCallStacks(Collection stkcoll, MethodWrapper mw, final Stack pathStack) {
        final Collection _coll = mw.getMethodCall().getCallLocations();
        if (_coll == null) {
            stkcoll.add(pathStack.clone());
            return;
        }
        for (Iterator iter = _coll.iterator(); iter.hasNext();) {
            final CallLocation _callLoc = (CallLocation) iter.next();
            final Triple _triple = new Triple(mw, mw.getParent(), _callLoc);
            pathStack.push(_triple);
            generateCallStacks(stkcoll, mw.getParent(), pathStack);
            pathStack.pop();
        }
        
    }

    /**
     * Get the invoke expression for the given method.
     * @param jimplelist The list of jimple in which the call lies.
     * @param callee
     * @param caller
     * @return
     */
    private CallTriple fetchTriple(final List jimplelist, final SootMethod callee, final SootMethod caller) {
        CallTriple _triple = null;
        for (Iterator iter = jimplelist.iterator(); iter.hasNext();) {
            final Stmt _stmt = (Stmt) iter.next();
            if (_stmt.containsInvokeExpr()) {
                final InvokeExpr _expr1 = _stmt.getInvokeExpr();                
                if (_expr1.getMethod().equals(callee)) {
                    _triple = new CallTriple(caller, _stmt, _expr1);
                    break;
                }
            }                        
        }
        return _triple;
    }

    /**
     * Checks if the callee view has been activated.
     * @param mw The source method wrapper.
     * @param parent The parent method wrapper.
     * @return 
     */
    private boolean validateForCalleeView(MethodWrapper mw, MethodWrapper parent) {
        boolean _result = false;
        if (mw != null && parent != null) {
            final CallLocation _loc = mw.getMethodCall().getFirstCallLocation();
            if (_loc != null) {
                final IMethod _methodCall= (IMethod) _loc.getMember();
                final IMethod _srcMethod = (IMethod) mw .getMember();
                final IMethod _callMethod = (IMethod) parent.getMember();
                if (_methodCall.equals(_callMethod)) {
                    _result = true;
                }
            }
        }
        return _result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            final IStructuredSelection _ssl = (IStructuredSelection) selection;
           this.sSel = _ssl;
        }

    }

}
