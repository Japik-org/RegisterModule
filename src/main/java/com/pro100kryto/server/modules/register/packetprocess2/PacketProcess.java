package com.pro100kryto.server.modules.register.packetprocess2;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.modules.register.MsgErrorCode;
import com.pro100kryto.server.modules.register.packet.PacketCreator;
import com.pro100kryto.server.utils.datagram.packetprocess2.IPacketProcess;
import com.pro100kryto.server.utils.datagram.packets.IPacket;
import com.pro100kryto.server.utils.datagram.packets.IPacketInProcess;

public abstract class PacketProcess implements IPacketProcess {
    protected final IPacketProcessCallback callback;
    protected final ILogger logger;

    protected PacketProcess(IPacketProcessCallback callback, ILogger logger) {
        this.callback = callback;
        this.logger = logger;
    }

    @Override
    public final void run(IPacket packet) {
        try {
            processPacket(packet);
        } catch (Throwable throwable) {
            try {
                final IPacketInProcess newPacket = callback.getPacketPool().getNextPacket();
                try {
                    PacketCreator.msgError(newPacket.getDataCreator(), MsgErrorCode.WrongPacket);
                    callback.getSender().sendPacketAndRecycle(newPacket);
                    return;
                } catch (NullPointerException ignored){
                }
                newPacket.recycle();
            } catch (NullPointerException ignored){
            }
        }
    }

    public abstract void processPacket(IPacket packet) throws Throwable;
}
