package me.maple_bamboo_team.autoelytra.client;

import me.maple_bamboo_team.autoelytra.client.event.ElytraAutoReplaceHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class AutoelytraClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(ElytraAutoReplaceHandler::onClientTick);
    }
}