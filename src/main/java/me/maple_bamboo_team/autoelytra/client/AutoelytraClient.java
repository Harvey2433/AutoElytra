package me.maple_bamboo_team.autoelytra.client;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import me.maple_bamboo_team.autoelytra.client.config.AutoElytraConfig;
import me.maple_bamboo_team.autoelytra.client.event.ElytraAutoReplaceHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AutoelytraClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 初始化配置和处理程序
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
            ElytraAutoReplaceHandler.initialize();
        });

        ClientTickEvents.END_CLIENT_TICK.register(ElytraAutoReplaceHandler::onClientTick);

        // 注册客户端命令
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("autoelytra")
                    .then(ClientCommandManager.literal("autoexit")
                            .then(ClientCommandManager.argument("enabled", BoolArgumentType.bool())
                                    .executes(context -> {
                                        boolean enabled = BoolArgumentType.getBool(context, "enabled");
                                        AutoElytraConfig config = ElytraAutoReplaceHandler.getConfig();
                                        config.setAutoExitEnabled(enabled);

                                        if (enabled) {
                                            context.getSource().sendFeedback(Text.translatable("message.autoelytra.config.onautoexit"));
                                        } else {
                                            context.getSource().sendFeedback(Text.translatable("message.autoelytra.config.offautoexit"));
                                        }
                                        return 1;
                                    })
                            )
                            .executes(context -> {
                                AutoElytraConfig config = ElytraAutoReplaceHandler.getConfig();
                                boolean enabled = config.isAutoExitEnabled();
                                String status = enabled ? "§a" + Text.translatable("message.autoelytra.config.onautoexit").getString() :
                                        "§c" + Text.translatable("message.autoelytra.config.offautoexit").getString();
                                context.getSource().sendFeedback(Text.of(status));
                                return 1;
                            })
                    )
                            .then(ClientCommandManager.literal("autoexit_value")
                                    .then(ClientCommandManager.argument("value", IntegerArgumentType.integer(1, 1000))
                                            .executes(context -> {
                                                int value = IntegerArgumentType.getInteger(context, "value");
                                                AutoElytraConfig config = ElytraAutoReplaceHandler.getConfig();
                                                config.setMaxDurabilityThreshold(value);
                                                context.getSource().sendFeedback(
                                                        Text.translatable("message.autoelytra.config.setautoexitvalue").append(String.valueOf(value))
                                                );
                                                return 1;
                                            })
                                    )
                            )
                            .executes(context -> {
                                AutoElytraConfig config = ElytraAutoReplaceHandler.getConfig();
                                context.getSource().sendFeedback(
                                        Text.translatable("message.autoelytra.config.getautoexitvalue")
                                                .append(String.valueOf(config.getMaxDurabilityThreshold()))
                                );
                                return 1;
                            })
                    );
        });
    }
}