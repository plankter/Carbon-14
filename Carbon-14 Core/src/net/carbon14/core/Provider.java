package net.carbon14.core;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Provider {
	public Provider(String name, String description, String widgetUrl, String detailsUrl)
	{
		this.name = name;
		this.description = description;
		this.widgetUrl = widgetUrl;
		this.detailsUrl = detailsUrl;
	}
	
	@PrimaryKey
	@Persistent
	private String name;
	public String getName() {
	  return this.name;
	}
	public void setName(String value) {
	  this.name = value;
	}
	
	@Persistent
	private String description;
	public String getDescription() {
	  return this.description;
	}
	public void setDescription(String value) {
	  this.description = value;
	}
	
	@Persistent
	private String widgetUrl;
	public String getWidgetUrl() {
	  return this.widgetUrl;
	}
	public void setWidgetUrl(String value) {
	  this.widgetUrl = value;
	}
	
	@Persistent
	private String detailsUrl;
	public String getDetailsUrl() {
	  return this.detailsUrl;
	}
	public void setDetailsUrl(String value) {
	  this.detailsUrl = value;
	}
}
