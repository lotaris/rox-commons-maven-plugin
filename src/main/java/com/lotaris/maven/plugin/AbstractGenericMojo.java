package com.lotaris.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.filtering.MavenResourcesFiltering;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;

/**
 * Consolid some commons elements for the Lotaris Maven Plugins that needs
 * to use ROX Center
 *
 * @author Laurent Prévost, laurent.prevost@lotaris.com
 */
public abstract class AbstractGenericMojo extends AbstractMojo {
	/**
	 * Define if more logs must be shown or not
	 */
	@Parameter(defaultValue = "false")
	protected Boolean verbose;
	
	/**
	 * Define if the plugin must be skip
	 */
	@Parameter(defaultValue = "false")
	private Boolean skip;
	
	@Parameter
	protected Long seed = null;
	
	/**
	 * The directory containing generated test classes of the project being tested. This will be included at the
	 * beginning of the test classpath.
	 */
	@Parameter( defaultValue = "${project.build.testOutputDirectory}" )
	protected File testClassesDirectory;	
	
	/**
	 * The directory containing generated classes of the project being tested. This will be included after the test
	 * classes in the test classpath.
	 */
	@Parameter( defaultValue = "${project.build.outputDirectory}" )
	protected File classesDirectory;
		
	/**
	 * The Maven Project Object
	 */
	@Parameter( defaultValue = "${project}", required = true, readonly = true )	
	protected MavenProject project;

	/**
	 * The Maven Session Object
	 */
	@Parameter( defaultValue = "${session}", required = true, readonly = true )
	protected MavenSession session;

	/**
	 * The Maven PluginManager Object
	 */
	@Component(role = BuildPluginManager.class, hint = "default")
	protected BuildPluginManager pluginManager;	
	
	/**
	 * Maven resources filtering
	 */
	@Component(role = MavenResourcesFiltering.class, hint = "default")
	protected MavenResourcesFiltering mavenResourcesFiltering;

	@Override
	public final void execute() throws MojoExecutionException {
		if (skip) {
			getLog().info("Plugin execution skipped");
			return;
		}

		try {
			setup();
	
			run();
		}
		finally {
			cleanup();
		}
	}
	
	/**
	 * Configure the plugin
	 * 
	 * @throws MojoExecutionException Throws when error occured in the clean plugin 
	 */
	protected void setup() throws MojoExecutionException {}
	
	/**
	 * Run the main actions of the plugin
	 * 
	 * @throws MojoExecutionException Throws when error occured in the clean plugin 
	 */
	protected abstract void run() throws MojoExecutionException;

	/**
	 * Executed after the run to clean working files
	 * 
	 * @throws MojoExecutionException Throws when error occured in the clean plugin 
	 */
	protected void cleanup() throws MojoExecutionException {}
	
	/**
	 * Utility method to use the clean plugin in the cleanup methods
	 * 
	 * @param elements Elements to configure the clean plugin
	 * @throws MojoExecutionException Throws when error occurred in the clean plugin 
	 */
	protected void useCleanPlugin(Element ... elements) throws MojoExecutionException {
		List<Element> tempElems = new ArrayList<Element>(Arrays.asList(elements));
		tempElems.add(new Element("excludeDefaultDirectories", "true"));
		
		// Configure the Maven Clean Plugin to clean working files
		executeMojo(
			plugin(
				groupId("org.apache.maven.plugins"),
				artifactId("maven-clean-plugin"),
				version("2.5")
			), 
			goal("clean"),
			configuration(
				tempElems.toArray(new Element[tempElems.size()])
			),
			executionEnvironment(
				project, 
				session, 
				pluginManager
			)
		);		
	}
	
	/**
	 * @return The current working directory where the maven plugin is executed
	 */
	protected File getWorkingDirectory() {
		return new File(System.getProperty("user.dir"));
	}
}
