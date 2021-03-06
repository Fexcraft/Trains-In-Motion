package ebf.tim.gui;


import ebf.tim.TrainsInMotion;
import ebf.tim.api.skin;
import ebf.tim.entities.GenericRailTransport;
import ebf.tim.models.Bogie;
import ebf.tim.models.RenderEntity;
import ebf.tim.networking.PacketPaint;
import ebf.tim.utility.DebugUtil;
import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.TextureManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 *@author Oskiek
 */
public class GUISkinManager extends GuiScreen {


    public GuiButton buttonLeft;
    public GuiButton buttonRight;
    public GuiButton buttonApply;

    List<String>  skinList = new ArrayList<>();
    skin currentSkin;

    int page = 0;

    protected int xSize = 176;
    protected int ySize = 166;

    public static int guiTop;
    public static int guiLeft;
    public GenericRailTransport entity;

    public GUISkinManager(GenericRailTransport t){
        entity=t;
    }

    @Override
    public void initGui() {
        super.initGui();
        skinList=new ArrayList<>();
        for (String s : entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).keySet()) {
            skinList.add(s);
        }

        currentSkin=entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).get(entity.getDefaultSkin());
        guiLeft=new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight).getScaledWidth();
        guiTop=new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight).getScaledHeight();

        Keyboard.enableRepeatEvents(true);

        buttonList =new ArrayList();
        buttonList.add(buttonLeft = new GuiButton(-1, percentLeft(10)-10,percentTop(65), 20,20,"<<"));//left
        buttonList.add(buttonRight = new GuiButton(-1, percentLeft(90)-10,percentTop(65), 20,20,">>"));//right
        buttonList.add(buttonApply = new GuiButton(-1, percentLeft(50)-16,percentTop(80), 32,20,"Apply"));//apply
        buttonApply.visible=true;
    }

    @Override
    public void updateScreen() {}

    public static int percentTop(int value){return (int)(guiTop*(value*0.01f));}
    public static int percentLeft(int value){return (int)(guiLeft*(value*0.01f));}

    @Override
    public void drawScreen(int parWidth, int parHeight, float p_73863_3_)
    {
        super.drawScreen(parWidth, parHeight, p_73863_3_);

        initGui();
        if(currentSkin==null){return;}
        GL11.glPushMatrix();
        GL11.glColor4f(1F, 1F, 1F, 0.5F);
        float offsetFromScreenLeft = width * 0.5f;


        fontRendererObj.drawString(currentSkin.name,
                (int)(offsetFromScreenLeft - fontRendererObj.getStringWidth(currentSkin.name)*0.5f),
                (int)((height*0.1f)*6),0,false);

        if(currentSkin.getDescription()!=null) {
            for(int i=0; i<currentSkin.getDescription().length;i++) {
                fontRendererObj.drawString(currentSkin.getDescription()[i],
                        (int) (offsetFromScreenLeft - fontRendererObj.getStringWidth(currentSkin.getDescription()[0]) * 0.5f),
                        (int) ((height * 0.1f) * 7)+(10*i), 0, false);
            }
        }
        RenderEntity.instance.doRender(entity,page, 0,0,0,0, true, currentSkin.texture.toString());
        renderTransport(entity,skinList.get(page));
        GL11.glPopMatrix();

    }

    @Override
    protected void actionPerformed(GuiButton parButton) {
        if (parButton==buttonApply) {
            applySkin();
            mc.displayGuiScreen(null);//todo make an actual close button
        }
        else if (parButton==buttonLeft) {
            DebugUtil.println(page, skinList.size(), skinList.get(page), entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).get(skinList.get(page)).name);
            page = (page <= 0 ? entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).keySet().size() -1: page - 1);
            currentSkin=entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).get(skinList.get(page));
        }
        else if (parButton==buttonRight) {
            page = (page+1 >= entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).keySet().size() ? 0 : page + 1);
            currentSkin=entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).get(skinList.get(page));
        }
    }


    void applySkin(){
        TrainsInMotion.keyChannel.sendToServer(new PacketPaint(skinList.get(page), entity.getEntityId()));
        entity.renderData.needsModelUpdate=true;
    }

    @Override
    public boolean doesGuiPauseGame() {return true;}

    void renderTransport(GenericRailTransport entity, String key) {

        //get skin from page
        ebf.tim.api.skin s = entity.getSkinList(Minecraft.getMinecraft().thePlayer, true).get(key);
        //bind skin to render
        TextureManager.bindTexture(s.texture);

        //render models with offsets
        int i=1;
        for (ModelBase m : entity.getModel()) {
            GL11.glPushMatrix();
            if (entity.modelOffsets() != null && entity.modelOffsets().length > i) {
                GL11.glTranslated(entity.modelOffsets()[i][0], entity.modelOffsets()[i][1], entity.modelOffsets()[i][2]);
            }
            m.render(null, 0, 0, 0, 0, 0, 0.0625f);
            GL11.glPopMatrix();
            i++;
        }

        if(entity.bogies()==null){
            return;
        }
        //render bogies with textures and offsets
        int b = 0, sb = 0;
        for (Bogie m : entity.bogies()) {
            TextureManager.bindTexture(s.getBogieSkin(b));
            b++;
            GL11.glPushMatrix();
            GL11.glTranslated(-m.offset[0], -m.offset[1], -m.offset[2]);
            m.bogieModel.render(null, 0, 0, 0, 0, 0, 0.0625f);
            //render the sub bogies with textures if the bogie has any
            if (m.subBogies != null) {
                for (Bogie sub : m.subBogies) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(-sub.offset[0], -sub.offset[1], -sub.offset[2]);
                    sub.bogieModel.render(null, 0, 0, 0, 0, 0, 0.0625f);
                    GL11.glPopMatrix();
                }
            }
            GL11.glPopMatrix();
        }
    }
}