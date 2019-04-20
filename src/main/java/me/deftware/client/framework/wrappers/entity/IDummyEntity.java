package me.deftware.client.framework.wrappers.entity;


import me.deftware.client.framework.wrappers.world.IBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

public class IDummyEntity extends IEntity {

	public IDummyEntity(IBlockPos pos) {
		super(new dummyEntity(pos));
	}

	public static class dummyEntity extends Entity {

		public dummyEntity(IBlockPos pos) {
			super(net.minecraft.entity.EntityType.PLAYER, Minecraft.getMinecraft().player.getEntityWorld());
			posX = pos.getX();
			posY = pos.getY();
			posZ = pos.getZ();
		}

		@Override
		protected void entityInit() {

		}

		@Override
		protected void readEntityFromNBT(NBTTagCompound nbtTagCompound) {

		}

		@Override
		protected void writeEntityToNBT(NBTTagCompound nbtTagCompound) {

		}
	}

}
