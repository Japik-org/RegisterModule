package com.pro100kryto.server.modules;

import com.pro100kryto.server.module.IModuleConnectionSafe;
import com.pro100kryto.server.module.Module;
import com.pro100kryto.server.modules.packetpool.connection.IPacketPoolModuleConnection;
import com.pro100kryto.server.modules.receiverbuffered.connection.IReceiverBufferedModuleConnection;
import com.pro100kryto.server.modules.register.RegisterProcess;
import com.pro100kryto.server.modules.register.packet.PacketId;
import com.pro100kryto.server.modules.register.packetprocess2.IPacketProcessCallback;
import com.pro100kryto.server.modules.register.packetprocess2.RegPacketProcess;
import com.pro100kryto.server.modules.register.packetprocess2.WrongPacketPacketProcess;
import com.pro100kryto.server.modules.sender.connection.ISenderModuleConnection;
import com.pro100kryto.server.service.IServiceConnectionSafe;
import com.pro100kryto.server.service.IServiceControl;
import com.pro100kryto.server.services.usersdatabase.connection.IUsersDatabaseServiceConnection;
import com.pro100kryto.server.utils.datagram.objectpool.ObjectPool;
import com.pro100kryto.server.utils.datagram.packetprocess2.IPacketProcess;
import com.pro100kryto.server.utils.datagram.packetprocess2.ProcessorThreadPool;
import com.pro100kryto.server.utils.datagram.packets.IPacket;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class RegisterModule extends Module implements IPacketProcessCallback {

    private IModuleConnectionSafe<IReceiverBufferedModuleConnection> receiverModuleConnection;
    private IModuleConnectionSafe<IPacketPoolModuleConnection> packetPoolModuleConnection;
    private IModuleConnectionSafe<ISenderModuleConnection> senderModuleConnection;
    private IServiceConnectionSafe<IUsersDatabaseServiceConnection> usersDbServiceConnection;

    private ProcessorThreadPool<RegisterProcess> processor;
    private ObjectPool<RegisterProcess> processesPool;
    private IntObjectHashMap<IPacketProcess> packetIdPacketProcessMap;


    public RegisterModule(IServiceControl service, String name) {
        super(service, name);
    }

    @Override
    protected void startAction() throws Throwable {

        packetPoolModuleConnection = initModuleConnection(settings.getOrDefault("packetpool-module-name", "packetPool"));
        senderModuleConnection = initModuleConnection(settings.getOrDefault("sender-module-name", "sender"));
        receiverModuleConnection = initModuleConnection(settings.getOrDefault("receiver-module-name", "receiver"));
        usersDbServiceConnection = initServiceConnection(settings.getOrDefault("usersDb-service-name", "usersDb"));


        packetIdPacketProcessMap = new IntObjectHashMap<>();
        packetIdPacketProcessMap.put(PacketId.Client.REGISTER, new RegPacketProcess(this, logger));
        packetIdPacketProcessMap.put(PacketId.Client.WRONG, new WrongPacketPacketProcess());

        final int maxProcesses = Integer.parseInt(settings.getOrDefault("max-processes", "256"));
        processesPool = new ObjectPool<RegisterProcess>(maxProcesses) {
            @Override
            protected RegisterProcess createRecycledObject() {
                return new RegisterProcess(processesPool, packetIdPacketProcessMap);
            }
        };
        processesPool.refill();
        processor = new ProcessorThreadPool<>(maxProcesses);
    }

    @Override
    protected void stopAction(boolean force) throws Throwable {
        packetIdPacketProcessMap.clear();
        processesPool.clear();
    }

    @Override
    public void tick() throws Throwable {
        try {
            final IPacket packet = receiverModuleConnection.getModuleConnection().getNextPacket();
            Objects.requireNonNull(packet);

            try {
                final RegisterProcess process = processesPool.nextAndGet();
                process.setPacket(packet);
                processor.startOrRecycle(process);

            } catch (final Throwable throwable){
                logger.writeException(throwable, "Failed process packet");
            }
        } catch (final NullPointerException ignored){
        }
    }

    // ---------- callback


    @Nullable
    @Override
    public IUsersDatabaseServiceConnection getDatabase() {
        return usersDbServiceConnection.getServiceConnection();
    }

    @Nullable
    @Override
    public IPacketPoolModuleConnection getPacketPool() {
        return packetPoolModuleConnection.getModuleConnection();
    }

    @Nullable
    @Override
    public ISenderModuleConnection getSender() {
        return senderModuleConnection.getModuleConnection();
    }
}
