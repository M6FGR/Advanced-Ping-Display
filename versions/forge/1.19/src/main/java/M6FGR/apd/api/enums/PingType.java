package M6FGR.apd.api.enums;

public enum PingType {
    PACKET("Packet"),
    NORMAL("Normal");
    private final String name;
    PingType(String serializedName) {
        this.name = serializedName;
    }

    public String getName() {
        return this.name;
    }
}
