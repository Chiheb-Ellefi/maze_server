package org.algorithm.game_server.server;

import java.io.IOException;

public interface Server {
    String createSession(String firstIp,String secondIp);
    void handleGame() throws IOException;
}
