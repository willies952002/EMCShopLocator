package de.guntram.mcmod.emcshoplocator.config;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class SLGuiFactory implements IModGuiFactory {

    @Override
    public boolean hasConfigGui() {
        return true;
    }
    
    @Override
    public void initialize(final Minecraft minecraftInstance) {
    }
    
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return SLGuiConfig.class;
    }

    @Override
    public GuiScreen createConfigGui(GuiScreen parentScreen) {
        return new SLGuiConfig(parentScreen);
    }
    
    @Override
    public Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
    
    @Override
    public IModGuiFactory.RuntimeOptionGuiHandler getHandlerFor(final IModGuiFactory.RuntimeOptionCategoryElement element) {
        return null;
    }
}
