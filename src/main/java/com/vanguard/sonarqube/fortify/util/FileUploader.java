package com.vanguard.sonarqube.fortify.util;

import java.io.File;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;


public class FileUploader {

	private static final Logger LOG = Loggers.get(FileUploader.class);

	public static void upload(final File file, final String url, String username, String password) {
		PostMethod filePost = new PostMethod(url);

		try {
			LOG.info("Uploading PDF to server...");

			Part[] parts = { new FilePart("upload", file) };

			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

			HttpClient client = new HttpClient();
			if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
				client.getParams().setAuthenticationPreemptive(true);
				Credentials credentials = new UsernamePasswordCredentials(username, password);
				client.getState().setCredentials(new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);
			}
			client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
			int status = client.executeMethod(filePost);
			if (status == HttpStatus.SC_OK) {
				LOG.info("PDF uploaded.");
			} else {
				LOG.error("Something went wrong storing the PDF at server side. Status: " + status);
			}
		} catch (Exception ex) {
			LOG.error("Something went wrong storing the PDF at server side", ex);
		} finally {
			filePost.releaseConnection();
		}
	}
}
