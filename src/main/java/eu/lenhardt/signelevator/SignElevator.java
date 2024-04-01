package eu.lenhardt.signelevator;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SignElevator implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("sign-elevator");

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		// LOGGER.info("Hello Fabric world!");

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			BlockPos pos = hitResult.getBlockPos();
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			// check if clicked block is a wall sign
			if (block instanceof WallSignBlock) {
				BlockEntity blockEntity = world.getBlockEntity(pos);
				if (blockEntity instanceof SignBlockEntity) {

					// read text from sign to determine if player wants to go up or down
					SignBlockEntity sign = (SignBlockEntity) blockEntity;
					SignText signText = sign.getText(true);
					Text text = signText.getMessage(0, true);

					// player wants to get up
					if ("[UP]".equals(text.getString())) {

						// +1 to exclude the current sign, only search next sign from current+1 above
						Integer target = this.getVerticalWallSignUp(world, pos, pos.getY() + 1);
						if (target == null) {
							player.sendMessage(Text.literal("No higher lift level found."));
						} else {
							player.teleport(player.getX(), target, player.getZ());
						}

						// return SUCCESS here to prevent player from editing the sign, but do not prevent editing of
						// other signs which are not elevator signs
						return ActionResult.SUCCESS;
					} else if ("[DOWN]".equals(text.getString())) {

						// -1 to exclude the current sign, only search next sign from current-1 below
						Integer target = this.getVerticalWallSignDown(world, pos, pos.getY() - 1);
						if (target == null) {
							player.sendMessage(Text.literal("No lower lift level found."));
						} else {
							player.teleport(player.getX(), target - 1, player.getZ());
						}

						// return SUCCESS here to prevent player from editing the sign, but do not prevent editing of
						// other signs which are not elevator signs
						return ActionResult.SUCCESS;
					}
				}
			}
			return ActionResult.PASS;
		});
	}

	/**
	 Checks blocks above clicked sign for next elevator level
	 @param world - World instance
	 @param pos - Position of the sign which the user clicked on
	 @return target y of elevator level if found, null if no next level exists
	 */
	public Integer getVerticalWallSignUp(World world, BlockPos pos, Integer start) {
		for (int y = start; y < world.getTopY(); y++) {
			BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
			if (world.getBlockState(checkPos).getBlock() instanceof WallSignBlock) {
				Boolean targetSignIsElevator = this.SignIsElevator(world, checkPos);
				if (targetSignIsElevator == Boolean.TRUE) {
					return y;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 Checks blocks below clicked sign for next elevator level
	 @param world - World instance
	 @param pos - Position of the sign which the user clicked on
	 @return target y of elevator level if found, null if no next level exists
	 */
	public Integer getVerticalWallSignDown(World world, BlockPos pos, Integer start) {
		for (int y = start; y > world.getBottomY(); y--) {
			BlockPos checkPos = new BlockPos(pos.getX(), y, pos.getZ());
			if (world.getBlockState(checkPos).getBlock() instanceof WallSignBlock) {
				// check if sign is an elevator sign
				Boolean targetSignIsElevator = this.SignIsElevator(world, checkPos);
				if (targetSignIsElevator == Boolean.TRUE) {
					return y;
				} else {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 Checks whether a given sign is an elevator block or not (is one if text in first sign row is either "[UP]" or "[DOWN]")
	 @param world - World instance, used to access upper and lower blocks for elevator levels
	 @param pos - Position of the sign to check
	 @return true if sign is an elevator sign, false if it is not
	 */
	public Boolean SignIsElevator(World world, BlockPos pos) {
		BlockEntity blockEntity = world.getBlockEntity(pos);
		SignBlockEntity sign = (SignBlockEntity) blockEntity;
		SignText signText = sign.getText(true);
		Text text = signText.getMessage(0, true);

		if ("[UP]".equals(text.getString()) || "[DOWN]".equals(text.getString())) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}