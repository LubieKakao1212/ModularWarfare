package com.modularwarfare.common.network;

import com.modularwarfare.common.guns.ItemGun;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class PacketGunFire extends PacketBase {
	
	public PacketGunFire() {}
		
	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf data) {
		
	}

	@Override
	public void handleServerSide(EntityPlayerMP entityPlayer) {
		if(entityPlayer.getHeldItemMainhand() != null && entityPlayer.getHeldItemMainhand().getItem() instanceof ItemGun)
		{
			ItemGun itemGun = (ItemGun) entityPlayer.getHeldItemMainhand().getItem();
			// TODO: add ammo check here to save time
			itemGun.onGunFire(entityPlayer, entityPlayer.world, entityPlayer.getHeldItemMainhand(), itemGun);
		}
	}

	@Override
	public void handleClientSide(EntityPlayer entityPlayer) {
		// UNUSED
		System.out.println("called packet handle client");
	}

}
