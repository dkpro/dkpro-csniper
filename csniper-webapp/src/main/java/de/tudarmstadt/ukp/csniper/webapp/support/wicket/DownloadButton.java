/*******************************************************************************
 * Copyright 2013
 * Ubiquitous Knowledge Processing (UKP) Lab
 * Technische Universität Darmstadt
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tudarmstadt.ukp.csniper.webapp.support.wicket;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

public class DownloadButton
	extends Button
{
	private static final long serialVersionUID = 1L;
	private IModel<File> fileModel;
	private IModel<String> fileNameModel;
	private boolean deleteAfter;

	public DownloadButton(String aId, IModel<String> aButtonLabel, IModel<File> aFileModel,
			IModel<String> aFileName)
	{
		super(aId, aButtonLabel);
		fileModel = aFileModel;
		fileNameModel = aFileName;
		deleteAfter = false;
	}

	/**
	 * Copied from DownloadLink
	 */
	@Override
	public void onSubmit()
	{
		try {
			final File file = fileModel.getObject();
	
			String fileName = fileNameModel != null ? fileNameModel.getObject() : null;
			if (StringUtils.isEmpty(fileName)) {
				fileName = file.getName();
			}
	
			fileName = UrlEncoder.QUERY_INSTANCE.encode(fileName, getRequest().getCharset());
	
			IResourceStream resourceStream = new FileResourceStream(
					new org.apache.wicket.util.file.File(file));
			getRequestCycle().scheduleRequestHandlerAfterCurrent(
					new ResourceStreamRequestHandler(resourceStream)
					{
						@Override
						public void respond(IRequestCycle requestCycle)
						{
							super.respond(requestCycle);
	
							if (deleteAfter) {
								FileUtils.deleteQuietly(file);
							}
						}
					}.setFileName(fileName).setContentDisposition(ContentDisposition.ATTACHMENT));
		}
		catch (Exception e) {
			error("Unable to export: " + ExceptionUtils.getRootCauseMessage(e));
		}
	}

	public boolean getDeleteAfterDownload()
	{
		return deleteAfter;
	}
	
	public DownloadButton setDeleteAfterDownload(boolean doDeleteAfter)
	{
		deleteAfter = doDeleteAfter;
		return this;
	}
}
