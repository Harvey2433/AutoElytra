package me.maple_bamboo_team.autoelytra.client.event;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ElytraAutoReplaceHandler {

    private static final int DURABILITY_THRESHOLD = 5; // 耐久度阈值
    private static int cooldown = 0;
    private static int alertCooldown = 0;
    private static int alertSoundCounter = 0;
    private static int alertDelay = 0; // 警报延迟计时器
    private static boolean hasTriggeredAlert = false; // 标记是否已触发过警报
    private static boolean isAlertActive = false; // 标记警报是否处于活动状态
    private static boolean titleCleared = false; // 标记标题是否已清除
    private static final int ALERT_DURATION = 100; // 5秒 (20 ticks/秒 * 5)
    private static final int ALERT_SOUND_INTERVAL = 5; // 每5tick播放一次声音
    private static final int ALERT_DELAY = 20; // 1秒延迟 (20 ticks)

    public static void onClientTick(MinecraftClient client) {
        if (client.player == null) {
            return;
        }

        ClientPlayerEntity player = client.player;

        if (cooldown > 0) {
            cooldown--;
        }

        // 处理警报效果
        if (isAlertActive && alertCooldown > 0) {
            alertCooldown--;

            // 播放连续急促的经验声音
            if (alertCooldown > 0 && alertSoundCounter <= 0) {
                player.playSound(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.5F);
                alertSoundCounter = ALERT_SOUND_INTERVAL;
            }

            if (alertSoundCounter > 0) {
                alertSoundCounter--;
            }

            // 警报结束时清除标题（只执行一次）
            if (alertCooldown == 0 && !titleCleared) {
                clearTitle(player);
                titleCleared = true;
                isAlertActive = false;
            }
        }

        if (client.interactionManager == null) {
            return;
        }

        ItemStack currentElytra = player.getEquippedStack(EquipmentSlot.CHEST);

        // 检查当前胸甲是否是鞘翅且耐久低
        if (currentElytra.getItem() == Items.ELYTRA && isLowDurability(currentElytra)) {
            // 查找背包中的新鞘翅
            int newElytraSlot = findBestElytraSlotInInventory(player);

            if (newElytraSlot != -1) {
                // 重置警报状态，因为找到了可替换的鞘翅
                resetAlertState(player);

                replaceElytra(client, player, newElytraSlot);
                cooldown = 20; // 1秒冷却
            } else if (!hasTriggeredAlert) {
                // 延迟触发警报
                if (alertDelay <= 0) {
                    // 设置延迟计时器
                    alertDelay = ALERT_DELAY;
                } else {
                    alertDelay--;

                    // 延迟结束后触发警报
                    if (alertDelay <= 0) {
                        triggerFailureAlert(player);
                    }
                }
            }
        } else {
            // 如果玩家身上的鞘翅不再低于临界值，重置警报状态
            resetAlertState(player);
        }
    }

    private static void resetAlertState(ClientPlayerEntity player) {
        alertDelay = 0;
        if (isAlertActive) {
            // 只在警报活动时清除标题
            clearTitle(player);
        }
        alertCooldown = 0;
        isAlertActive = false;
        hasTriggeredAlert = false;
        titleCleared = false;
    }

    private static void clearTitle(ClientPlayerEntity player) {
        if (player != null) {
            player.sendMessage(Text.empty(), true);
            System.out.println("[AutoElytra] Title cleared");
        }
    }

    private static boolean isLowDurability(ItemStack elytra) {
        return elytra.getMaxDamage() - elytra.getDamage() <= DURABILITY_THRESHOLD;
    }

    private static int findBestElytraSlotInInventory(ClientPlayerEntity player) {
        List<ElytraCandidate> enchantedCandidates = new ArrayList<>();
        List<ElytraCandidate> normalCandidates = new ArrayList<>();

        // 收集所有符合条件的鞘翅
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == Items.ELYTRA && !isLowDurability(stack)) {
                boolean hasEnchantments = !EnchantmentHelper.get(stack).isEmpty();
                int durability = stack.getMaxDamage() - stack.getDamage();

                ElytraCandidate candidate = new ElytraCandidate(i, durability, hasEnchantments);

                if (hasEnchantments) {
                    enchantedCandidates.add(candidate);
                } else {
                    normalCandidates.add(candidate);
                }
            }
        }

        // 优先选择有附魔的鞘翅，按耐久度降序排序
        if (!enchantedCandidates.isEmpty()) {
            enchantedCandidates.sort(Comparator.comparing(ElytraCandidate::getDurability).reversed());
            System.out.println("[AutoElytra] Found " + enchantedCandidates.size() + " enchanted elytra candidates");
            return enchantedCandidates.get(0).getSlot();
        }

        // 如果没有附魔鞘翅，选择耐久度最高的普通鞘翅
        if (!normalCandidates.isEmpty()) {
            normalCandidates.sort(Comparator.comparing(ElytraCandidate::getDurability).reversed());
            System.out.println("[AutoElytra] Found " + normalCandidates.size() + " normal elytra candidates");
            return normalCandidates.get(0).getSlot();
        }

        System.out.println("[AutoElytra] No suitable elytra found in inventory");
        return -1;
    }

    private static void replaceElytra(MinecraftClient client, ClientPlayerEntity player, int newElytraSlot) {
        ClientPlayerInteractionManager interactionManager = client.interactionManager;
        if (interactionManager == null) {
            System.out.println("[AutoElytra] Interaction manager is null");
            return;
        }

        // 计算网络槽位ID
        int sourceSlot = convertToNetworkSlot(newElytraSlot);
        int armorSlot = 6; // 胸甲槽位在网络包中的ID

        System.out.println("[AutoElytra] Attempting to replace elytra from slot " + newElytraSlot + " (network slot: " + sourceSlot + ")");

        // 使用PICKUP和QUICK_MOVE来模拟拖放操作
        try {
            // 首先点击新鞘翅槽位（拾取）
            interactionManager.clickSlot(
                    player.playerScreenHandler.syncId,
                    sourceSlot,
                    0, // button
                    SlotActionType.PICKUP,
                    player
            );

            // 然后点击胸甲槽位（放置）
            interactionManager.clickSlot(
                    player.playerScreenHandler.syncId,
                    armorSlot,
                    0, // button
                    SlotActionType.PICKUP,
                    player
            );

            // 如果之前胸甲槽位有物品，现在它会在光标上，需要放回源槽位
            if (!player.playerScreenHandler.getCursorStack().isEmpty()) {
                interactionManager.clickSlot(
                        player.playerScreenHandler.syncId,
                        sourceSlot,
                        0, // button
                        SlotActionType.PICKUP,
                        player
                );
            }

            // 替换成功，在聊天栏发送本地化消息
            player.sendMessage(Text.translatable("message.autoelytra.replace_success").formatted(Formatting.GREEN));
            System.out.println("[AutoElytra] Elytra replacement successful");

        } catch (Exception e) {
            System.out.println("[AutoElytra] Error during elytra replacement: " + e.getMessage());

            // 发生错误时重置光标
            if (!player.playerScreenHandler.getCursorStack().isEmpty()) {
                try {
                    interactionManager.clickSlot(
                            player.playerScreenHandler.syncId,
                            -999, // 点击屏幕外区域丢弃或取消
                            0,
                            SlotActionType.PICKUP,
                            player
                    );
                    System.out.println("[AutoElytra] Cursor reset after error");
                } catch (Exception ex) {
                    System.out.println("[AutoElytra] Failed to reset cursor: " + ex.getMessage());
                }
            }
        }
    }

    private static void triggerFailureAlert(ClientPlayerEntity player) {
        // 设置警报状态
        alertCooldown = ALERT_DURATION;
        alertSoundCounter = 0; // 立即播放第一次声音
        hasTriggeredAlert = true; // 标记已触发警报
        isAlertActive = true; // 标记警报处于活动状态
        titleCleared = false; // 重置标题清除状态

        // 显示大标题（红色）使用本地化文本
        player.sendMessage(Text.translatable("message.autoelytra.replace_failure").formatted(Formatting.RED), true);
        System.out.println("[AutoElytra] Triggered failure alert - will last for " + (ALERT_DURATION / 20) + " seconds");
    }

    private static int convertToNetworkSlot(int inventorySlot) {
        if (inventorySlot >= 0 && inventorySlot < 9) {
            // 快捷栏: 36-44
            return inventorySlot + 36;
        } else if (inventorySlot >= 9 && inventorySlot < 36) {
            // 主背包: 9-35
            return inventorySlot;
        } else if (inventorySlot >= 36 && inventorySlot < 40) {
            // 盔甲栏: 5-8 (靴子、护腿、胸甲、头盔)
            return 8 - (inventorySlot - 36);
        }
        return inventorySlot;
    }

    // 辅助类，用于存储鞘翅候选信息
    private static class ElytraCandidate {
        private final int slot;
        private final int durability;
        private final boolean hasEnchantments;

        public ElytraCandidate(int slot, int durability, boolean hasEnchantments) {
            this.slot = slot;
            this.durability = durability;
            this.hasEnchantments = hasEnchantments;
        }

        public int getSlot() {
            return slot;
        }

        public int getDurability() {
            return durability;
        }

        public boolean hasEnchantments() {
            return hasEnchantments;
        }
    }
}