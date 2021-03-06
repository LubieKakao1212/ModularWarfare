package com.modularwarfare.client.model;

import org.lwjgl.opengl.GL11;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class RenderGun implements IItemRenderer {
	
	private static TextureManager renderEngine;
	public static float smoothing;
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
		case ENTITY:
			if (!Minecraft.getMinecraft().gameSettings.fancyGraphics)
				return false;
		case EQUIPPED:
		case EQUIPPED_FIRST_PERSON:
			return item != null && item.getItem() instanceof ItemGun && ((ItemGun) item.getItem()).type.model != null;
		default:
			break;
		}
		return false;
	}
	
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return false;
	}
	
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		// Avoid any broken cases by returning
		if (!(item.getItem() instanceof ItemGun))
			return;
		
		GunType gunType = ((ItemGun) item.getItem()).type;
		if (gunType == null)
			return;

		ModelGun model = gunType.model;
		if (model == null)
			return;
		
		{
			renderGun(type, item, gunType, data);
		}	
	}
	
	private void renderGun(ItemRenderType renderType, ItemStack item, GunType gunType, Object... data) {
		
		ModelGun model = gunType.model;
		
		if (renderEngine == null)
			renderEngine = Minecraft.getMinecraft().renderEngine;
		
		if(model == null)
			return;
			
		GL11.glPushMatrix();
		{
			switch(renderType)
			{
			
			case ENTITY:
			{
				EntityItem entity = (EntityItem) data[1];
				GL11.glRotatef(entity.getAge() + (entity.getAge() == 0 ? 0 : smoothing), 0F, 1F, 0F);
				GL11.glTranslatef(-0.2F + model.itemFrameOffset.x, 0.2F + model.itemFrameOffset.y,
						0.1F + model.itemFrameOffset.z);
				break;
			}
					
			case EQUIPPED:
			{
				GL11.glRotatef(35F, 0F, 0F, 1F);
				GL11.glRotatef(-5F, 0F, 1F, 0F);
				GL11.glTranslatef(0.75F, -0.22F, -0.08F);
				GL11.glScalef(1F, 1F, -1F);
				GL11.glTranslatef(model.thirdPersonOffset.x, model.thirdPersonOffset.y, model.thirdPersonOffset.z);
				break;
			}
				
			case EQUIPPED_FIRST_PERSON:
			{
				float adsSwitch = 0f;
				GL11.glRotatef(25F - 5F * adsSwitch, 0F, 0F, 1F);
				GL11.glRotatef(-5F, 0F, 1F, 0F);
				GL11.glTranslatef(0.15F, 0.2F + 0.175F * adsSwitch, -0.6F - 0.405F * adsSwitch);
				GL11.glRotatef(4.5F * adsSwitch, 0F, 0F, 1F);
				GL11.glTranslatef(-0.0F, -0.03F * adsSwitch, 0F);
				break;
			}
				
			default:
				break;
				
			}
			
			GL11.glPushMatrix();
			{
				float f = 1F / 16F;
				float modelScale = model.modelScale;
							
				renderEngine.bindTexture(new ResourceLocation(ModularWarfare.MOD_ID, "skins/" + gunType.weaponSkins[0].getSkin(gunType) + ".png"));				
				
				GL11.glScalef(modelScale, modelScale, modelScale);
				//GL11.glRotatef(25F - 5F * 1f, 0F, 0F, 1F);
				//GL11.glRotatef(-5F, 0F, 1F, 0F);
				//GL11.glTranslatef(3.5f, -0.3f, 0);

				model.renderGun(f);
				model.renderDefaultScope(f);
				model.renderDefaultBarrel(f);
				model.renderDefaultStock(f);
				model.renderDefaultGrip(f);
				model.renderDefaultGadget(f);
				
				
				//TODO ARM RENDER CALL
				 
				/*if (renderType == ItemRenderType.EQUIPPED_FIRST_PERSON && model.hasArms)
				{
					Minecraft mc = Minecraft.getMinecraft();
					renderAnimArm(mc.thePlayer, model, renderType, animations);
				}*/	
			}
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}
	
	/*private void renderFirstPersonArm(EntityPlayer player, ModelGun model, GunAnimations anim) {
		Minecraft mc = Minecraft.getMinecraft();
		ModelBiped modelBipedMain = new ModelBiped(0.0F);
		mc.renderEngine.bindTexture(mc.thePlayer.getLocationSkin());

		float f = 1.0F;
		GL11.glColor3f(f, f, f);
		modelBipedMain.onGround = 0.0F;
		
		GL11.glPushMatrix();
		{
			if (!anim.reloading && model.righthandPump) {
				RenderArms.renderArmPump(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
			} 
			else if (anim.charged < 0.9 && model.leftHandAmmo && model.rightHandCharge && anim.charged != -1.0F) {
				RenderArms.renderArmCharge(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
			}
			else if (anim.pumped < 0.9 && model.rightHandBolt && model.leftHandAmmo) {
				RenderArms.renderArmBolt(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
			}
			else if (!anim.reloading && !model.righthandPump) {
				RenderArms.renderArmDefault(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
			}
			else {
				RenderArms.renderArmReload(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
			}
			
			GL11.glScalef(model.rightArmScale.x, model.rightArmScale.y, model.rightArmScale.z);
			modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
			modelBipedMain.bipedRightArm.offsetY = 0F;
			if (!model.rightHandAmmo) {
				modelBipedMain.bipedRightArm.render(0.0625F);
			}
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		if (!anim.reloading && model.lefthandPump) {
			RenderArms.renderArmPump(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}
		else if (anim.charged < 0.9 && model.rightHandCharge && model.leftHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmCharge(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		}
		else if (anim.pumped < 0.9 && model.rightHandBolt && model.leftHandAmmo) {
			RenderArms.renderArmBolt(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		} else if (!anim.reloading && !model.lefthandPump) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		} else {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		}

		GL11.glScalef(model.leftArmScale.x, model.leftArmScale.y, model.leftArmScale.z);
		modelBipedMain.bipedLeftArm.offsetY = 0F;
		if (!model.leftHandAmmo) {
			modelBipedMain.bipedLeftArm.render(0.0625F);
		}
		GL11.glPopMatrix();
	}

	private void renderAnimArm(EntityPlayer player, ModelGun model, GunType type, GunAnimations anim) {
		Minecraft mc = Minecraft.getMinecraft();
		ModelBiped modelBipedMain = new ModelBiped(0.0F);
		mc.renderEngine.bindTexture(mc.thePlayer.getLocationSkin());
		GL11.glPushMatrix();
		GL11.glScalef(1 / model.modelScale, 1 / model.modelScale, 1 / model.modelScale);
		float f = 1.0F;
		GL11.glColor3f(f, f, f);
		modelBipedMain.onGround = 0.0F;
		GL11.glPushMatrix();
		float effectiveReloadAnimationProgress = anim.lastReloadAnimationProgress
				+ (anim.reloadAnimationProgress - anim.lastReloadAnimationProgress) * smoothing;

		if (anim.charged < 0.9 && model.rightHandCharge && model.rightHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmPump(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
		}
		else if (anim.pumped < 0.9 && model.rightHandBolt && model.rightHandAmmo) {
			RenderArms.renderArmBolt(model, anim, smoothing, model.rightArmChargeRot, model.rightArmChargePos);
			//model.renderAmmo(f);
		}
		else if (!anim.reloading) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.rightArmRot, model.rightArmPos);
		}
		else {
			RenderArms.renderArmReload(model, anim, smoothing, model.rightArmReloadRot, model.rightArmReloadPos);
		}

		GL11.glScalef(model.rightArmScale.x, model.rightArmScale.y, model.rightArmScale.z);
		modelBipedMain.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F, player);
		modelBipedMain.bipedRightArm.offsetY = 0F;
		if (model.rightHandAmmo) {
			modelBipedMain.bipedRightArm.render(0.0625F);
		}
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		if (anim.charged < 0.9 && model.leftHandCharge && model.leftHandAmmo && anim.charged != -1.0F) {
			RenderArms.renderArmCharge(model, anim, smoothing, model.leftArmChargeRot, model.leftArmChargePos);
		}

		else if (!anim.reloading && model.lefthandPump) {
			RenderArms.renderArmPump(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}

		else if (!anim.reloading) {
			RenderArms.renderArmDefault(model, anim, smoothing, model.leftArmRot, model.leftArmPos);
		}
		else if (effectiveReloadAnimationProgress < 0.5 && model.stagedleftArmReloadPos.x != 0) {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		} else if (effectiveReloadAnimationProgress > 0.5 && model.stagedleftArmReloadPos.x != 0) {
			RenderArms.renderArmReload(model, anim, smoothing, model.stagedleftArmReloadRot, model.stagedleftArmReloadPos);
		} else {
			RenderArms.renderArmReload(model, anim, smoothing, model.leftArmReloadRot, model.leftArmReloadPos);
		}

		GL11.glScalef(model.leftArmScale.x, model.leftArmScale.y, model.leftArmScale.z);
		modelBipedMain.bipedLeftArm.offsetY = 0F;
		if (model.leftHandAmmo) {
			modelBipedMain.bipedLeftArm.render(0.0625F);
		}
		GL11.glPopMatrix();

		GL11.glPopMatrix();
	}*/

}
