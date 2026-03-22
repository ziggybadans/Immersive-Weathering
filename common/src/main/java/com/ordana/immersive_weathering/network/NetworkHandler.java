package com.ordana.immersive_weathering.network;

import net.mehvahdjukaar.moonlight.api.platform.network.Message;
import net.mehvahdjukaar.moonlight.api.platform.network.NetworkHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class NetworkHandler {

    public static final Channel CHANNEL = new Channel();


    public static void init() {
        NetworkHelper.addNetworkRegistration(
                event -> event.registerClientBound(SendCustomParticlesPacket.TYPE),
                1
        );
    }

    public static final class Channel {
        public void sendToAllClientPlayersInRange(ServerLevel level, BlockPos pos, double range, Message message) {
            NetworkHelper.sendToAllClientPlayersInRange(level, pos, range, message);
        }
    }

}
