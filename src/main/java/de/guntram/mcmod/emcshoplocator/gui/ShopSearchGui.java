package de.guntram.mcmod.emcshoplocator.gui;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import de.guntram.mcmod.emcshoplocator.ShopSign;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class ShopSearchGui extends GuiScreen {
    
    private GuiButton search, close;
    private GuiTextField pattern;
    private MatchingItemScrollList matchingStrings;
    private FoundShopsScrollList foundShops;
    
    @Override
    public void drawScreen (int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRenderer, "Shop search", width/2, 20, 0xffffff);
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
            mc.fontRenderer.drawString("on res "+sign.getRes()+
                    " at x="+sign.getPos().getX()+
                    "  y="+sign.getPos().getY()+
                    "  z="+sign.getPos().getZ()+
                    "", this.width/2+2, this.height-80, 0xffffff);
        }
        
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public void initGui() {
        this.pattern=new GuiTextField(0, fontRenderer, 20, 45, this.width/2-40, 20);
        this.pattern.setFocused(true);
        buttonList.add(search=new GuiButton(0, 20, this.height-30, "Search"));
        buttonList.add(close =new GuiButton(1, this.width-220, this.height-30, "Close"));
        this.matchingStrings=new MatchingItemScrollList(this, mc, this.width/2-40, this.height, 80, this.height-50, 20, 20);
        this.foundShops=new FoundShopsScrollList(mc, this.width/2-40, this.height, 45, this.height-170, this.width/2+20, 20);
    }
    
    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button==close) {
            mc.displayGuiScreen(null);
            mc.setIngameFocus();
        } else {
            HashSet<String> items=new HashSet<String>();
            String searchText=pattern.getText();
            Pattern regex=Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);
            for (ShopSign sign:EMCShopLocator.instance.getSigns()) {
                String itemName=sign.getItemName();
                if (regex.matcher(itemName).find())
                    items.add(itemName);
            }
            matchingStrings.setItems(items.toArray(new String[items.size()]));
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
}
