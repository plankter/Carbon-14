/*
 * Copyright (C) 2009 Anton Rau
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.carbon14.android;

public class Provider {
	public Provider() {

	}

	public Provider(String name, String description, String widgetUrl,
			String detailsUrl) {
		this.name = name;
		this.description = description;
		this.widgetUrl = widgetUrl;
		this.detailsUrl = detailsUrl;
	}

	private String name;

	public String getName() {
		return this.name;
	}

	public void setName(String value) {
		this.name = value;
	}

	private String description;

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String value) {
		this.description = value;
	}

	private String widgetUrl;

	public String getWidgetUrl() {
		return this.widgetUrl;
	}

	public void setWidgetUrl(String value) {
		this.widgetUrl = value;
	}

	private String detailsUrl;

	public String getDetailsUrl() {
		return this.detailsUrl;
	}

	public void setDetailsUrl(String value) {
		this.detailsUrl = value;
	}
}
