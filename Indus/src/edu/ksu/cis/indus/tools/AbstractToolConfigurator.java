
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

package edu.ksu.cis.indus.tools;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;


/**
 * This class provides abstract implementation of <code>ITooConfigurator</code> interface which the concrete implementations
 * should extend.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$
 */
public abstract class AbstractToolConfigurator
  implements DisposeListener,
	  IToolConfigurator {
	/** 
	 * The parent composite on which the provided interface will be displayed.
	 */
	protected Composite parent;

	/** 
	 * This is the configuration to be handled by this object.
	 */
	protected IToolConfiguration configuration;

	/**
	 * This class handles the changing of boolean property as per to the selection of the associated button widget.
	 *
	 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
	 * @author $Author$
	 * @version $Revision$ $Date$
	 */
	protected static class BooleanPropertySelectionListener
	  implements SelectionListener {
		/** 
		 * The button widget that triggers property changes.
		 */
		protected final Button button;

		/** 
		 * The configuration that houses the associated property.
		 */
		protected final IToolConfiguration containingConfiguration;

		/** 
		 * The id of the property which can be changed via <code>button</code>.
		 */
		protected final Object id;

		/**
		 * Creates a new BooleanSelectionListener object.
		 *
		 * @param propID is the property id that can be changed via <code>sender</code>.
		 * @param sender is the button widget that is tied to the property.
		 * @param config is the confifugration that houses the given property.
		 *
		 * @pre propID != null and sender != null and config != null
		 */
		public BooleanPropertySelectionListener(final Object propID, final Button sender, final IToolConfiguration config) {
			id = propID;
			button = sender;
			containingConfiguration = config;
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(final SelectionEvent evt) {
			widgetSelected(evt);
		}

		/**
		 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		public void widgetSelected(final SelectionEvent evt) {
			containingConfiguration.setProperty(id, Boolean.valueOf(button.getSelection()));
		}
	}

	/**
	 * @see IToolConfigurator#setConfiguration(IToolConfiguration)
	 */
	public final void setConfiguration(final IToolConfiguration toolConfiguration) {
		checkConfiguration(toolConfiguration);
		configuration = toolConfiguration;
	}

	/**
	 * @see IToolConfigurator#initialize(Composite)
	 */
	public final void initialize(final Composite composite) {
		composite.removeDisposeListener(this);
		parent = composite;
		parent.addDisposeListener(this);
		setup();
	}

	/**
	 * @see IToolConfigurator#widgetDisposed(DisposeEvent)
	 */
	public void widgetDisposed(final DisposeEvent evt) {
	}

	/**
	 * Checks the given configuration.  This is an empty implementation.  Subclasses can check the configuration in this
	 * method.
	 *
	 * @param toolConfiguration to be checked.
	 *
	 * @pre toolConfiguration != null
	 */
	protected abstract void checkConfiguration(final IToolConfiguration toolConfiguration);

	/**
	 * Setup the graphical parts of the configurator.  This will be called before the configurator is displayed.
	 */
	protected abstract void setup();
}

// End of File
