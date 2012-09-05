/**
 * 
 */
package org.jenkins.plugins.dbaudit.test.htmlunit.webpages;

import java.io.IOException;
import java.util.List;


import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

/**
 * @author Marco Scata
 *
 */
public class JenkinsConfigurationPage extends AbstractJenkinsPage {
	private final static String urlPath = "/configure";
	private HtmlForm configForm;

	public JenkinsConfigurationPage(final String baseUrl) {
		super(baseUrl, urlPath);
	}
	
	@Override
	public void load() {
		super.load();
		configForm = getPage().getFormByName("config");
	}
	
	public String getConfigValue(final String inputName) {
		String retval = null;
		final HtmlInput input = configForm.getInputByName(inputName);
		if (null == input) {
			throw new RuntimeException(
					String.format("Input '%s' cannot be found", inputName));
		} else {
			retval = input.getValueAttribute();
		}
		
		return retval;
	}
	
	public void setConfigValue(final String inputName, final String value) {
		final HtmlInput input = configForm.getInputByName(inputName);
		input.setValueAttribute(value);
	}
	
	public HtmlElement getJndiDatasourceRadioButton() {
		HtmlElement retval = null;
		final List<HtmlElement> elements = getPage().getElementsByName("dbaudit.datasource");
		for (final HtmlElement element : elements) {
			if (element.getAttribute("value").equalsIgnoreCase("true")) {
				retval = element;
				break;
			}
		}
		return retval;
	}
	
	public HtmlElement getJdbcDatasourceRadioButton() {
		HtmlElement retval = null;
		final List<HtmlElement> elements = getPage().getElementsByName("dbaudit.datasource");
		for (final HtmlElement element : elements) {
			if (element.getAttribute("value").equalsIgnoreCase("false")) {
				retval = element;
				break;
			}
		}
		return retval;
	}
	
	public boolean isUseJndi() {
		final HtmlElement element = getJndiDatasourceRadioButton();
		final String checked = element.getAttribute("checked");
		return ((checked != null) && (!checked.isEmpty()));
	}
	
	public void setUseJndi(final boolean useJndi) {
		try {
			if (useJndi) {
				getJndiDatasourceRadioButton().click();
			} else {
				getJdbcDatasourceRadioButton().click();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public String getJndiDatasource() {
		return getConfigValue("dbaudit.jndiName");
	}
	
	public void setJndiDatasource(final String datasourceName) {
		setConfigValue("dbaudit.jndiName", datasourceName);
	}

	
	public String getJndiUser() {
		return getConfigValue("dbaudit.jndiUser");
	}
	
	public void setJndiUser(final String user) {
		setConfigValue("dbaudit.jndiUser", user);
	}
	
	public String getJndiPassword() {
		return getConfigValue("dbaudit.jndiPassword");
	}
	
	public void setJndiPassword(final String password) {
		setConfigValue("dbaudit.jndiPassword", password);
	}

	public String getJdbcDriver() {
		return getConfigValue("dbaudit.jdbcDriver");
	}
	
	public void setJdbcDriver(final String driver) {
		setConfigValue("dbaudit.jdbcDriver", driver);
	}
	
	public String getJdbcUrl() {
		return getConfigValue("dbaudit.jdbcUrl");
	}
	
	public void setJdbcUrl(final String url) {
		setConfigValue("dbaudit.jdbcUrl", url);
	}
	
	public String getJdbcUser() {
		return getConfigValue("dbaudit.jdbcUser");
	}
	
	public void setJdbcUser(final String user) {
		setConfigValue("dbaudit.jdbcUser", user);
	}
	
	public String getJdbcPassword() {
		return getConfigValue("dbaudit.jdbcPassword");
	}
	
	public void setJdbcPassword(final String password) {
		setConfigValue("dbaudit.jdbcPassword", password);
	}
	
	public HtmlElement getSaveButton() {
		final List<HtmlElement> buttons = configForm.getElementsByTagName("button");
		HtmlElement saveButton = null;
		// find the save button (it has no predictable id)
		for (final HtmlElement button : buttons) {
			if (button.getTextContent().trim().equalsIgnoreCase("save")) {
				saveButton = button;
				break;
			}
		}
		return saveButton;
	}
	
	public void saveChanges() {
		final HtmlElement saveButton = getSaveButton();
		
		if (null == saveButton) {
			throw new RuntimeException("Save button not found on config form!");
		}
		
		try {
			saveButton.click();
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}
}