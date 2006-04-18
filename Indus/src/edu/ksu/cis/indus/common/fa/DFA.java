/*
 * Indus, a toolkit to customize and adapt Java programs.
 * Copyright (c) 2003, 2004, 2005 SAnToS Laboratory, Kansas State University
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

package edu.ksu.cis.indus.common.fa;

import edu.ksu.cis.indus.annotations.Functional;
import edu.ksu.cis.indus.annotations.Immutable;
import edu.ksu.cis.indus.annotations.NonNull;

import java.util.Collection;

/**
 * This is an implementation of deterministic finite automaton.
 * 
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$ *
 * @param <S> the type of the implementation of this interface.
 * @param <L> the type of the implementation of this interface.
 */
public class DFA<S extends IState<S>, L extends ITransitionLabel<L>>
		extends NFA<S, L> {

	/**
	 * Creates an instance of this class.
	 * 
	 * @param eFactory <i>refer to documentation in super class constructor</i>
	 */
	public DFA(@NonNull @Immutable final ITransitionLabel.IEpsilonLabelFactory<L> eFactory) {
		super(eFactory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override public void addLabelledTransitionFromTo(@NonNull @Immutable final S src, @NonNull @Immutable final L label,
			@NonNull @Immutable final S dest) {
		final Collection<S> _states = getResultingStates(src, label);

		if (!_states.isEmpty()) {
			final String _msg = "A transition labelled '" + label + "' already exists from the given source (" + src + ")";
			throw new IllegalStateException(_msg);
		} else if (label.equals(epsilonFactory.getEpsilonTransitionLabel())) {
			final String _msg = "Epsilon transitions are not allowed in Deterministic automata.";
			throw new IllegalArgumentException(_msg);
		} else {
			super.addLabelledTransitionFromTo(src, label, dest);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override @Functional public boolean isDeterministic() {
		return true;
	}
}

// End of File
