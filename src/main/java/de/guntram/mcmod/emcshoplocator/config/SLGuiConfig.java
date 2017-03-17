package de.guntram.mcmod.emcshoplocator.config;

import de.guntram.mcmod.emcshoplocator.EMCShopLocator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import static net.minecraftforge.common.config.Configuration.CATEGORY_CLIENT;
import net.minecraftforge.fml.client.config.GuiConfig;

public class SLGuiConfig extends GuiConfig {
    public SLGuiConfig(GuiScreen parent) {
        super(parent,
                new ConfigElement(ConfigurationHandler.getConfig().getCategory(CATEGORY_CLIENT)).getChildElements(),
                EMCShopLocator.MODID,
                false,
                false,
                "EMC Shop Locator config");
    }
}
