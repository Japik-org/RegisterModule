package com.pro100kryto.server.modules.register.packet;

public final class PacketId {
    public static final class Server{
        public static final short WRONG = 0;
        public static final short REGISTER = 1;
        public static final short MSG_ERROR = 2;
    }

    public static final class Client{
        public static final short WRONG = 0;
        public static final short REGISTER = 1;
    }
}
