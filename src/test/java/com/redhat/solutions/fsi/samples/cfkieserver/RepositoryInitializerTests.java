package com.redhat.solutions.fsi.samples.cfkieserver;

import com.redhat.solutions.fsi.samples.RepositoryInitializer;
import org.junit.Assert;
import org.junit.Test;
import java.io.InputStream;


public class RepositoryInitializerTests {

	private RepositoryInitializer repositoryInitializer = new RepositoryInitializer();

	@Test
	public void writeSettingsFile() {

		InputStream reader1 = ClassLoader.class.getResourceAsStream("/settings-template.xml");
		Assert.assertNotNull(reader1);

		repositoryInitializer.writeSettingsFile(
				"/settings-template.xml",
				"settings-test1.xml",
				"http://sample.repo.address/maven2/", "myuser", "mypass");

	}

}
