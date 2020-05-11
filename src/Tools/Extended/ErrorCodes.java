package Tools.Extended;

public enum ErrorCodes {
    OPEN_FAIL(10),
    CONFIG_FAIL(20),
    ACCEPT_FAIL(30),
    REGISTER_FAIL(40),
    SELECTION_FAIL(50),
    SENDING_FAIL(60),
    CLOSING_FAIL(100);

    private int code;

    ErrorCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

