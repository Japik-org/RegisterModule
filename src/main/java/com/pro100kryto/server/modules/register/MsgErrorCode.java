package com.pro100kryto.server.modules.register;

public enum MsgErrorCode {
    Unknown,
    ServerInternal,
    NicknameExists,
    EmailExists,
    WrongNickname,
    WrongPassword,
    WrongEmail,
    WrongPacket
}
