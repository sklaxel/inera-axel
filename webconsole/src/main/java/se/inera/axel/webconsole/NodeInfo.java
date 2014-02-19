/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.webconsole;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class NodeInfo {
    private String nodeId;

    private String organizationNumber;

    private String externalReceiveServiceUrl;

    private String groupId;

    private String artifactId;

    private static String POM_PROPERTIES_PATH = "/META-INF/maven/%1$s/%2$s/pom.properties";

    public String getOrganizationNumber() {
        return organizationNumber;
    }

    public void setOrganizationNumber(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getExternalReceiveServiceUrl() {
        return externalReceiveServiceUrl;
    }

    public void setExternalReceiveServiceUrl(String externalReceiveServiceUrl) {
        this.externalReceiveServiceUrl = externalReceiveServiceUrl;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getMavenVersion() {
        Properties properties = loadProperties(getClass().getResourceAsStream(String.format(POM_PROPERTIES_PATH, groupId, artifactId)));
        if (properties == null) {
            return "unknown";
        } else {
            return properties.getProperty("version");
        }
    }

    private Properties loadProperties(InputStream inputStream) {
        if (inputStream != null) {
            Properties properties = new Properties();
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                return null;
            }
            return properties;
        }
        return null;
    }


}
