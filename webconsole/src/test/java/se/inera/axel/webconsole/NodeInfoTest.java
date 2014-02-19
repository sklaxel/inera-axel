package se.inera.axel.webconsole;

import org.testng.annotations.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class NodeInfoTest {
    @Test
    public void versionShouldBeRetrievedFromPomProperties() {
        NodeInfo nodeInfo = new NodeInfo();
        nodeInfo.setArtifactId("nodeInfoTestArtifactId");
        nodeInfo.setGroupId("se.inera.axel");

        String mavenVersion = nodeInfo.getMavenVersion();
        assertThat(mavenVersion, equalTo("1.0-NodeInfoTest"));
    }
}
