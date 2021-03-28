package com.pro100kryto.server.modules.register.packetprocess2;


import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.modules.register.MsgErrorCode;
import com.pro100kryto.server.modules.register.packet.PacketCreator;
import com.pro100kryto.server.services.usersdatabase.connection.IUsersDatabaseServiceConnection;
import com.pro100kryto.server.utils.datagram.packets.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

public class RegPacketProcess extends PacketProcess {


    public RegPacketProcess(IPacketProcessCallback callback, ILogger logger) {
        super(callback, logger);
    }

    @Override
    public void processPacket(final IPacket packet) throws Throwable {
        final IUsersDatabaseServiceConnection db = callback.getDatabase();
        final DataReader reader = packet.getDataReader();

        final String email = reader.readShortString();
        final String nickname = reader.readByteString();
        final String passHash = reader.readIntString();

        final IPacketInProcess newPacket = callback.getPacketPool().getNextPacket();
        newPacket.setEndPoint(new EndPoint(packet.getEndPoint()));
        final DataCreator creator = newPacket.getDataCreator();

        try {
            if (!isValidNickname(nickname)) {
                PacketCreator.msgError(creator, MsgErrorCode.WrongNickname);

            } else if (db.userExistsByKeyValue("nickname", nickname)) {
                PacketCreator.msgError(creator, MsgErrorCode.NicknameExists);

            } else if (!isValidEmail(email)) {
                PacketCreator.msgError(creator, MsgErrorCode.WrongEmail);

            } else if (db.userExistsByKeyValue("email", email)) {
                PacketCreator.msgError(creator, MsgErrorCode.EmailExists);

            } else if (!isValidPass(passHash)) {
                PacketCreator.msgError(creator, MsgErrorCode.WrongPassword);

            } else {
                final HashMap<String, Object> keyValues = new HashMap<>();
                keyValues.put("email", email);
                keyValues.put("email_verified", false);
                keyValues.put("nickname", nickname);
                keyValues.put("password", passHash);
                keyValues.put("password_date", LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE));
                final long userId = db.createUser(keyValues);
                PacketCreator.registerSuccess(creator, userId, nickname, email);
            }

        } catch (Throwable throwable){
            PacketCreator.msgError(creator, MsgErrorCode.ServerInternal);
        }

        callback.getSender().sendPacketAndRecycle(newPacket);
    }

    public boolean isValidNickname(String nickname){
        return nickname.length()>5;
    }

    public boolean isValidEmail(String email){
        final String[] emailSplit = email.split("@");
        return emailSplit.length==2 && !emailSplit[0].isEmpty() && !emailSplit[1].isEmpty();
    }

    public boolean isValidPass(String passHash){
        return !passHash.isEmpty();
    }
}
