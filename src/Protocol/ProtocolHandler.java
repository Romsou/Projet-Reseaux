package Protocol;

public class ProtocolHandler {
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


    public String stripProtocolHeaders(String message) {
        String[] messageParts = message.split(" ");

        if (isLoginHeader(messageParts[0]))
            return message.substring("LOGIN".length());
        else if (isMessageHeader(messageParts[0]))
            return message.substring("MESSAGE".length(), message.length() - "envoye".length()).strip();
        else
            return null;
    }

}
