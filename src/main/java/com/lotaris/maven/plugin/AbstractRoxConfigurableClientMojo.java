package com.lotaris.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.shared.filtering.MavenFilteringException;
import org.apache.maven.shared.filtering.MavenResourcesExecution;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Consolid some commons elements for the Lotaris Maven Plugins that needs
 * to use ROX Center
 *
 * @author Laurent Prévost, laurent.prevost@lotaris.com
 */
public abstract class AbstractRoxConfigurableClientMojo extends AbstractGenericMojo {
	/**
	 * constants
	 */
	protected static final String ROX_PROPERTIES_FILENAME = "rox.yml";
	
	/**
	 * The filters to use for filtering dedicated tests
	 */
	@Parameter
	protected List<String> filters = new ArrayList<String>(); 	
	
	/**
	 * The character encoding scheme to be applied when filtering resources.
	 */
	@Parameter( property = "encoding", defaultValue = "${project.build.sourceEncoding}" )
	protected String encoding; 

	/**
	 * Define if ROX is active or not
	 */
	@Parameter(defaultValue = "true")
	protected boolean roxActive = true;
	
	/**
	 * Configuration for ROX
	 */
	@Parameter(defaultValue = "${project.basedir}/src/test/resources/rox.yml")
	private File roxConfig;
	
	/**
	 * Run the main actions of the plugin
	 * 
	 * @throws MojoExecutionException Throws when error occured in the clean plugin 
	 */
	protected abstract void run() throws MojoExecutionException;

	/**
	 * Apply the internal setup for the plugin
	 * 
	 * @throws MojoExecutionException Throws when error occurred in the clean plugin 
	 */
	@Override
	protected void setup() throws MojoExecutionException {
		if (verbose) {
			getLog().info("Rox configuration is generated");
		}
		
		// Check if the ROX configuration is available
		if (roxActive && roxConfig != null) {
			// Check the ROX configuration file
			if (!roxConfig.exists() || !roxConfig.isFile() || !roxConfig.getAbsolutePath().endsWith(".yml")) {
				getLog().warn("The " + ROX_PROPERTIES_FILENAME + " configuration seems not to be a valid file or path is incorrect. Rox will be disabled.");
				roxActive = false;
			}
			else {
				try {
					// Prepare the resource to copy
					Resource r = new Resource();
					r.setFiltering(true);
					r.setDirectory(roxConfig.getParent());
					r.setIncludes(Arrays.asList(new String[] {roxConfig.getName()}));

					// Configure the resource filtering
					MavenResourcesExecution mre = new MavenResourcesExecution(
						Arrays.asList(new Resource[] {r}), getWorkingDirectory(), project, encoding, null, null, session
					);
					
					// Filter the resources
					mavenResourcesFiltering.filterResources(mre);
				}
				catch (MavenFilteringException mfe) {
					getLog().warn("Unable to filter the " + ROX_PROPERTIES_FILENAME + " file. Rox will be disabled.", mfe);
					roxActive = false;
				}
			}
		}
		else {
			getLog().info("No rox configuration to use");
			roxActive = false;
		}		
	}	
	
	/**
	 * Clean the ROX configuration files
	 * 
	 * @throws MojoExecutionException Throws when error occurred in the clean plugin 
	 */
	@Override
	protected void cleanup() throws MojoExecutionException {
		if (verbose) {
			getLog().info("Clean the rox configuration files");
		}
		
		if (roxActive) {
			// Cleanup the rox configuration files
			useCleanPlugin(
				element("filesets", 
					element("fileset",
						element("directory", getWorkingDirectory().getAbsolutePath()),
						element("followSymlinks", "false"),
						element("includes",
							element("include", ROX_PROPERTIES_FILENAME) // Clean ROX configuration file
						)
					)
				)
			);
		}
	}
}
