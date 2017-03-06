package de.guntram.mcmod.emcshoplocator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

class SignFile {
    public static HashMap<String, ShopSign> load(File configFile) {
        HashMap<String, ShopSign> result=new HashMap<String, ShopSign>();
        File saveFile=signFile(configFile);
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
    
    public static void save(HashMap<String, ShopSign> signs, File configFile) throws IOException {
        File saveFile=signFile(configFile);
        BufferedWriter writer;
        writer=new BufferedWriter(new FileWriter(saveFile));
        for (ShopSign sign: signs.values()) {
            writer.write(sign.toString());
            writer.write('\n');
        }
        writer.close();
        System.out.println(Integer.toString(signs.size())+" signs saved");
    }
    
    private static File signFile(File configFile) {
        return new File(configFile.getAbsolutePath()+".signdata");
    }
}
