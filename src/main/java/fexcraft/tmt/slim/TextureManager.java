package fexcraft.tmt.slim;

import ebf.tim.TrainsInMotion;
import ebf.tim.utility.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;

public class TextureManager {


    public static ByteBuffer renderPixels = ByteBuffer.allocateDirect((4096*4096)*4);
    private static int i,ii, length, skyLight;
    private static int[] RGBint, pixels;
    private static final byte fullAlpha=(byte)0;

    //2 bytes for idk GL does something with them, then 4 bytes per pixel at 4kx4k resolution. any bigger breaks intel GPU's anyway.
    private static ByteBuffer bufferTexturePixels = GLAllocation.createDirectByteBuffer(67108866);

    public static int[] ingotColors = new int[]{};


    private static Map<ResourceLocation, Integer> tmtMap = new HashMap<>();

    private static Map<ResourceLocation, int[]> tmtTextureMap = new HashMap<>();

    private static ITextureObject object;
    /**
     * custom texture binding method, generally same as vanilla, but possible to improve performance later.
     * @param textureURI
     */
    public static boolean bindTexture(ResourceLocation textureURI) {
        if (textureURI == null){
            textureURI= new ResourceLocation(TrainsInMotion.MODID,"nullTrain");
        }
        if(ClientProxy.ForceTextureBinding) {
            object = Minecraft.getMinecraft().getTextureManager().getTexture(textureURI);
            if (object == null) {
                object = new SimpleTexture(textureURI);
                Minecraft.getMinecraft().getTextureManager().loadTexture(textureURI, object);
            }
            GL11.glBindTexture(GL_TEXTURE_2D, object.getGlTextureId());
        } else {
            Integer id = tmtMap.get(textureURI);
            if (id ==null){
                object = Minecraft.getMinecraft().getTextureManager().getTexture(textureURI);
                if (object == null) {
                    object = new SimpleTexture(textureURI);
                    Minecraft.getMinecraft().getTextureManager().loadTexture(textureURI, object);
                }
                id=object.getGlTextureId();
                tmtMap.put(textureURI, id);
            }
            if(GL11.glGetInteger(GL_TEXTURE_2D) !=id) {
                GL11.glBindTexture(GL_TEXTURE_2D, id);
            }
        }
        return true;
    }

    public static int[] loadTexture(ResourceLocation resource){
        int[] texture = tmtTextureMap.get(resource);

        bindTexture(resource);
        if(texture==null){
            int width =glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH);
            int height =glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT);

            GL11.glGetTexImage(GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL_UNSIGNED_BYTE, bufferTexturePixels);

            texture = new int[((width*height)*4)+2];
            texture[0]=width;
            texture[1]=height;
            for (int i=2; i<((width*height)*(4))-2; i+=(4)){
                texture[i+3]=bufferTexturePixels.get(i+3);//alpha
                texture[i+2]=bufferTexturePixels.get(i+2);//Red
                texture[i+1]=bufferTexturePixels.get(i+1);//Green
                texture[i]=bufferTexturePixels.get(i);//Blue
            }
            tmtTextureMap.put(resource, texture);
        }

        return texture;
    }




    public static void maskColors(ResourceLocation textureURI, List<int[]> colors){
        pixels = loadTexture(textureURI);
        if(pixels.length==2){
            return;
        }
        length = ((pixels[0]*pixels[1])*4)-4;

        for(i=0; i<length; i+=4) {
            renderPixels.put(i+3, b(pixels[i+3]));//alpha is always from host texture.
            if (pixels[i+3] == fullAlpha){
                continue;//skip pixels with no color
            }
            //for each set of recoloring
            if (colors!=null) {
                for (ii = 0; ii < colors.size(); ii++) {
                    RGBint = colors.get(ii);
                    //if it's within 10 RGB, add the actual color we want to the differences
                    if (colorInRange(pixels[i] & 0xFF, pixels[i + 1] & 0xFF, pixels[i + 2] & 0xFF,
                            RGBint[0] & 0xFF, (RGBint[0] >> 8) & 0xFF, (RGBint[0] >> 16) & 0xFF)) {
                        renderPixels.put(i, b(RGBint[1]));
                        renderPixels.put(i + 1, b(RGBint[1] >> 8));
                        renderPixels.put(i + 2, b(RGBint[1] >> 16));
                    } else {
                        renderPixels.put(i, b(pixels[i]));
                        renderPixels.put(i + 1, b(pixels[i + 1]));
                        renderPixels.put(i + 2, b(pixels[i + 2]));
                    }
                }
            } else {
                renderPixels.put(i, b(pixels[i]));
                renderPixels.put(i + 1, b(pixels[i + 1]));
                renderPixels.put(i + 2, b(pixels[i + 2]));
            }
        }

        glTexSubImage2D (GL_TEXTURE_2D, 0, 0, 0, pixels[0], pixels[1], GL_RGBA, GL_UNSIGNED_BYTE, renderPixels);
        renderPixels.clear();//reset the buffer to all 0's.
    }

    //most compilers should process this type of function faster than a normal typecast.
    public static byte b(int i){return (byte) i;}

    public static boolean colorInRange(int r, int g, int b, int oldR, int oldG, int oldB){
        return oldR-r>-15 && oldR-r <15 &&
                oldG-g>-15 && oldG-g <15 &&
                oldB-b>-15 && oldB-b <15;
    }


    public static void adjustLightFixture(World world, int i, int j, int k) {
        skyLight = world.getLightBrightnessForSkyBlocks(i, j, k, 0);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit,  skyLight % 65536,  skyLight / 65536f);
    }
}