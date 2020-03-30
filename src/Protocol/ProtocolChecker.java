package Protocol;

public class ProtocolChecker {
    private final String MESSAGE_HEADER = "MESSAGE";
    private final String MESSAGE_FOOTER = "envoye";
    private final String LOGIN_HEADER = "LOGIN";

    public boolean isMessageHeader(String message) {
        return message.equals(MESSAGE_HEADER);
    }

    public boolean isMessageFooter(String message) {
        return message.equals(MESSAGE_FOOTER);
    }

    public boolean isMessage(String[] messageParts) {
        return isMessageHeader(messageParts[0]) && isMessageFooter(messageParts[messageParts.length - 1]);
    }

    public boolean isLoginHeader(String message) {
        return message.equals(LOGIN_HEADER);
    }
}
