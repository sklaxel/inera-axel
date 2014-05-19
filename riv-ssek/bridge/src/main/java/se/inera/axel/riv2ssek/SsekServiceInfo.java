package se.inera.axel.riv2ssek;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class SsekServiceInfo implements Serializable {
    @Id
    String id;
    private String receiver;
    private String rivServiceNamespace;
    private String address;

    private SsekServiceInfo() {

    }

    private SsekServiceInfo(Builder builder) {
        this.receiver = builder.receiver;
        this.rivServiceNamespace = builder.rivServiceNamespace;
        this.address = builder.address;
    }

    public String getReceiver() {
        return receiver;
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
        return "SsekServiceInfo{" +
               "id='" + id + '\'' +
               ", receiver='" + receiver + '\'' +
               ", rivServiceNamespace='" + rivServiceNamespace + '\'' +
               ", address='" + address + '\'' +
               '}';
    }

    public static class Builder {
        private String receiver;
        private String rivServiceNamespace;
        private String address;

        public Builder receiver(String receiver) {
            this.receiver = receiver;

            return this;
        }

        public Builder rivServiceNamespace(String rivServiceNamespace) {
            this.rivServiceNamespace = rivServiceNamespace;

            return this;
        }

        public Builder address(String address) {
            this.address = address;

            return this;
        }

        public SsekServiceInfo build() {
            return new SsekServiceInfo(this);
        }
    }
}
