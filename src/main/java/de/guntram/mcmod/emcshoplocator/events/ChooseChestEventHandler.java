package de.guntram.mcmod.emcshoplocator.events;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.NotAShopSignException;
import de.guntram.mcmod.emcshoplocator.ShopSign;
import java.util.HashSet;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ChooseChestEventHandler {
    private EMCShopLocator mod;
    long lastChooseSignClickTime;
    BlockPos lastChooseSignPos;
    Container playerOpenedContainer;
    private TileEntitySign lastChooseSign;
    
    public ChooseChestEventHandler(EMCShopLocator mod) {
        this.mod=mod;
        lastChooseSignClickTime=0;
        lastChooseSignPos=null;
        playerOpenedContainer=null;
    }
    
    @SideOnly(Side.CLIENT)    
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.LeftClickBlock event) {
        World world=event.getWorld();
        BlockPos pos=event.getPos();
        Block block=world.getBlockState(event.getPos()).getBlock();
        if (block==Blocks.WALL_SIGN || block==Blocks.STANDING_SIGN) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity==null || !(entity instanceof TileEntitySign))
                return;
            TileEntitySign sign=(TileEntitySign) entity;
            if (sign.signText!=null 
            &&  sign.signText[3]!=null 
            &&  sign.signText[3].getFormattedText().startsWith("[CHOOSE")) {
                // System.out.println("Choose sign clicked");
                lastChooseSignClickTime=System.currentTimeMillis();
                lastChooseSignPos=pos;
                lastChooseSign=sign;
            }
        }
    }
    
    private BlockPos getCurrentlyActiveChooseSign() {
        if (System.currentTimeMillis()< lastChooseSignClickTime+1000) {
            // System.out.println("returning choose sign at "+lastChooseSignPos.getX()+"/"+lastChooseSignPos.getY()+"/"+lastChooseSignPos.getZ());
            return lastChooseSignPos;
        } else {
            return null;
        }
    }
    
    @SideOnly(Side.CLIENT)    
    @SubscribeEvent
    public void onGuiOpen(GuiOpenEvent event) {
        BlockPos signPos;
        if (event!=null && event.getGui()!=null && event.getGui().getClass()==net.minecraft.client.gui.inventory.GuiChest.class
        &&  (signPos=getCurrentlyActiveChooseSign())!=null) {
            GuiChest chest=(GuiChest) event.getGui();
            playerOpenedContainer= chest.inventorySlots;
        }
    }
    
    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority=EventPriority.NORMAL, receiveCanceled=true)
    /* This is messy, but:
       - we want to remember the contents after the chest is closed, not when it's opened
       - there is no onGuiClose event
       - if there was a better way, then WDL would probably do it that way, but it doesn't.
    */
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (playerOpenedContainer != null
        && Minecraft.getMinecraft().player.openContainer != playerOpenedContainer) {
            try {
                HashSet<String> alreadySeen=new HashSet();
                for (int i=0; i<playerOpenedContainer.inventorySlots.size()-36; i++) {
                    Slot slot=playerOpenedContainer.inventorySlots.get(i);
                    ItemStack stack=slot.getStack();
                    if (!stack.isEmpty()) {
                        String displayName=stack.getDisplayName();
                        NBTTagList enchantments;

                        if (stack.getItem() instanceof ItemEnchantedBook) {
                            enchantments=((ItemEnchantedBook)(stack.getItem())).getEnchantments(stack);
                        } else {
                            enchantments=stack.getEnchantmentTagList();
                        }
                        if (enchantments != null)
                        {
                            StringBuilder enchants=new StringBuilder();
                            for (int t = 0; t < enchantments.tagCount(); ++t)
                            {
                                int j = enchantments.getCompoundTagAt(t).getShort("id");
                                int k = enchantments.getCompoundTagAt(t).getShort("lvl");

                                Enchantment enchant = Enchantment.getEnchantmentByID(j);
                                if (enchant != null)
                                {
                                    if (t>0)
                                        enchants.append(",");
                                    enchants.append(enchant.getTranslatedName(k).charAt(0));
                                    if (k>1)
                                        enchants.append(k);
                                }
                            }
                            if (enchants.length()>0)
                                displayName=displayName+(" ("+enchants+")");
                        }
                        // System.out.println("On close, slot "+i+ " had "+displayName);
                        if (!alreadySeen.contains(displayName)) {
                            mod.addSign(new ShopSign(lastChooseSign, mod.serverName, i, displayName));
                            alreadySeen.add(displayName);
                        }
                    }
                }
            } catch (NotAShopSignException ex) {
                System.err.println("Choose sign is not a shop sign: "+ex.getMessage());
            }
            playerOpenedContainer=null;
        }
    }
}
