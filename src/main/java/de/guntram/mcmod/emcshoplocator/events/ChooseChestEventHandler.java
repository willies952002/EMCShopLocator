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
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
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
        if (event!=null && event.getGui()!=null
        && event.getGui().getClass()==net.minecraft.client.gui.inventory.GuiChest.class
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

            long now=System.currentTimeMillis();
            for (ShopSign shopsign:mod.getSigns()) {
                if (shopsign.getChoosePosition()!=-1
                &&  shopsign.getPos().getX() == lastChooseSignPos.getX()
                &&  shopsign.getPos().getZ() == lastChooseSignPos.getZ()
                &&  shopsign.getServer().equals(mod.serverName)) {
                    shopsign.markForDeletion();
                }
            }
            // System.out.println("marking for deletion took "+(System.currentTimeMillis()-now)+ "ms");
            
            HashSet<String> alreadySeen=new HashSet();
            for (int i=0; i<playerOpenedContainer.inventorySlots.size()-36; i++) {
                try {
                    Slot slot=playerOpenedContainer.inventorySlots.get(i);
                    ItemStack stack=slot.getStack();
                    if (!stack.isEmpty()) {

                        String defaultName=stack.getItem().getItemStackDisplayName(stack);
                        String displayName=stack.getDisplayName();
                        String usedName=defaultName;
                        if (displayName.startsWith("§")) {
                            // this is for promo and other special EMC items
                            usedName=displayName;
                        }
                        if (stack.getItem()==Items.SKULL && stack.getItemDamage()==3) {
                            // handle deco heads
                            if (defaultName.startsWith("MHF_"))     // mob heads like MHF_Slime's Head
                                usedName=displayName;
                            else if (defaultName.equals(displayName))
                                ;
                            else
                                usedName="Deco Head - "+displayName;
                        }
                        if (stack.getItem()==Items.SPAWN_EGG) {
                            // Horse, Mule, Donkey, Llama eggs
                            String extra=parseSpawnEggLore(stack);
                            if (extra!=null && !extra.isEmpty())
                                usedName+=" ("+extra+")";
                        }
                        NBTTagList enchantments;

                        // handle enchanted items and books

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
                                usedName=usedName+(" ("+enchants+")");
                        }
                        // System.out.println("On close, slot "+i+ " had "+displayName);
                        if (!alreadySeen.contains(usedName)) {
                            // System.out.println("using category name: "+usedName);
                            mod.addSign(new ShopSign(lastChooseSign, mod.serverName, i, usedName));
                            alreadySeen.add(usedName);
                        }
                    }
                } catch (NotAShopSignException ex) {
                    System.err.println("Choose sign is not a shop sign: "+ex.getMessage());
                }
            }
            playerOpenedContainer=null;
        }
    }
    
    private String parseSpawnEggLore(ItemStack stack) {
        int strength=0, speed=0, jump=0, hitpoints=0;
        String type=null;
        NBTTagCompound compound = stack.getTagCompound();
        NBTTagCompound compoundDisplay=compound==null ? null : compound.getCompoundTag("display");
        if (compoundDisplay!=null && compoundDisplay.getTagId("Lore")==9) {
            NBTTagList lore=compoundDisplay.getTagList("Lore", 8);
            for (int j=0; j<lore.tagCount(); j++) {
                String loretext=lore.getStringTagAt(j);
                int keyend;
                if (loretext.startsWith("§b") && (keyend=loretext.indexOf(" §a"))>0) {
                    String key=loretext.substring(2, keyend);
                    String value=loretext.substring(keyend+3);
                    if (key.equals("Type:"))    type=value;
                    else if (key.equals("Speed:"))      speed=safelyParseInt(value);
                    else if (key.equals("Jump:"))       jump=safelyParseInt(value);
                    else if (key.equals("HP:"))         hitpoints=safelyParseInt(value);
                    else if (key.equals("Strength:"))   strength=safelyParseInt(value);
                    else if (key.equals("Description:"))    ;
                    else {
                        // System.out.println("unknown key "+key+" value '"+value+"'");
                    }
                }
            }
            StringBuilder extra=new StringBuilder(100);
            if (speed>=128  || speed>=120 && !("Horse".equals(type))) {
                extra.append(", Spd=").append(speed);
            }
            else if (speed>=120)
                extra.append(", Spd>120");

            if (jump>110)
                extra.append(", Jmp=").append(jump);
            else if (jump>100)
                extra.append(", Jmp>100");
            
            if (hitpoints>28)
                extra.append(", HP=").append(hitpoints);
            
            if (strength>=4)
                extra.append(", Str=").append(strength);
            
            if (extra.length()>2)
                return extra.substring(2);
        }
        return null;
    }
    
    private int safelyParseInt(String text) {
        int value=0;
        for (int i=0; i<text.length(); i++) {
            char c=text.charAt(i);
            if (c>='0' && c<='9')
                value=value*10+c-'0';
            else
                break;
        }
        return value;
    }
}
