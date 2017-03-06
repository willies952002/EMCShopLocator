/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.mcmod.emcshoplocator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 *
 * @author gbl
 */
public class SignUploaderThread  extends Thread {

    HashMap<String, ShopSign> toSave;
    EMCShopLocator emcShopLocator;
    
    public SignUploaderThread(HashMap<String, ShopSign> signs, EMCShopLocator mod) {
        toSave=signs;
        emcShopLocator=mod;
    }
    
    @Override
    public void run() {
        try {
            HttpURLConnection conn=(HttpURLConnection) new URL("http://minecraft.guntram.de/emcmarket/upload.pl").openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            StringBuilder upload =new StringBuilder("upload=");
            for (ShopSign sign: toSave.values()) {
                if (!sign.isUploaded())
                    upload.append(sign.toString()).append("\n");
            }
            conn.setRequestProperty("Content-Length", Integer.toString(upload.length()));
            conn.setRequestProperty("charset", "utf-8");
            conn.setUseCaches(false);
            OutputStream stream=conn.getOutputStream();
            stream.write(upload.toString().getBytes(StandardCharsets.UTF_8));
            stream.flush();
            stream.close();
            BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line=reader.readLine())!=null)
                System.out.println(line);
            reader.close();
            for (ShopSign sign: toSave.values()) {
                sign.markUploaded();
            }
        } catch (IOException ex) {
            System.out.println("Can not upload shop sign info: "+ex);
        } finally {
            emcShopLocator.setSignUploadDone();
        }
    }
}
