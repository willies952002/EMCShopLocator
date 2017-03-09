/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator.gui;

import java.util.Arrays;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

/**
 *
 * @author gbl
 */
public class MatchingItemScrollList extends GuiScrollingList {
    
    private String[] itemNames;
    private final Minecraft mc;
    private final ShopSearchGui gui;
    private int selectedIndex;
    
    MatchingItemScrollList(ShopSearchGui gui, Minecraft mc, int width, int height, int top, int bottom, int left, int slotheight) {
        super(mc, width, height, top, bottom, left, slotheight);
        setHeaderInfo(false, 0);
        itemNames=null;
        this.mc=mc;
        this.gui=gui;
        selectedIndex=-1;
    }
    
    public void setItems(String[] items) {
        itemNames=items;
        Arrays.sort(itemNames);
        selectedIndex=-1;
    }

    @Override
    protected int getSize() {
        return itemNames==null ? 0 
                : itemNames.length < ((bottom-top)/slotHeight) ? (bottom-top)/slotHeight
                : itemNames.length;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        if (itemNames!=null && index<itemNames.length) {
            String item=itemNames[index];
            // System.out.println("Itemlist chosen :"+item);
            gui.itemChosen(item);
            selectedIndex=index;
        }
    }

    @Override
    protected boolean isSelected(int index) {
        return index==selectedIndex;
    }

    @Override
    protected void drawBackground() {
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        if (slotIdx>=itemNames.length)
            return;
        mc.fontRenderer.drawString(itemNames[slotIdx], this.left+3 , slotTop+3 , 0xffffff);
    }
    
}
