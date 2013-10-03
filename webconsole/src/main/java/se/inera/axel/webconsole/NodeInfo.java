package se.inera.axel.webconsole;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class NodeInfo {
    private String nodeId;

    private String organizationNumber;

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
}