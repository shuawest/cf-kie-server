package com.redhat.solutions.fsi.samples;

import org.appformer.maven.integration.MavenRepository;
import org.kie.server.services.impl.KieServerImpl;
import org.springframework.beans.factory.annotation.Value;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.InputStream;
import javax.annotation.PostConstruct;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.stereotype.Component;

@Component
public class RepositoryInitializer {

    private static final Log logger = LogFactory.getLog(RepositoryInitializer.class);

    private static final String REPO_LOCATION = "../.m2/repository/";
    private static final String SETTINGS_TEMPLATE_FILE = "/settings-template.xml";
    private static final String SETTINGS_OUTPUT_FILE = "../.m2/settings.xml";

    @Value("${cfkieserver.remoterepo.enabled:true}")
    private Boolean isRemoteRepoEnabled;

    @Value("${cfkieserver.remoterepo.url}")
    private String remoteRepoUrl;

    @Value("${cfkieserver.remoterepo.user}")
    private String repoteRepoUser;

    @Value("${cfkieserver.remoterepo.pass}")
    private String repoteRepoPass;


    @PostConstruct
    public void init() {
        if(!isRemoteRepoEnabled) {
            logger.info("KIE remote repository is disabled");
        } else {
            logger.info("Initializing KIE remote repository Settings");
            initializeDirectories();
            writeSettingsFile(SETTINGS_TEMPLATE_FILE,
                    SETTINGS_OUTPUT_FILE,
                    remoteRepoUrl, repoteRepoUser, repoteRepoPass);
        }

        //MavenRepository
        //KieServerImpl
    }

    public void initializeDirectories() {
        File m2RepoDir = new File( REPO_LOCATION );
        if(!m2RepoDir.exists())
            m2RepoDir.mkdirs();
    }

    public void writeSettingsFile(String templateFile, String outputFile, String url, String user, String pass) {
        try {
            InputStream settingsTemplateReader = this.getClass().getResourceAsStream(templateFile);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(settingsTemplateReader);

            xpathSetText(doc, "//repository/url", url);
            xpathSetText(doc, "//pluginRepository/url", url);
            xpathSetText(doc, "//servers/server/username", user);
            xpathSetText(doc, "//servers/server/password", pass);

            Transformer xformer = TransformerFactory.newInstance().newTransformer();
            xformer.transform(new DOMSource(doc), new StreamResult(new File(outputFile)));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void xpathSetText(Document doc, String expression, String content) throws XPathExpressionException {
        XPath xpath = XPathFactory.newInstance().newXPath();
        NodeList urls = (NodeList) xpath.evaluate(expression, doc, XPathConstants.NODESET);

        for (int idx = 0; idx < urls.getLength(); idx++) {
            urls.item(idx).setTextContent(content);
        }
    }
}
