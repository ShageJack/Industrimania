package shagejack.shagecraft.gui.element;

import shagejack.shagecraft.client.render.HoloIcon;
import shagejack.shagecraft.container.slot.ShageSlot;
import shagejack.shagecraft.gui.ShageGuiBase;
import shagejack.shagecraft.util.ShageStringHelper;

import java.util.List;

public class ElementInventorySlot extends ElementSlot {
    ShageSlot slot;

    public ElementInventorySlot(ShageGuiBase gui, ShageSlot slot, int posX, int posY, int width, int height, String type, HoloIcon icon) {
        super(gui, posX, posY, width, height, type, icon);
        this.slot = slot;

    }

    public ElementInventorySlot(ShageGuiBase gui, ShageSlot slot, int posX, int posY, int width, int height, String type) {
        this(gui, slot, posX, posY, width, height, type, slot.getHoloIcon());
    }

    public ElementInventorySlot(ShageGuiBase gui, ShageSlot slot, int width, int height, String type, HoloIcon icon) {
        this(gui, slot, slot.xPos, slot.yPos, width, height, type, icon);
    }

    public ElementInventorySlot(ShageGuiBase gui, ShageSlot slot, int width, int height, String type) {
        this(gui, slot, slot.xPos, slot.yPos, width, height, type, slot.getHoloIcon());
    }

    @Override
    public void addTooltip(List<String> list, int mouseX, int mouseY) {
        if (slot.getUnlocalizedTooltip() != null && !slot.getUnlocalizedTooltip().isEmpty() && !slot.getHasStack()) {
            list.add(ShageStringHelper.translateToLocal(slot.getUnlocalizedTooltip()));
        }
    }

    @Override
    public void updateInfo() {
        boolean isVisible = isVisible() && (parent == null || parent.isVisible());

        if (!isVisible) {
            slot.xPos = Integer.MIN_VALUE + 10;
            slot.yPos = Integer.MIN_VALUE + 10;
        } else {
            slot.xPos = getGlobalX() + iconOffsetX;
            slot.yPos = getGlobalY() + iconOffsetY;
        }

        slot.setVisible(isVisible);
    }

    @Override
    protected boolean canDrawIcon(HoloIcon icon) {
        return !slot.getHasStack();
    }

    public ShageSlot getSlot() {
        return slot;
    }

    public void setSlot(ShageSlot slot) {
        this.slot = slot;
    }
}

