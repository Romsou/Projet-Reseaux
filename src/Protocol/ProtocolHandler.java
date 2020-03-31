package Protocol;

public class ProtocolHandler {
    private static final String MESSAGE_HEADER = "MESSAGE";
    private static final String MESSAGE_FOOTER = "envoye";
    private static final String LOGIN_HEADER = "LOGIN";
    private static final String SERVERCONNECT_HEADER = "SERVERCONNECT";


    public static boolean isMessageHeader(String message) {
        return message.equals(MESSAGE_HEADER);
    }


    public static boolean isMessageFooter(String message) {
        return message.equals(MESSAGE_FOOTER);
    }


    public static boolean isMessage(String[] messageParts) {
        return isMessageHeader(messageParts[0]) && isMessageFooter(messageParts[messageParts.length - 1]);
    }


    public static boolean isLoginHeader(String message) {
        return message.equals(LOGIN_HEADER);
    }


    public static boolean isServerConnection(String message) {
        return message.equals(SERVERCONNECT_HEADER);
    }

    public String stripProtocolHeaders(String message) {
        String[] messageParts = message.split(" ");

        if (isLoginHeader(messageParts[0]))
            return message.substring("LOGIN".length());
        else if (isMessageHeader(messageParts[0]))
            return message.substring("MESSAGE".length(), message.length() - "envoye".length()).strip();
        else
            return message;
    }

}
