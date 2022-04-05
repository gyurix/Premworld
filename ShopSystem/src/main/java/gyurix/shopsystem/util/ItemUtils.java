package gyurix.shopsystem.util;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static gyurix.shopsystem.ShopSystem.pl;

public class ItemUtils {

    public static ItemStack addLore(ItemStack is, String... lore) {
        is = is.clone();
        ItemMeta meta = is.getItemMeta();
        List<String> l = meta.getLore();
        if (l == null)
            l = new ArrayList<>();
        Collections.addAll(l, lore);
        meta.setLore(l);
        is.setItemMeta(meta);
        return is;
    }

    public static ItemStack addLore(ItemStack is, List<String> lore, Object... vars) {
        is = is.clone();
        ItemMeta meta = is.getItemMeta();
        List<String> l = meta.getLore();
        if (l == null)
            l = new ArrayList<>();
        for (String s : lore) {
            l.add(StrUtils.fillVariables(s, vars));
        }
        meta.setLore(l);
        is.setItemMeta(meta);
        return is;
    }

    public static int countItem(Player plr, ItemStack item) {
        int count = 0;
        PlayerInventory inv = plr.getInventory();
        String dn = item.getItemMeta().getDisplayName();
        for (int i = 0; i < 36; ++i) {
            ItemStack is = inv.getItem(i);
            if (is != null && dn.equals(is.getItemMeta().getDisplayName()))
                count += is.getAmount();
        }
        return count;
    }

    public static int countItemSpace(Player plr, ItemStack item) {
        int count = 0;
        int ss = item.getMaxStackSize();
        PlayerInventory inv = plr.getInventory();
        String dn = item.getItemMeta().getDisplayName();
        for (int i = 0; i < 36; ++i) {
            ItemStack is = inv.getItem(i);
            if (is == null || is.getType() == Material.AIR)
                count += ss;
            else if (dn.equals(is.getItemMeta().getDisplayName()))
                count += ss - is.getAmount();
        }
        return count;
    }

    public static ItemStack fillVariables(ItemStack is, Object... vars) {
        if (is == null)
            return null;
        is = is.clone();
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(StrUtils.fillVariables(meta.getDisplayName(), vars));
        meta.setLore(StrUtils.fillVariables(meta.getLore(), vars));
        is.setItemMeta(meta);
        return is;
    }

    public static Material getMaterial(String s) {
        try {
            return Material.valueOf(s.toUpperCase());
        } catch (Throwable err) {
            logError("Material " + s + " does not exist");
            return Material.STONE;
        }
    }

    public static String itemToString(ItemStack is) {
        if (is == null || is.getType() == Material.AIR)
            return "AIR";
        StringBuilder sb = new StringBuilder();
        sb.append(is.getType());
        if (is.getDurability() != 0)
            sb.append(':').append(is.getDurability());
        if (is.getAmount() != 1)
            sb.append(' ').append(is.getAmount());
        if (!is.hasItemMeta())
            return sb.toString();
        ItemMeta meta = is.getItemMeta();
        if (meta.hasDisplayName())
            sb.append(" name:").append(meta.getDisplayName().replace(" ", "_"));
        if (meta.hasLore())
            sb.append(" lore:").append(StringUtils.join(meta.getLore(), "|").replace(" ", "_"));
        meta.getItemFlags().forEach(flag -> sb.append(" hide:").append(flag.name().substring(5)));

        if (meta instanceof SkullMeta && ((SkullMeta) meta).getOwner() != null)
            sb.append(" owner:").append(((SkullMeta) meta).getOwner());

        if (meta instanceof LeatherArmorMeta) {
            ((LeatherArmorMeta) meta).getColor();
            sb.append(" color:").append(((LeatherArmorMeta) meta).getColor().asRGB());
        }

        if (meta instanceof PotionMeta && ((PotionMeta) meta).hasCustomEffects())
            ((PotionMeta) meta).getCustomEffects().forEach(pe ->
                sb.append(' ').append(pe.getType()).append(':').append(pe.getAmplifier()).append(':').append(pe.getDuration()));

        meta.getEnchants().forEach((e, lvl) -> sb.append(' ').append(e.getName()).append(':').append(lvl));
        return sb.toString();
    }

    private static void logError(String s) {
        Bukkit.getConsoleSender().sendMessage("§e[" + pl.getName() + "]§f Error, " + s);
    }

    public static ItemStack stringToItemStack(String s) {
        s = s.replaceAll("&([0-9a-fk-or])", "§$1");
        String[] d = s.split(" ");
        String[] id = d[0].split(":", 2);
        boolean hasAmount = (d.length > 1 && d[1].matches("\\d+"));
        int amount = hasAmount ? Integer.parseInt(d[1]) : 1;
        ItemStack is = new ItemStack(getMaterial(id[0]), amount, id.length == 2 ? Short.parseShort(id[1]) : 0);
        ItemMeta meta = is.getItemMeta();
        if (meta == null)
            return is;
        for (int i = hasAmount ? 2 : 1; i < d.length; ++i) {
            String m = d[i];
            try {
                String[] parts = m.split(":",2);
                switch (parts[0]) {
                    case "addattribute" -> meta.addAttributeModifier(Attribute.valueOf(parts[1].toUpperCase()), new AttributeModifier(UUID.randomUUID(),
                        UUID.randomUUID().toString().substring(16, 32), Double.parseDouble(parts[3]), AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.valueOf(parts[2].toUpperCase())));
                    case "name" -> meta.setDisplayName(parts[1].replace("_", " "));
                    case "lore" -> meta.setLore(Lists.newArrayList(parts[1].replace("_", " ").split("\\|")));
                    case "hide" -> meta.addItemFlags(ItemFlag.valueOf("HIDE_" + parts[1].toUpperCase()));
                    case "owner" -> ((SkullMeta) meta).setOwner(parts[1]);
                    case "color" -> ((LeatherArmorMeta) meta).setColor(Color.fromRGB(Integer.parseInt(parts[1])));
                    default -> {
                        if (meta instanceof PotionMeta) {
                            PotionEffectType type = PotionEffectType.getByName(parts[0]);
                            if (type != null) {
                                ((PotionMeta) meta).addCustomEffect(new PotionEffect(type, Integer.parseInt(parts[2]), Integer.parseInt(parts[1]), false, false), true);
                                break;
                            }
                        }
                        Enchantment enchantment = Enchantment.getByName(parts[0].toUpperCase());
                        if (enchantment != null) {
                            meta.addEnchant(enchantment, Integer.parseInt(parts[1]), true);
                            break;
                        }
                        logError("Invalid meta tag: " + m);
                    }
                }
            } catch (Throwable ignored) {
                logError("Invalid meta tag: " + m);
            }
        }
        is.setItemMeta(meta);
        return is;
    }
}
