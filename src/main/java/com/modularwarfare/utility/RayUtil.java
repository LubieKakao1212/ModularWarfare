package com.modularwarfare.utility;

import com.modularwarfare.ModularWarfare;
import com.modularwarfare.common.entity.EntityBot;
import com.modularwarfare.common.guns.GunType;
import com.modularwarfare.common.guns.ItemGun;
import com.modularwarfare.common.network.PacketGunTrail;
import com.modularwarfare.common.network.PacketPlaySound;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class RayUtil {

    public static boolean isLiving(Entity entity){
        return entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
    }


    public static Vec3d getGunAccuracy(float pitch, float yaw, final float accuracy, final Random rand) {
        final float randAccPitch = rand.nextFloat() * accuracy;
        final float randAccYaw = rand.nextFloat() * accuracy;
        pitch += (rand.nextBoolean() ? randAccPitch : (-randAccPitch));
        yaw += (rand.nextBoolean() ? randAccYaw : (-randAccYaw));
        final float f = MathHelper.cos(-yaw * 0.017453292f - 3.1415927f);
        final float f2 = MathHelper.sin(-yaw * 0.017453292f - 3.1415927f);
        final float f3 = -MathHelper.cos(-pitch * 0.017453292f);
        final float f4 = MathHelper.sin(-pitch * 0.017453292f);
        return new Vec3d((double)(f2 * f3), (double)f4, (double)(f * f3));
    }

    public static float calculateAccuracyServer(final ItemGun item, final EntityLivingBase player) {
        final GunType gun = item.type;
        float acc = gun.bulletSpread;
        if (player.posX != player.lastTickPosX || player.posZ != player.lastTickPosZ) {
            acc += 0.75f;
        }
        if (!player.onGround) {
            acc += 1.5f;
        }
        if (player.isSprinting()) {
            acc += 0.25f;
        }
        if (player.isSneaking()) {
            acc *= gun.accuracySneakFactor;
        }
        if(player instanceof EntityBot){
            switch (((EntityBot) player).difficulty){
                case 1:
                    acc += 1.5f;
                    break;
                case 2:
                    acc += 0.75f;
                case 3:
                    acc += 0.25f;
                case 4:
                    break;
                default:
                    break;
            }
        }
        return acc;
    }

    public static float calculateAccuracyClient(final ItemGun item, final EntityPlayer player) {
        final GunType gun = item.type;
        float acc = gun.bulletSpread;
        final GameSettings settings = Minecraft.getMinecraft().gameSettings;
        if (settings.keyBindForward.isKeyDown() || settings.keyBindLeft.isKeyDown() || settings.keyBindBack.isKeyDown() || settings.keyBindRight.isKeyDown()) {
            acc += 0.75f;
        }
        if (!player.onGround) {
            acc += 1.5f;
        }
        if (player.isSprinting()) {
            acc += 0.25f;
        }
        if (player.isSneaking()) {
            acc *= gun.accuracySneakFactor;
        }
        return acc;
    }


    /**
     * Attacks the given entity with the given damage source and amount, but
     * preserving the entity's original velocity instead of applying knockback, as
     * would happen with
     * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)} <i>(More
     * accurately, calls that method as normal and then resets the entity's velocity
     * to what it was before).</i> Handy for when you need to damage an entity
     * repeatedly in a short space of time.
     *
     * @param entity The entity to attack
     * @param source The source of the damage
     * @param amount The amount of damage to apply
     * @return True if the attack succeeded, false if not.
     */
    public static boolean attackEntityWithoutKnockback(Entity entity, DamageSource source, float amount) {
        double vx = entity.motionX;
        double vy = entity.motionY;
        double vz = entity.motionZ;
        boolean succeeded = entity.attackEntityFrom(source, amount);
        entity.motionX = vx;
        entity.motionY = vy;
        entity.motionZ = vz;
        return succeeded;
    }

    /**
     * Helper method which does a rayTrace for entities from an entity's eye level in the direction they are looking
     * with a specified range, using the tracePath method. Tidies up the code a bit. Border size defaults to 1.
     *
     * @param world
     * @param range
     * @return
     */
    @Nullable
    public static RayTraceResult standardEntityRayTrace(World world, EntityLivingBase player, double range, ItemGun item, boolean isPunched){
        HashSet<Entity> hashset = new HashSet<Entity>(1);
        hashset.add(player);

        final float accuracy = calculateAccuracyServer(item, player);
        final Vec3d dir = getGunAccuracy(player.rotationPitch, player.rotationYaw, accuracy, player.world.rand);

        double dx = dir.x * range;
        double dy = dir.y * range;
        double dz = dir.z * range;

        ModularWarfare.NETWORK.sendToDimension(new PacketGunTrail(player.posX,player.getEntityBoundingBox().minY + player.getEyeHeight() - 0.10000000149011612, player.posZ, player.motionX, player.motionZ, dir.x, dir.y, dir.z, range, 10, isPunched), player.world.provider.getDimension());

        return RayUtil.tracePath(world, (float)player.posX, (float)(player.getEntityBoundingBox().minY + player.getEyeHeight() - 0.10000000149011612), (float)player.posZ, (float)(player.posX + dx+ player.motionX), (float)(player.posY + dy + player.motionY), (float)(player.posZ + dz + player.motionZ), 0.1f, hashset, false);
    }

    /**
     * Helper method which does a rayTrace for entities from a entity's eye level in the direction they are looking with
     * a specified range and radius, using the tracePath method. Tidies up the code a bit.
     *
     * @param world
     * @param entity
     * @param range
     * @param borderSize
     * @return
     */
    @Nullable
    public static RayTraceResult standardEntityRayTrace(World world, EntityLivingBase entity, double range,
                                                        float borderSize){
        double dx = entity.getLookVec().x * range;
        double dy = entity.getLookVec().y * range;
        double dz = entity.getLookVec().z * range;
        HashSet<Entity> hashset = new HashSet<Entity>(1);
        hashset.add(entity);
        return RayUtil.tracePath(world, (float)entity.posX, (float)(entity.getEntityBoundingBox().minY + entity.getEyeHeight()), (float)entity.posZ, (float)(entity.posX + dx), (float)(entity.posY + entity.getEyeHeight() + dy), (float)(entity.posZ + dz),
                borderSize, hashset, false);
    }

    /**
     * Method for ray tracing entities (the useless default method doesn't work, despite EnumHitType having an ENTITY
     * field...) You can also use this for seeking.
     *
     * @param world
     * @param x startX
     * @param y startY
     * @param z startZ
     * @param tx endX
     * @param ty endY
     * @param tz endZ
     * @param borderSize extra area to examine around line for entities
     * @param excluded any excluded entities (the player, etc)
     * @return a RayTraceResult of either the block hit (no entity hit), the entity hit (hit an entity), or null for
     *         nothing hit
     */
    @Nullable
    public static RayTraceResult tracePath(World world, float x, float y, float z, float tx, float ty, float tz, float borderSize, HashSet<Entity> excluded, boolean collideablesOnly){

        Vec3d startVec = new Vec3d(x, y, z);
        // Vec3d lookVec = new Vec3d(tx-x, ty-y, tz-z);
        Vec3d endVec = new Vec3d(tx, ty, tz);

        float minX = x < tx ? x : tx;
        float minY = y < ty ? y : ty;
        float minZ = z < tz ? z : tz;
        float maxX = x > tx ? x : tx;
        float maxY = y > ty ? y : ty;
        float maxZ = z > tz ? z : tz;
        AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(borderSize, borderSize,
                borderSize);
        List<Entity> allEntities = world.getEntitiesWithinAABBExcludingEntity(null, bb);
        RayTraceResult blockHit = rayTraceBlocks(world, startVec, endVec,  true, true, false);
        startVec = new Vec3d(x, y, z);
        endVec = new Vec3d(tx, ty, tz);
        float maxDistance = (float)endVec.distanceTo(startVec);
        if(blockHit != null){
            maxDistance = (float)blockHit.hitVec.distanceTo(startVec);
        }

        Entity closestHitEntity = null;
        Vec3d hit = null;
        float closestHit = maxDistance;
        float currentHit = 0.f;
        AxisAlignedBB entityBb;// = ent.getBoundingBox();
        RayTraceResult intercept;
        for(Entity ent : allEntities){
            if((ent.canBeCollidedWith() || !collideablesOnly) && ((excluded != null && !excluded.contains(ent)) || excluded == null)){
                if(ent instanceof EntityLivingBase) {
                    EntityLivingBase entityLivingBase = (EntityLivingBase)ent;
                    if(!ent.isDead && entityLivingBase.getHealth() > 0.0F){
                        if (ent instanceof EntityPlayerMP) {
                            ModularWarfare.NETWORK.sendTo(new PacketPlaySound(ent.getPosition(), "flyby", 0.4f, 1f), (EntityPlayerMP) ent);
                        }
                        float entBorder = ent.getCollisionBorderSize();
                        entityBb = ent.getEntityBoundingBox();
                        if (entityBb != null) {
                            entityBb = entityBb.grow(entBorder, entBorder, entBorder);
                            intercept = entityBb.calculateIntercept(startVec, endVec);
                            if (intercept != null) {
                                currentHit = (float) intercept.hitVec.distanceTo(startVec);
                                hit = intercept.hitVec;
                                if (currentHit < closestHit || currentHit == 0) {
                                    closestHit = currentHit;
                                    closestHitEntity = ent;
                                }
                            }
                        }
                    }
                }
            }
        }
        if(closestHitEntity != null && hit != null){
            blockHit = new RayTraceResult(closestHitEntity, hit);
        }
        return blockHit;
    }



    @Nullable
    public static RayTraceResult rayTraceBlocks(World world, Vec3d vec31, Vec3d vec32, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
        if (!Double.isNaN(vec31.x) && !Double.isNaN(vec31.y) && !Double.isNaN(vec31.z)) {
            if (!Double.isNaN(vec32.x) && !Double.isNaN(vec32.y) && !Double.isNaN(vec32.z)) {
                int i = MathHelper.floor(vec32.x);
                int j = MathHelper.floor(vec32.y);
                int k = MathHelper.floor(vec32.z);
                int l = MathHelper.floor(vec31.x);
                int i1 = MathHelper.floor(vec31.y);
                int j1 = MathHelper.floor(vec31.z);
                BlockPos blockpos = new BlockPos(l, i1, j1);
                IBlockState iblockstate = world.getBlockState(blockpos);
                Block block = iblockstate.getBlock();

                if ((!ignoreBlockWithoutBoundingBox || iblockstate.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) && block.canCollideCheck(iblockstate, stopOnLiquid)) {
                    RayTraceResult raytraceresult = iblockstate.collisionRayTrace(world, blockpos, vec31, vec32);

                    if (raytraceresult != null) {
                        return raytraceresult;
                    }
                }

                RayTraceResult raytraceresult2 = null;
                int k1 = 200;

                while (k1-- >= 0) {
                    if (Double.isNaN(vec31.x) || Double.isNaN(vec31.y) || Double.isNaN(vec31.z)) {
                        return null;
                    }

                    if (l == i && i1 == j && j1 == k) {
                        return returnLastUncollidableBlock ? raytraceresult2 : null;
                    }

                    boolean flag2 = true;
                    boolean flag = true;
                    boolean flag1 = true;
                    double d0 = 999.0D;
                    double d1 = 999.0D;
                    double d2 = 999.0D;

                    if (i > l) {
                        d0 = (double) l + 1.0D;
                    } else if (i < l) {
                        d0 = (double) l + 0.0D;
                    } else {
                        flag2 = false;
                    }

                    if (j > i1) {
                        d1 = (double) i1 + 1.0D;
                    } else if (j < i1) {
                        d1 = (double) i1 + 0.0D;
                    } else {
                        flag = false;
                    }

                    if (k > j1) {
                        d2 = (double) j1 + 1.0D;
                    } else if (k < j1) {
                        d2 = (double) j1 + 0.0D;
                    } else {
                        flag1 = false;
                    }

                    double d3 = 999.0D;
                    double d4 = 999.0D;
                    double d5 = 999.0D;
                    double d6 = vec32.x - vec31.x;
                    double d7 = vec32.y - vec31.y;
                    double d8 = vec32.z - vec31.z;

                    if (flag2) {
                        d3 = (d0 - vec31.x) / d6;
                    }

                    if (flag) {
                        d4 = (d1 - vec31.y) / d7;
                    }

                    if (flag1) {
                        d5 = (d2 - vec31.z) / d8;
                    }

                    if (d3 == -0.0D) {
                        d3 = -1.0E-4D;
                    }

                    if (d4 == -0.0D) {
                        d4 = -1.0E-4D;
                    }

                    if (d5 == -0.0D) {
                        d5 = -1.0E-4D;
                    }

                    EnumFacing enumfacing;

                    if (d3 < d4 && d3 < d5) {
                        enumfacing = i > l ? EnumFacing.WEST : EnumFacing.EAST;
                        vec31 = new Vec3d(d0, vec31.y + d7 * d3, vec31.z + d8 * d3);
                    } else if (d4 < d5) {
                        enumfacing = j > i1 ? EnumFacing.DOWN : EnumFacing.UP;
                        vec31 = new Vec3d(vec31.x + d6 * d4, d1, vec31.z + d8 * d4);
                    } else {
                        enumfacing = k > j1 ? EnumFacing.NORTH : EnumFacing.SOUTH;
                        vec31 = new Vec3d(vec31.x + d6 * d5, vec31.y + d7 * d5, d2);
                    }

                    l = MathHelper.floor(vec31.x) - (enumfacing == EnumFacing.EAST ? 1 : 0);
                    i1 = MathHelper.floor(vec31.y) - (enumfacing == EnumFacing.UP ? 1 : 0);
                    j1 = MathHelper.floor(vec31.z) - (enumfacing == EnumFacing.SOUTH ? 1 : 0);
                    blockpos = new BlockPos(l, i1, j1);
                    IBlockState iblockstate1 = world.getBlockState(blockpos);
                    Block block1 = iblockstate1.getBlock();

                    if(block1 instanceof BlockDoor || block1 instanceof BlockGlass || block1 instanceof BlockBarrier){
                        return null;
                    }

                    if (!ignoreBlockWithoutBoundingBox || iblockstate1.getMaterial() == Material.BARRIER || iblockstate1.getMaterial() == Material.PORTAL || iblockstate1.getCollisionBoundingBox(world, blockpos) != Block.NULL_AABB) {
                        if (block1.canCollideCheck(iblockstate1, stopOnLiquid)) {
                            RayTraceResult raytraceresult1 = iblockstate1.collisionRayTrace(world, blockpos, vec31, vec32);
                            if (raytraceresult1 != null) {
                                return raytraceresult1;
                            }
                        } else {
                            raytraceresult2 = new RayTraceResult(RayTraceResult.Type.MISS, vec31, enumfacing, blockpos);
                        }
                    }
                }

                return returnLastUncollidableBlock ? raytraceresult2 : null;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }




}