package me.deftware.client.framework.wrappers.entity;

import me.deftware.mixin.imp.IMixinPlayerControllerMP;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class IPlayerController {

    public static void windowClick(int id, int next, IClickType type) {
        windowClick(0, id, next, type);
    }

    public static void windowClick(int windowID, int id, int next, IClickType type) {
        Minecraft.getMinecraft().playerController.windowClick(windowID, id, next,
                type.equals(IClickType.THROW) ? ClickType.THROW :
                        type.equals(IClickType.QUICK_MOVE) ? ClickType.QUICK_MOVE :
                                ClickType.PICKUP,
                Minecraft.getMinecraft().player);
    }

    /**
     * Looks for an item in the inventory and returns the slot id where the item is located,
     * returns -1 if the player does not have the item in their inventory
     */
    public static int getSlot(String name) {
        InventoryPlayer in = Minecraft.getMinecraft().player.inventory;
        for (int i = 0; i < in.mainInventory.size(); i++) {
            ItemStack it = in.mainInventory.get(i);
            if (it.getItem().getTranslationKey().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Swaps a stack in the same inventory
     */
    @SuppressWarnings("Duplicates")
    public static void swapStack(int one, int two, int windowId) {
        Minecraft.getMinecraft().playerController.windowClick(windowId, one, 0, ClickType.SWAP,
                Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().playerController.windowClick(windowId, two, 0, ClickType.SWAP,
                Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().playerController.updateController();
    }

    /**
     * Swaps a stack between one inventory to another
     */
    @SuppressWarnings("Duplicates")
    public static void swapStack(int srcInventoryId, int dstInventoryId, int srcSlot, int dstSlot){
        Minecraft.getMinecraft().playerController.windowClick(srcInventoryId, srcSlot, 0,
                ClickType.SWAP, Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().playerController.windowClick(dstInventoryId, dstSlot, 0,
                ClickType.SWAP, Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().playerController.updateController();
    }

    public static void moveStack(int srcInventoryId, int dstInventoryId, int srcSlot, int dstSlot){
        Minecraft.getMinecraft().playerController.windowClick(srcInventoryId, srcSlot, 0,
                ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
        Minecraft.getMinecraft().playerController.windowClick(dstInventoryId, dstSlot, 0,
                ClickType.QUICK_MOVE, Minecraft.getMinecraft().player);
    }

    public static void moveItem(int slotId) {
        Minecraft.getMinecraft().playerController.windowClick(0, slotId, 0, ClickType.QUICK_MOVE,
                Minecraft.getMinecraft().player);
    }

    public static void processRightClick(boolean offhand) {
        Minecraft.getMinecraft().playerController.processRightClick(Minecraft.getMinecraft().player, Minecraft.getMinecraft().world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
    }

    public static void resetBlockRemoving() {
        Minecraft.getMinecraft().playerController.resetBlockRemoving();
    }

    public static void setPlayerHittingBlock(boolean state) {
        ((IMixinPlayerControllerMP) Minecraft.getMinecraft().playerController).setPlayerHittingBlock(state);
    }

    public enum IClickType {
        THROW, QUICK_MOVE, PICKUP
    }

}
