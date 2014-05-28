package se.inera.axel.riv2ssek;

import org.springframework.data.annotation.Id;
import se.inera.axel.ssek.common.schema.ssek.IdType;

import java.io.Serializable;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class RivSsekServiceMapping implements Serializable {
    @Id
    String id;

    private IdType ssekReceiverType;
    private String ssekReceiver;
    private String rivServiceNamespace;
    private String rivLogicalAddress;
    private String address;
    private RivSsekServiceMapping() {

    }

    private RivSsekServiceMapping(Builder builder) {
        this.ssekReceiverType = builder.ssekReceiverType;
        this.ssekReceiver = builder.ssekReceiver;
        this.rivServiceNamespace = builder.rivServiceNamespace;
        this.rivLogicalAddress = builder.rivLogicalAddress;
        this.address = builder.address;
    }

    public IdType getSsekReceiverType() {
        return ssekReceiverType;
    }

    public String getSsekReceiver() {
        return ssekReceiver;
    }

    public String getRivServiceNamespace() {
        return rivServiceNamespace;
    }

    public String getId() {
        return this.id;
    }

    public String getAddress() {
        return this.address;
    }

    @Override
    public String toString() {
        return "RivSsekServiceMapping{" +
               "id='" + id + '\'' +
               ", ssekReceiverType=" + ssekReceiverType +
               ", ssekReceiver='" + ssekReceiver + '\'' +
               ", rivServiceNamespace='" + rivServiceNamespace + '\'' +
               ", address='" + address + '\'' +
               '}';
    }

    public static class Builder {
        private IdType ssekReceiverType;
        private String ssekReceiver;
        private String rivServiceNamespace;
        public String rivLogicalAddress;
        private String address;

        public Builder ssekReceiverType(IdType ssekReceiverType) {
            this.ssekReceiverType = ssekReceiverType;

            return this;
        }

        public Builder ssekReceiver(String ssekReceiver) {
            this.ssekReceiver = ssekReceiver;

            return this;
        }

        public Builder rivServiceNamespace(String rivServiceNamespace) {
            this.rivServiceNamespace = rivServiceNamespace;

            return this;
        }

        public Builder rivLogicalAddress(String rivLogicalAddress) {
            this.rivLogicalAddress = rivLogicalAddress;

            return this;
        }

        public Builder address(String address) {
            this.address = address;

            return this;
        }

        public RivSsekServiceMapping build() {
            return new RivSsekServiceMapping(this);
        }
    }
}
