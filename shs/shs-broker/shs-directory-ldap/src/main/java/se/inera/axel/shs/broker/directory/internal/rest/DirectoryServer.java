package se.inera.axel.shs.broker.directory.internal.rest;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
@XmlRootElement
public class DirectoryServer {
    private String name;

    public DirectoryServer() {

    }

    public DirectoryServer(String serverName) {
        this.name = serverName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
