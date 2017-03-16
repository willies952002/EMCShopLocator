package de.guntram.mcmod.emcshoplocator.gui;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.ShopSign;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

public class ShopSearchGui extends GuiScreen {
    
    private GuiButton search, close, buy, sell;
    private GuiTextField pattern;
    private MatchingItemScrollList matchingStrings;
    private FoundShopsScrollList foundShops;
    private boolean inited=false;
    private boolean useSellPrice=false;
    private int lastwidth=0, lastheight=0;

    private final int serverx1=2, serverx2=42;
    private final int resx1=80, resx2=130;
    private final int xyzx1=170, xyzx2=200;
    
    private final int minwidth=480, minheight=360;
    
    // for map
    private static BlockPos newWaypointPos;
    private static String shopName;
    
    ShopSearchGui() {
        super();
        // System.out.println("new ShopSearchGui (constructor)");
    }

    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, "Shop search - "+EMCShopLocator.instance.getSignCount()+" signs", width/2, 20, 0xffffff);
        if (this.width<minwidth || this.height<minheight) {
            drawCenteredString(fontRenderer, "I am sorry ", width/2, 50, 0xff0000);
            drawCenteredString(fontRenderer, "Your GUI scale is too big to", width/2, 80, 0xffffff);
            drawCenteredString(fontRenderer, "safely display the shop search GUI.", width/2, 100, 0xffffff);
            drawCenteredString(fontRenderer, "Please choose a smaller GUI scale in", width/2, 120, 0xffffff);
            drawCenteredString(fontRenderer, "Video Settings and restart Minecraft.", width/2, 140, 0xffffff);
            
            drawCenteredString(fontRenderer, "Size="+this.width+"x"+this.height+", need at least "+minwidth+"x"+minheight+"", width/2, 180, 0xffff00);
        } else {
            pattern.drawTextBox();
            matchingStrings.drawScreen(mouseX, mouseY, partialTicks);
            foundShops.drawScreen(mouseX, mouseY, partialTicks);
            ShopSign sign;

            if ((sign=foundShops.getSelectedSign())!=null) {
                mc.fontRenderer.drawString(""+sign.getAmount()+" "+sign.getItemName(), this.width/2+2, this.height-150, 0xffffff);
                if (sign.getBuyPrice()>0)
                    mc.fontRenderer.drawString("buy at "+sign.getBuyPrice()+ " ("+sign.getBuyPerItem()+" per item)", this.width/2+2, this.height-130, 0xffffff);
                if (sign.getSellPrice()>0)
                    mc.fontRenderer.drawString("sell at "+sign.getSellPrice()+ " ("+sign.getSellPerItem()+" per item)", this.width/2+2, this.height-110, 0xffffff);
                mc.fontRenderer.drawString("Server ", this.width/2+serverx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+sign.getServer(),  this.width/2+serverx2, this.height-80, 0x80c0ff);
                mc.fontRenderer.drawString("Residence ", this.width/2+resx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+Integer.toString(sign.getRes()),  this.width/2+2+resx2, this.height-80, 0x80c0ff);
                mc.fontRenderer.drawString("XYZ ", this.width/2+xyzx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+sign.getPos().getX()+"/"+sign.getPos().getY()+"/"+sign.getPos().getZ(),
                            this.width/2+xyzx2, this.height-80, 0x80c0ff);
                if (sign.getChoosePosition()!=-1)
                    mc.fontRenderer.drawString("Choose sign, position "+sign.getChoosePosition(), this.width/2+2, this.height-60, 0xffffff);
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ShopSign sign;
        if (mouseY>this.height-80 && mouseY<this.height-60) {
            if ((sign=foundShops.getSelectedSign())!=null) {
                mouseX-=this.width/2;
                if (mouseX>serverx1 && mouseX<resx1) {
                    command(sign.getServer());
                } else if (mouseX>resx1 && mouseX<xyzx1) {
                    command("v "+sign.getRes());
                } else if (mouseX>xyzx1 && mouseX<this.width) {
                    // System.out.println("set waypoint part1, sign name ="+sign.getItemName());
                    newWaypointPos=sign.getPos();
                    shopName=sign.getItemName();
                }
            }
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }
        
    private void command(String string) {
        Minecraft.getMinecraft().player.sendChatMessage("/"+string);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public void initGui() {
        if (!inited || lastwidth!=width || lastheight!=height) {
            // System.out.println("init shop search gui");
            pattern=new GuiTextField(0, fontRenderer, 20, 45, width/2-40, 20);
            pattern.setFocused(true);
            matchingStrings=new MatchingItemScrollList(this, mc, width/2-40, height, 80, height-50, 20, 20);
            foundShops=new FoundShopsScrollList(mc, width/2-40, height, 80, height-170, width/2+20, 20);
            newWaypointPos=null;
            lastwidth=width; lastheight=height;
        }
        // Seems like the buttons get reset every time the GUI closes so we have to re-add them
        buttonList.add(close =new GuiButton(1, width-220, height-30, I18n.format("button.close")));
        if (width>=minwidth && height>=minheight) {
            buttonList.add(search=new GuiButton(0, 20, height-30, I18n.format("button.search")));
            buttonList.add(buy   =new GuiButton(2, width/2+20, 45, 40, 20, I18n.format("button.buy")));
            buttonList.add(sell  =new GuiButton(3, width-60, 45, 40, 20, I18n.format("button.sell")));
            markBuyOrSell();
        }

        inited=true;
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button==close) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (button==search) {
            HashSet<String> items=new HashSet<String>();
            String searchText=pattern.getText();
            Pattern regex=Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            for (ShopSign sign:EMCShopLocator.instance.getSigns()) {
                String itemName=sign.getItemName();
                if (regex.matcher(itemName).find())
                    items.add(itemName);
            }
            matchingStrings.setItems(items.toArray(new String[items.size()]));
        } else if (button==buy) {
            useSellPrice=false;
            markBuyOrSell();
        } else if (button==sell) {
            useSellPrice=true;
            markBuyOrSell();
        }
    }
    
    @Override
    public void keyTyped(char c, int i) throws IOException {
        super.keyTyped(c, i);
        if (c=='\r' || c=='\n')
            actionPerformed(search);
        else if (pattern.isFocused())
            pattern.textboxKeyTyped(c, i);
    }
    
    public void itemChosen(String item) {
        ArrayList<ShopSign> foundSigns=new ArrayList<ShopSign>();
        for (ShopSign sign:EMCShopLocator.instance.getSigns()) {
            String itemName=sign.getItemName();
            if (itemName.equals(item))
                foundSigns.add(sign);
        }
        foundShops.setSigns(foundSigns.toArray(new ShopSign[foundSigns.size()]));
    }
    
    public static BlockPos getJourneyMapNewWaypointPos() {
        return newWaypointPos;
    }
    
    public static void journeyMapNewWaypointPosHandled() {
        newWaypointPos=null;
    }
    
    public static String getJourneyMapShopName() {
        return shopName;
    }
    
    private void markBuyOrSell() {
        foundShops.setUseSellPrice(useSellPrice);
        if (useSellPrice) {
            buy.packedFGColour=0x800000;
            sell.packedFGColour=0x00ff00;
        } else {
            buy.packedFGColour=0x00ff00;
            sell.packedFGColour=0x800000;
        }
    }
}
