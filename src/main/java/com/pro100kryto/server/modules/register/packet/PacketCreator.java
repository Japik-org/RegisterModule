package com.pro100kryto.server.modules.register.packet;

import com.pro100kryto.server.modules.register.MsgErrorCode;
import com.pro100kryto.server.utils.datagram.packets.DataCreator;

public final class PacketCreator {

    protected static void setHeader(DataCreator creator, short packetId){
        creator.setPosition(PacketHeaderInfo.Server.POS_PACKET_ID);
        creator.write(packetId);
        creator.setPosition(PacketHeaderInfo.Server.POS_BODY);
    }

    public static void msgError(DataCreator creator, MsgErrorCode msgError){
        setHeader(creator, PacketId.Server.MSG_ERROR);
        creator.write(msgError.ordinal());
    }

    public static void registerSuccess(DataCreator creator, long userId, String nickname, String email){
        setHeader(creator, PacketId.Server.REGISTER);
        creator.write(userId);
        creator.writeShortStrings(email);
        creator.writeByteStrings(nickname);
    }
}
