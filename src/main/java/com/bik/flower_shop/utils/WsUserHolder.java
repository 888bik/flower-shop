package com.bik.flower_shop.utils;

import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bik
 */
public class WsUserHolder {

    // userId -> WebSocketSession
    public static final ConcurrentHashMap<Integer, WebSocketSession> USER = new ConcurrentHashMap<>();

    // serviceId -> WebSocketSession
    public static final ConcurrentHashMap<Integer, WebSocketSession> SERVICE = new ConcurrentHashMap<>();
}
