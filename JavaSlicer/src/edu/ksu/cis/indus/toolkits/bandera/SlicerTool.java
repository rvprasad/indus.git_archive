
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

package edu.ksu.cis.indus.toolkits.bandera;

import soot.Scene;

import edu.ksu.cis.bandera.tool.Tool;
import edu.ksu.cis.bandera.tool.ToolConfigurationView;
import edu.ksu.cis.bandera.tool.ToolIconView;
import edu.ksu.cis.bandera.util.BaseObservable;
import edu.ksu.cis.indus.tools.Phase;
import edu.ksu.cis.indus.transformations.slicer.TagBasedSlicingTransformer;
import edu.ksu.cis.indus.slicer.SliceCriteriaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * This class wraps the slicer in the tool interface required by the tool pipeline in Bandera.
 *
 * @author <a href="http://www.cis.ksu.edu/~rvprasad">Venkatesh Prasad Ranganath</a>
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SlicerTool
  extends BaseObservable
  implements Tool {
	/**
	 * This identifies the scene in the input arguments.
	 */
	public static final Object SCENE = "scene";

	/**
	 * This identifies the root methods/entry point methods in the input arguments.
	 */
	public static final Object ROOT_METHODS = "entryPoints";

	/**
	 * This identifies the slicing criteria in the input arguments.
	 */
	public static final Object CRITERIA = "slicingCriteria";

	/**
	 * This identifies the slicing tag name in the input arguments.
	 */
	public static final String TAG_NAME = "slicingTagName";

	/**
	 * The collection of input argument identifiers.
	 */
	private static final List IN_ARGUMENTS_IDS;

	/**
	 * The collection of output argument identifiers.
	 */
	private static final List OUT_ARGUMENTS_IDS;

	static {
		IN_ARGUMENTS_IDS = new ArrayList();
		IN_ARGUMENTS_IDS.add(SCENE);
		IN_ARGUMENTS_IDS.add(ROOT_METHODS);
		IN_ARGUMENTS_IDS.add(CRITERIA);
		IN_ARGUMENTS_IDS.add(TAG_NAME);
		OUT_ARGUMENTS_IDS = new ArrayList();
		OUT_ARGUMENTS_IDS.add(SCENE);
	}

	/**
	 * The logger used by instances of this class to log messages.
	 */
	private static final Log LOGGER = LogFactory.getLog(SlicerTool.class);

	/**
	 * The slicer tool that is adapted by this object.
	 */
	private final edu.ksu.cis.indus.tools.slicer.SlicerTool tool;

	/**
	 * The configuration interface provided by this object to configure the slicer tool.
	 */
	private SlicerConfigurationView configurationView;

	/**
	 * Creates a new SlicerTool object.
	 */
	public SlicerTool() {
		tool = new edu.ksu.cis.indus.tools.slicer.SlicerTool();

		TagBasedSlicingTransformer tgsbt = new TagBasedSlicingTransformer();
		tgsbt.setTagName("Bandera");
		tool.setTransformer(tgsbt);

		configurationView = new SlicerConfigurationView(tool.getConfigurator());
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#setConfiguration(java.lang.String)
	 */
	public void setConfiguration(final String configStr)
	  throws Exception {
		tool.destringizeConfiguration(configStr);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getConfiguration()
	 */
	public String getConfiguration() {
		return tool.stringizeConfiguration();
	}

	/**
	 * {@inheritDoc}
	 *
	 * @param inputArgs maps the input argument identifiers to the arguments.
	 *
	 * @pre inputArgs.get(SCENE) != null and inputArgs.get(SCENE).oclIsKindOf(Scene)
	 * @pre inputArgs.get(CRITERIA) != null and
	 * 		inputArgs.get(CRITERIA).oclIsKindOf(Collection(edu.ksu.cis.indus.slicer.AbstractSliceCriterion))
	 * @pre inputArgs.get(TAG_NAME) != null and inputArgs.get(TAG_NAME).oclIsKindOf(String)
	 * @pre inputArgs.get(ROOT_METHODS) != null and inputArgs.get(ROOT_METHODS).oclIsKindOf(Collection(SootMethod))
	 *
	 * @see edu.ksu.cis.bandera.tool.Tool#setInputMap(java.util.Map)
	 */
	public void setInputMap(final Map inputArgs) {
		Scene theScene = (Scene) inputArgs.get(SCENE);

		if (theScene == null) {
			LOGGER.error("A scene must be provided for slicing.");
			throw new IllegalArgumentException("A scene must be provided for slicing.");
		}
		tool.setSystem(theScene);

		Collection criteria = (Collection) inputArgs.get(CRITERIA);

		if (criteria == null) {
			LOGGER.error("Atlease one slicing criteria should be specified.");
			throw new IllegalArgumentException("Atlease one slicing criteria should be specified.");
		} else if (criteria.isEmpty()) {
			LOGGER.warn("Deadlock criteria will be used.");
		} else {
			tool.setCriteria(criteria);
		}

		for (Iterator i = criteria.iterator(); i.hasNext();) {
			Object o = i.next();

			if (!SliceCriteriaFactory.isSlicingCriterion(o)) {
				LOGGER.error(o
					+ " is an invalid slicing criterion.  All slicing criterion should be created via SliceCriteriaFactory.");
				throw new IllegalArgumentException("Slicing criteion " + o + " was not created by SliceCriteriaFactory.");
			}
		}

		Collection rootMethods = (Collection) inputArgs.get(ROOT_METHODS);

		if (criteria == null || criteria.isEmpty()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Atleast one method should be specified as the entry-point into the system.");
			}
		}
		tool.setRootMethods(rootMethods);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getInputParameterList()
	 */
	public List getInputParameterList() {
		return Collections.unmodifiableList(IN_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputMap()
	 */
	public Map getOutputMap() {
		Map outputMap = new HashMap();
		outputMap.put(SCENE, tool.getSystem());
		return outputMap;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getOutputParameterList()
	 */
	public List getOutputParameterList() {
		return Collections.unmodifiableList(OUT_ARGUMENTS_IDS);
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#getToolConfigurationView()
	 */
	public ToolConfigurationView getToolConfigurationView() {
		return configurationView;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.AbstractTool#getToolIconView()
	 */
	public ToolIconView getToolIconView() {
		return configurationView;
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#quit()
	 */
	public void quit()
	  throws Exception {
	}

	/**
	 * @see edu.ksu.cis.bandera.tool.Tool#run()
	 */
	public void run()
	  throws Exception {
		tool.run(Phase.STARTING_PHASE);
	}
}

/*
   ChangeLog:
   $Log$
   Revision 1.9  2003/10/12 19:45:05  venku
    - Changed valus of input/output args Ids as per the suggestion
      of Todd.

   Revision 1.8  2003/09/28 23:16:18  venku
   - documentation
   Revision 1.7  2003/09/27 22:38:30  venku
   - package documentation.
   - formatting.
   Revision 1.6  2003/09/27 01:27:46  venku
   - documentation.
   Revision 1.5  2003/09/27 01:09:35  venku
   - changed AbstractToolConfigurator and CompositeToolConfigurator
     such that the composite to display the interface on is provided by the application.
   - documentation.
   Revision 1.4  2003/09/26 15:07:51  venku
   - completed support for exposing slicer as a tool
     and configuring it both in Bandera and outside it.
   Revision 1.3  2003/09/26 05:55:51  venku
   - a checkpoint commit.
   Revision 1.2  2003/09/24 07:33:24  venku
   - Nightly commit.
   - Need to wrap the indus tool api in ways specific to bandera
     tool api.
   Revision 1.1  2003/09/24 01:43:45  venku
   - Renamed edu.ksu.cis.indus.tools to edu.ksu.cis.indus.toolkits.
     This package is to house adaptation of each tools for each toolkits.
   - Retained edu.ksu.cis.indus.tools to contain API/interface to expose
     the implementation as a tool.
   Revision 1.1  2003/09/15 08:55:23  venku
   - Well, the SlicerTool is still a mess in my opinion as it needs
     to be implemented as required by Bandera.  It needs to be
     much richer than it is to drive the slicer.
   - SlicerConfigurator is supposed to bridge the above gap.
     I doubt it.
 */
