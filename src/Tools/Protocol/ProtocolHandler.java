package Tools.Protocol;

public class ProtocolHandler {
    public static final String LOGIN_HEADER = "LOGIN";
    public static final String MESSAGE_HEADER = "MESSAGE";
    public static final String MESSAGE_FOOTER = "envoye";
    public static final String ERROR_HEADER = "ERROR";
    public static final String ERROR_LOGIN = "ERROR LOGIN aborting chatamu protocol";
    public static final String ERROR_MESSAGE = "ERROR chatmau";
    public static final String SERVERCONNECT_HEADER = "SERVERCONNECT";


    public static boolean isLoginHeader(String message) {
        return message.equals(LOGIN_HEADER);
    }

    public static boolean isMessage(String[] messageParts) {
        return isMessageHeader(messageParts[0]) && isMessageFooter(messageParts[messageParts.length - 1]);
    }

    public static boolean isMessageHeader(String message) {
        return message.equals(MESSAGE_HEADER);
    }

    public static boolean isMessageFooter(String message) {
        return message.equals(MESSAGE_FOOTER);
    }

    public static boolean isError(String message) {
        return message.equals(ERROR_HEADER);
    }

    public static boolean isLoginError(String[] messageParts) {
        return isError(messageParts[0]) && isLoginHeader(messageParts[1]);

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

    public String addMessageHeaders(String message) {
        return "MESSAGE ".concat(message).concat(" envoye");
    }
}
