/**
 * 
 */
package org.jenkins.plugins.audit2db.internal;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;

import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hibernate.SessionFactory;
import org.jenkins.plugins.audit2db.DbAuditPublisher;
import org.jenkins.plugins.audit2db.data.BuildDetailsRepository;
import org.jenkins.plugins.audit2db.internal.data.BuildDetailsHibernateRepository;
import org.jenkins.plugins.audit2db.internal.data.HibernateUtil;
import org.jenkins.plugins.audit2db.internal.model.BuildDetailsImpl;
import org.jenkins.plugins.audit2db.model.BuildDetails;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Marco Scata
 * 
 */
public class DbAuditPublisherImpl extends Notifier implements DbAuditPublisher {
	private final static Logger LOGGER = Logger.getLogger(DbAuditPublisherImpl.class.getName());

	// must be transient or it will be serialised in the job config
	private transient BuildDetailsHibernateRepository repository;
	
	public BuildDetailsRepository getRepository() {
		if (null == repository) {
			repository = new BuildDetailsHibernateRepository(getSessionFactory());
		}
		return repository;
	}

	/**
	 * Default constructor annotated as data-bound is needed
	 * to load up the saved xml configuration values.  
	 */
	@DataBoundConstructor
	public DbAuditPublisherImpl() {}
	
	/**
	 * The annotated descriptor will hold and display the 
	 * configuration info. It doesn't have to be an inner
	 * class, as most sample plugins seem to suggest.
	 */
	@Extension
	public final static DbAuditPublisherDescriptorImpl descriptor = new DbAuditPublisherDescriptorImpl(DbAuditPublisherImpl.class);
	
	@Override
	public BuildStepDescriptor<Publisher> getDescriptor() {
		LOGGER.log(Level.FINE, "Retrieving descriptor");
		return descriptor;
	}
	
	/**
	 * @see hudson.tasks.Notifier#needsToRunAfterFinalized()
	 */
	@Override
	public boolean needsToRunAfterFinalized() {
		// run even after the build is marked as complete
		return true;
	}
	
	/**
	 * @see hudson.tasks.BuildStep#getRequiredMonitorService()
	 */
	@Override
	public BuildStepMonitor getRequiredMonitorService() {
		return BuildStepMonitor.NONE;
	}

	private SessionFactory getSessionFactory() {
		final Properties props = HibernateUtil.getExtraProperties(
		descriptor.getJdbcDriver(), descriptor.getJdbcUrl(), 
		descriptor.getJdbcUser(), descriptor.getJdbcPassword());
		
		return HibernateUtil.getSessionFactory(props);
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
			BuildListener listener) throws InterruptedException, IOException {
		
		listener.getLogger().format("perform: %s; launcher: %s",
				build.getDisplayName(), launcher.toString());
		
		final BuildDetails details = getRepository().getBuildDetailsById(build.getId());
		details.setDuration(build.getDuration());
		details.setEndDate(new Date(
				details.getStartDate().getTime() + details.getDuration()));
		getRepository().updateBuildDetails(details);
		
		return true;
	}
	
	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {
		listener.getLogger().format("prebuild: %s;",
				build.getDisplayName());
		
		final BuildDetails details = new BuildDetailsImpl(build);
		final Object id = getRepository().saveBuildDetails(details);
		
		return (id != null);
	}
}
