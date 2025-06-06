package com.example.kafkaredis.model;

import com.example.kafkaredis.events.v1.MessageStatus;

public enum StatusEnum {
    UNKNOWN(MessageStatus.UNKNOWN),
    RECEIVED(MessageStatus.RECEIVED),
    PROCESSING(MessageStatus.PROCESSING),
    PROCESSED(MessageStatus.PROCESSED),
    FAILED(MessageStatus.FAILED),
    EXPIRED(MessageStatus.EXPIRED);

    private final MessageStatus protoStatus;

    StatusEnum(MessageStatus protoStatus) {
        this.protoStatus = protoStatus;
    }

    public MessageStatus toProto() {
        return protoStatus;
    }

    public static StatusEnum fromProto(MessageStatus protoStatus) {
        for (StatusEnum status : values()) {
            if (status.protoStatus == protoStatus) {
                return status;
            }
        }
        return UNKNOWN;
    }
} 