public enum MessageType {
    // Explicit mapping between byte and enum
    //  → prevents breaking protocol when enum order changes.
    ECHO((byte) 1);

    private final byte code;

    MessageType(byte code) {
        this.code = code;
    }

    public byte getCode() {
        return code;
    }

    public static MessageType fromByte(byte b) {
        for (MessageType t : values()) {
            if (t.code == b) return t;
        }
        return null;
    }
}
