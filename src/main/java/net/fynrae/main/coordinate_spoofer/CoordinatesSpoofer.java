package net.fynrae.main.coordinate_spoofer;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.client.MinecraftClient;
import net.fynrae.main.coordinate_spoofer.command.CoordsCommand;

public class CoordinatesSpoofer implements ClientModInitializer {
	public static final String MOD_ID = "coordinates-spoofer";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeClient() {
		LOGGER.info("Initializing Coordinate Spoofer Mod");

		// Registering client play connection events
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			ClientPlayerEntity player = client.player;
			if (player != null) {
				player.sendMessage(Text.literal("Coordinates Spoofer is activated!").formatted(Formatting.GREEN), false);
			}
		});

		// Register custom commands
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			CoordsCommand.register(dispatcher);
		});

		LOGGER.info("Custom command registration complete");
	}
}
