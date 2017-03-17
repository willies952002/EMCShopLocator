package de.guntram.mcmod.emcshoplocator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import net.minecraft.client.Minecraft;

class SignFile {
    
    private static File configFile;
    
    public static HashMap<String, ShopSign> load() {
        HashMap<String, ShopSign> result=new HashMap<String, ShopSign>();
        File saveFile=signFile();
        BufferedReader reader;
        try {
            String s;
            reader=new BufferedReader(new FileReader(saveFile));
            while ((s=reader.readLine())!=null) {
                try {
                    ShopSign newSign=ShopSign.fromString(s);
                    result.put(newSign.getUniqueString(), newSign);
                } catch (NotAShopSignStringException ex) {
                    System.err.println(ex.getMessage());
                }
            }
            reader.close();
            System.out.println(Integer.toString(result.size())+" signs loaded");
            return result;
        } catch (IOException e) {
            return null;
        }
    }

    public static void save(HashMap<String, ShopSign> signs) throws IOException {
        File saveFile=signFile();
        BufferedWriter writer;
        writer=new BufferedWriter(new FileWriter(saveFile));
        for (ShopSign sign: signs.values()) {
            writer.write(sign.toString());
            writer.write('\n');
        }
        writer.close();
        System.out.println(Integer.toString(signs.size())+" signs saved");
    }
    
    public static void setConfigFile(File configFile) {
        SignFile.configFile=configFile;
    }
    
    private static File signFile() {
        return new File(configFile.getAbsolutePath()+"."+Minecraft.getMinecraft().getSession().getUsername()+".signs");
    }
}
