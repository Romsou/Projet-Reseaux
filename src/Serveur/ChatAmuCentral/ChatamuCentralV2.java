package Serveur.ChatAmuCentral;

import Tools.Communication.IOCommunicator;
import Tools.Extended.SelectorExt;
import Tools.Extended.ServerSocketChannelExt;
import Tools.UserManagement.ClientQueueManager;
import Tools.UserManagement.Register;

public class ChatamuCentralV2 {
    public ServerSocketChannelExt serverSocketChannel;
    public SelectorExt selector;
    public Register register;
    public IOCommunicator communicator;
    public ClientQueueManager clientQueues;

    public ChatamuCentralV2(int port) {
        serverSocketChannel = new ServerSocketChannelExt();
        serverSocketChannel.bind(port);

        selector = new SelectorExt();
        register = new Register();
        communicator = new IOCommunicator();
        clientQueues = new ClientQueueManager();
    }


}
