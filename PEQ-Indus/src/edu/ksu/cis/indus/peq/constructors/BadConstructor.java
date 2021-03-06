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
 
package edu.ksu.cis.indus.peq.constructors;

import edu.ksu.cis.indus.peq.fsm.FSMToken;
import edu.ksu.cis.indus.peq.graph.Edge;
import edu.ksu.cis.peq.fsm.interfaces.IFSMToken;

/**
 * @author ganeshan
 *
 * An instance of the bad constructor. 
 * Rejects everything.
 */
public class BadConstructor extends GeneralConstructor {

    /* (non-Javadoc)
     * @see edu.ksu.cis.indus.peq.constructors.GeneralConstructor#match(edu.ksu.cis.indus.peq.constructors.GeneralConstructor, edu.ksu.cis.indus.peq.graph.Edge)
     */
    public IFSMToken match(GeneralConstructor cons, Edge masterEdge)
            throws IllegalAccessException {
        FSMToken _token = new FSMToken();
        _token.setEmpty(true);
        return _token;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object) {
        if (!(object instanceof BadConstructor)) {
            return false;
        }
        BadConstructor rhs = (BadConstructor) object;
        return super.equals(rhs);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        int _result = 17;
        _result = _result * 37 + 15; 
        _result = _result * 37 + super.hashCode();
        return _result;
    }
}
