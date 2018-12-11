package ebf.tim.api;

import ebf.tim.utility.RailUtility;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SkinRegistry {

    public static Map<String, Map<String, skin>> transports = new HashMap<String, Map<String, skin>>();

    public static void addSkin(Class c, String modid, String textureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,null, null,name,description);
    }

    public static void addSkin(Class c, String modid, String textureURI, String bogieTextureURI, String name, String description){
        addSkinRecolor(c,modid,textureURI,new String[]{bogieTextureURI},null,name,description);
    }

    public static void addSkin(Class c, String modid, String textureURI, @Nullable String[] bogieTextureURIs, String name, String description){
        addSkinRecolor(c,modid,textureURI,bogieTextureURIs,null,name,description);
    }

    public static void addSkinRecolor(Class c,String modid, String textureURI, String[] bogieTextureURI, @Nullable int[][] recolor, String skinName, String skinDescription){
        if (!transports.containsKey(c.getName())){
            transports.put(c.getName(), new HashMap<String, skin>());
            //add the default/null skin
            transports.get(c.getName()).put(modid + ":" + "-1", new skin(new ResourceLocation(modid, textureURI),resourceList(modid,bogieTextureURI), recolor, skinName, skinDescription));
        }
        transports.get(c.getName()).put(modid + ":" + textureURI, new skin(new ResourceLocation(modid, textureURI),resourceList(modid,bogieTextureURI), recolor, skinName, skinDescription));
    }

    private static ResourceLocation[] resourceList(String modid, String[] URIs){
        if(URIs == null){
            return null;
        }
        ResourceLocation[] value = new ResourceLocation[URIs.length];
        for (int i=0; i< URIs.length; i++){
            value[i]= new ResourceLocation(modid, URIs[i]);
        }
        return value;
    }

    public static ResourceLocation getTexture(Class c,String modid, String textureURI){
        if (!transports.containsKey(c.getName()) || !transports.get(c.getName()).containsKey(modid + ":" + textureURI)){
            return null;
        }
        return transports.get(c.getName()).get(modid + ":" + textureURI).texture;
    }

    public static skin getSkin(Class c, String internalResourceURI){
        if (!transports.containsKey(c.getName()) || !transports.get(c.getName()).containsKey(internalResourceURI)){
            return null;
        }
        return transports.get(c.getName()).get(internalResourceURI);
    }

    public static ResourceLocation getTexture(Class c, String internalResourceURI){
        if (!transports.containsKey(c.getName()) || !transports.get(c.getName()).containsKey(internalResourceURI)){
            return null;
        }
        return transports.get(c.getName()).get(internalResourceURI).texture;
    }

    public static ResourceLocation getDefaultTexture(Class c){
        if (!transports.containsKey(c.getName()) || transports.get(c.getName()).size()<1){
            return null;
        }
        return transports.get(c.getName()).values().iterator().next().texture;
    }

    public static String getSkinName(Class c,String modid, String textureURI){
        return RailUtility.translate(transports.get(c.getName()).get(modid + ":" + textureURI).name);
    }

    public static String getSkinDescription(Class c,String modid, String textureURI){
        return RailUtility.translate(transports.get(c.getName()).get(modid + ":" + textureURI).description);
    }

}
