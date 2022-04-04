package gyurix.shopsystem.gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

public class GUIListener implements Listener {
    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Inventory inv = e.getClickedInventory();
        Inventory top = e.getView().getTopInventory();
        if (top.getHolder() instanceof CustomGUI) {
            e.setCancelled(true);
            if (inv == top)
                ((CustomGUI) top.getHolder()).onClick(e.getSlot(), e.isRightClick(), e.isShiftClick());
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top.getHolder() instanceof CustomGUI)
            e.setCancelled(true);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (top.getHolder() instanceof CustomGUI)
            ((CustomGUI) top.getHolder()).onClose();
    }
}
