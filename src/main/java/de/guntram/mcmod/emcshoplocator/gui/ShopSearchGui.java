package de.guntram.mcmod.emcshoplocator.gui;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.ShopSign;
import de.guntram.mcmod.emcshoplocator.config.ConfigurationHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

public class ShopSearchGui extends GuiScreen {
    
    private GuiButton search, close, buy, sell;
    private GuiButton serverEnabled[];
    private GuiTextField pattern, minAmount;
    private MatchingItemScrollList matchingStrings;
    private FoundShopsScrollList foundShops;
    private boolean inited=false;
    private boolean useSellPrice=false;
    private int lastwidth=0, lastheight=0;
    private String lastChosenItem;              // for rebuilding the sign list when server enabled buttons change
    private int firstServerButtonIndex;

    private final int serverx1=2, serverx2=42;
    private final int resx1=80, resx2=110;
    private final int xyzx1=150, xyzx2=180;
    
    private final int minwidth=480, minheight=360;
    
    // for map
    private static BlockPos newWaypointPos;
    private static String shopName;
    
    private static final int BUTTON_SEARCH=0;
    private static final int BUTTON_CLOSE=1;
    private static final int BUTTON_BUY=2;
    private static final int BUTTON_SELL=3;
    private static final int BUTTON_ALL=10;
    private static final int BUTTON_THIS=11;
    private static final int BUTTON_NONE=12;
    private static final int BUTTON_FIRST_SERVER=100;
    
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
            minAmount.drawTextBox();
            matchingStrings.drawScreen(mouseX, mouseY, partialTicks);
            foundShops.drawScreen(mouseX, mouseY, partialTicks);
            ShopSign sign;

            if ((sign=foundShops.getSelectedSign())!=null) {
                mc.fontRenderer.drawString(""+sign.getAmount()+" "+sign.getItemName(), this.width/2+20, this.height-150, 0xffffff);
                if (sign.getBuyPrice()>0)
                    mc.fontRenderer.drawString("buy at "+sign.getBuyPrice()+ " ("+sign.getBuyPerItem()+" per item)", this.width/2+20, this.height-130, 0xffffff);
                if (sign.getSellPrice()>0)
                    mc.fontRenderer.drawString("sell at "+sign.getSellPrice()+ " ("+sign.getSellPerItem()+" per item)", this.width/2+20, this.height-110, 0xffffff);
                mc.fontRenderer.drawString("Server ", this.width/2+20+serverx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+sign.getServer(),  this.width/2+20+serverx2, this.height-80, 0x80c0ff);
                mc.fontRenderer.drawString("Res ", this.width/2+20+resx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+Integer.toString(sign.getRes()),  this.width/2+20+resx2, this.height-80, 0x80c0ff);
                mc.fontRenderer.drawString("XYZ ", this.width/2+20+xyzx1, this.height-80, 0xffffff);
                mc.fontRenderer.drawString("§n"+sign.getPos().getX()+"/"+sign.getPos().getY()+"/"+sign.getPos().getZ(),
                            this.width/2+20+xyzx2, this.height-80, 0x80c0ff);
                if (sign.getChoosePosition()!=-1)
                    mc.fontRenderer.drawString("Choose sign, position "+sign.getChoosePosition(), this.width/2+20, this.height-60, 0xffffff);
            }
        }
    }
    
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        ShopSign sign;
        if (mouseY>this.height-80 && mouseY<this.height-60) {
            if ((sign=foundShops.getSelectedSign())!=null) {
                mouseX-=this.width/2+20;
                if (mouseX>serverx1 && mouseX<resx1) {
                    command(sign.getServer());
                } else if (mouseX>resx1 && mouseX<xyzx1) {
                    command("v "+sign.getRes());
                } else if (mouseX>xyzx1 && mouseX<this.width) {
                    // System.out.println("set waypoint part1, sign name ="+sign.getItemName());
                    BlockPos pos = sign.getPos();
                    newWaypointPos=pos;
                    shopName=sign.getItemName();
                    EntityPlayerSP player = Minecraft.getMinecraft().player;
                    double dx=player.posX-(pos.getX()+0.5);
                    double dz=player.posZ-(pos.getZ()+0.5);
                    double dy=player.posY+1-pos.getY();
                    double distance=Math.sqrt(dx*dx+dz*dz);
                    player.rotationYaw=player.rotationYawHead=(float)(Math.atan2(dz, dx)*180/Math.PI+90.0);
                    player.rotationPitch=(float) (Math.atan2(dy, distance)*180/Math.PI);
                }
            }
        } else {
            super.mouseClicked(mouseX, mouseY, mouseButton);
            pattern.mouseClicked(mouseX, mouseY, mouseButton);
            minAmount.mouseClicked(mouseX, mouseY, mouseButton);
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
            pattern=new GuiTextField(0, fontRenderer, 20, 45, width/2-40-70, 20);
            pattern.setFocused(true);
            minAmount=new GuiTextField(1, fontRenderer, width/2-40-20, 45, 40, 20);
            minAmount.setText(I18n.format("textboxhelp.amount"));
            minAmount.setTextColor(0x808080);
            matchingStrings=new MatchingItemScrollList(this, mc, width/2-40, height, 80, height-50, 20, 20);
            foundShops=new FoundShopsScrollList(mc, width/2-40, height, 80, height-170, width/2+20, 20);
            serverEnabled=new GuiButton[ConfigurationHandler.getNumberOfServers()];
            newWaypointPos=null;
            lastwidth=width; lastheight=height;
        }
        // Seems like the buttons get reset every time the GUI closes so we have to re-add them
        buttonList.add(close =new GuiButton(BUTTON_CLOSE, width-220, height-30, I18n.format("button.close")));
        if (width>=minwidth && height>=minheight) {
            buttonList.add(search=new GuiButton(BUTTON_SEARCH, 20, height-30, I18n.format("button.search")));
            buttonList.add(buy   =new GuiButton(BUTTON_BUY, width/2+20, 45, 40, 20, I18n.format("button.buy")));
            buttonList.add(sell  =new GuiButton(BUTTON_SELL, width-60, 45, 40, 20, I18n.format("button.sell")));
            markBuyOrSell();
        }
        
        firstServerButtonIndex=buttonList.size();
        for (int i=0; i<ConfigurationHandler.getNumberOfServers(); i++) {
            GuiButton newButton=new GuiButton(i+BUTTON_FIRST_SERVER, width/2-20, 80+i*20, 40, 20, ConfigurationHandler.getServerName(i));
            serverEnabled[i]=newButton;
            buttonList.add(newButton);
            newButton.packedFGColour=(ConfigurationHandler.isServerEnabled(i) ? 0x00ff00 : 0x800000);
        }

        buttonList.add(new GuiButton(BUTTON_ALL, width/2-20, 320, 40, 20, I18n.format("button.all")));
        buttonList.add(new GuiButton(BUTTON_THIS, width/2-20, 340, 40, 20, I18n.format("button.this")));
        buttonList.add(new GuiButton(BUTTON_NONE, width/2-20, 360, 40, 20, I18n.format("button.none")));
        inited=true;
        Keyboard.enableRepeatEvents(true);
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {

        // handle enable/disable server first, and search again in this case
        int buttonID=button.id;
        boolean newState;
        if (buttonID>=BUTTON_FIRST_SERVER && buttonID<BUTTON_FIRST_SERVER+ConfigurationHandler.getNumberOfServers()) {
            newState=!ConfigurationHandler.isServerEnabled(buttonID-BUTTON_FIRST_SERVER);
            if (GuiScreen.isShiftKeyDown()) {
                for (int i=0; i<ConfigurationHandler.getNumberOfServers(); i++) {
                    ConfigurationHandler.setServerEnabled(i, !newState);
                    GuiButton tempButton = buttonList.get(i+firstServerButtonIndex);
                    tempButton.packedFGColour=(!newState ? 0x00ff00 : 0x800000);
                }
            }
            ConfigurationHandler.setServerEnabled(buttonID-BUTTON_FIRST_SERVER, newState);
            button.packedFGColour=(newState ? 0x00ff00 : 0x800000);
            button=search;
            if (lastChosenItem != null)
                itemChosen(lastChosenItem);
        }
        
        if (buttonID>=BUTTON_ALL && buttonID<=BUTTON_NONE) {
            int currentServer=ConfigurationHandler.getServerIndex(EMCShopLocator.instance.serverName);
            for (int i=0; i<ConfigurationHandler.getNumberOfServers(); i++) {
                newState=(buttonID==10 || buttonID==BUTTON_THIS && i==currentServer);
                ConfigurationHandler.setServerEnabled(i, newState);
                GuiButton tempButton = buttonList.get(i+firstServerButtonIndex);
                tempButton.packedFGColour=(newState ? 0x00ff00 : 0x800000);
            }
            button=search;
            if (lastChosenItem != null)
                itemChosen(lastChosenItem);
        }

        if (button==close) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else if (button==search) {
            HashSet<String> items=new HashSet<String>();
            String searchText=pattern.getText();
            if (searchText.length()>0 && searchText.charAt(0) == '#' && EMCShopLocator.isDeveloperDebugVersion()) {
                if (searchText.equals("#uploadall")) {
                    System.out.println("Starting upload");
                    EMCShopLocator.instance.uploadAll();
                }
                else if (searchText.equals("#signinfo")) {
                    System.out.println(EMCShopLocator.instance.getSignCount()+" signs");
                    System.out.println(EMCShopLocator.instance.getSigns().size()+" sign values");
                }
            }
            Pattern regex=Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            int minitems;
            try {
                minitems=Integer.parseInt(minAmount.getText());
            } catch (Exception ex) {
                minitems=1;
            }
            for (ShopSign sign:EMCShopLocator.instance.getSigns()) {
                if (sign.markedForDeletion() || sign.getAmount()<minitems)
                    continue;
                String itemName=sign.getItemName();
                if (itemName!=null && regex.matcher(itemName).find() 
                && ConfigurationHandler.isServerEnabled(sign.getServerIndex()))
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
        else if (minAmount.isFocused() && (c>='0' && c<='9' || c<32)) {
            String current=minAmount.getText();
            if (current.length()>0 && !Character.isDigit(current.charAt(0)))
                minAmount.setText("");
            minAmount.textboxKeyTyped(c, i);
            minAmount.setTextColor(0xffffff);
        }
    }
    
    public void itemChosen(String item) {
        ArrayList<ShopSign> foundSigns=new ArrayList<ShopSign>();
        int minitems;
        try {
            minitems=Integer.parseInt(minAmount.getText());
        } catch (Exception ex) {
            minitems=1;
        }
        for (ShopSign sign:EMCShopLocator.instance.getSigns()) {
            if (sign.markedForDeletion() || sign.getAmount()<minitems)
                continue;
            String itemName=sign.getItemName();
            if (itemName!=null && itemName.equals(item)
            && ConfigurationHandler.isServerEnabled(sign.getServerIndex()))
                foundSigns.add(sign);
        }
        foundShops.setSigns(foundSigns.toArray(new ShopSign[foundSigns.size()]));
        lastChosenItem=item;
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
