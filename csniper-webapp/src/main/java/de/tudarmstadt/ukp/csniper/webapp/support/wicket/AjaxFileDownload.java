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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AbstractAjaxBehavior;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.handler.resource.ResourceStreamRequestHandler;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

public class AjaxFileDownload
	extends AbstractAjaxBehavior
{
	private static final long serialVersionUID = -2488536366826706596L;
	
	private File file;
	private String filename;
	private String contentType;

	/**
	 * Call this method to initiate the download.
	 */
	public void initiate(AjaxRequestTarget target, File aFile, String aFilename, String aContentType)
	{
		file = aFile;
		filename = aFilename;
		contentType = aContentType;

		String url = getCallbackUrl().toString();
		url = url + (url.contains("?") ? "&" : "?");
		url = url + "antiCache=" + System.currentTimeMillis();

		// the timeout is needed to let Wicket release the channel
		target.appendJavaScript("setTimeout(\"window.location.href='" + url + "'\", 100);");
	}

	@Override
	public void onRequest()
	{
		IResourceStream resourceStream = new FileResourceStream(file);
		getComponent().getRequestCycle().scheduleRequestHandlerAfterCurrent(
				new ResourceStreamRequestHandler(resourceStream)
				{
					@Override
					public void respond(IRequestCycle requestCycle)
					{
						((WebResponse) requestCycle.getResponse()).setContentType(contentType);
						super.respond(requestCycle);
						Files.remove(file);
					}
				}.setFileName(filename).setContentDisposition(ContentDisposition.ATTACHMENT));
	}
}
