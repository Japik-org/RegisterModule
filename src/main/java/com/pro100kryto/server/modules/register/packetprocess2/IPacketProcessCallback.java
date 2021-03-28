package com.pro100kryto.server.modules.register.packetprocess2;

import com.pro100kryto.server.modules.packetpool.connection.IPacketPoolModuleConnection;
import com.pro100kryto.server.modules.sender.connection.ISenderModuleConnection;
import com.pro100kryto.server.services.usersdatabase.connection.IUsersDatabaseServiceConnection;
import org.jetbrains.annotations.Nullable;

public interface IPacketProcessCallback {
    @Nullable
    IUsersDatabaseServiceConnection getDatabase();
    @Nullable
    IPacketPoolModuleConnection getPacketPool();
    @Nullable
    ISenderModuleConnection getSender();
}
