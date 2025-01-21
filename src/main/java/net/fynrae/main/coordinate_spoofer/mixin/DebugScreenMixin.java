package net.fynrae.main.coordinate_spoofer.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.fynrae.main.coordinate_spoofer.command.CoordsCommand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class DebugScreenMixin {
    private double lastPlayerX = Double.NaN;
    private double lastPlayerY = Double.NaN;
    private double lastPlayerZ = Double.NaN;
    private double spoofedX = Double.NaN;
    private double spoofedY = Double.NaN;
    private double spoofedZ = Double.NaN;

    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void onGetLeftText(CallbackInfoReturnable<List<String>> info) {
        List<String> leftText = info.getReturnValue();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player != null && CoordsCommand.isSpoofingEnabled()) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            boolean playerMoved = player.getX() != lastPlayerX || player.getY() != lastPlayerY || player.getZ() != lastPlayerZ;

            if (CoordsCommand.getStaticX() != null) {
                x = CoordsCommand.getStaticX();
            } else if (CoordsCommand.getTraditionalValueX() != null && CoordsCommand.getTraditionalOperator() != null) {
                x = applyOperation(x, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueX());
            } else if (CoordsCommand.getRandomFromValueX() != null && CoordsCommand.getRandomToValueX() != null) {
                if (CoordsCommand.isUpdateWhenPlayerMove() && playerMoved) {
                    x = CoordsCommand.getRandomFromValueX() + (CoordsCommand.getRandomToValueX() - CoordsCommand.getRandomFromValueX()) * Math.random();
                } else if (Double.isNaN(spoofedX)) {
                    x = CoordsCommand.getRandomFromValueX() + (CoordsCommand.getRandomToValueX() - CoordsCommand.getRandomFromValueX()) * Math.random();
                } else {
                    x = spoofedX;
                }
            }

            if (CoordsCommand.getStaticY() != null) {
                y = CoordsCommand.getStaticY();
            } else if (CoordsCommand.getTraditionalValueY() != null && CoordsCommand.getTraditionalOperator() != null) {
                y = applyOperation(y, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueY());
            } else if (CoordsCommand.getRandomFromValueY() != null && CoordsCommand.getRandomToValueY() != null) {
                if (CoordsCommand.isUpdateWhenPlayerMove() && playerMoved) {
                    y = CoordsCommand.getRandomFromValueY() + (CoordsCommand.getRandomToValueY() - CoordsCommand.getRandomFromValueY()) * Math.random();
                } else if (Double.isNaN(spoofedY)) {
                    y = CoordsCommand.getRandomFromValueY() + (CoordsCommand.getRandomToValueY() - CoordsCommand.getRandomFromValueY()) * Math.random();
                } else {
                    y = spoofedY;
                }
            }

            if (CoordsCommand.getStaticZ() != null) {
                z = CoordsCommand.getStaticZ();
            } else if (CoordsCommand.getTraditionalValueZ() != null && CoordsCommand.getTraditionalOperator() != null) {
                z = applyOperation(z, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueZ());
            } else if (CoordsCommand.getRandomFromValueZ() != null && CoordsCommand.getRandomToValueZ() != null) {
                if (CoordsCommand.isUpdateWhenPlayerMove() && playerMoved) {
                    z = CoordsCommand.getRandomFromValueZ() + (CoordsCommand.getRandomToValueZ() - CoordsCommand.getRandomFromValueZ()) * Math.random();
                } else if (Double.isNaN(spoofedZ)) {
                    z = CoordsCommand.getRandomFromValueZ() + (CoordsCommand.getRandomToValueZ() - CoordsCommand.getRandomFromValueZ()) * Math.random();
                } else {
                    z = spoofedZ;
                }
            }

            if (playerMoved) {
                spoofedX = x;
                spoofedY = y;
                spoofedZ = z;
            }

            lastPlayerX = player.getX();
            lastPlayerY = player.getY();
            lastPlayerZ = player.getZ();

            int blockX = (int) x;
            int blockY = (int) y;
            int blockZ = (int) z;
            int chunkX = blockX % 16;
            int chunkY = blockY % 16;
            int chunkZ = blockZ % 16;

            for (int i = 0; i < leftText.size(); i++) {
                String line = leftText.get(i);
                if (line.startsWith("XYZ:")) {
                    leftText.set(i, String.format("XYZ: %.3f / %.5f / %.3f", x, y, z));
                } else if (line.startsWith("Block:")) {
                    leftText.set(i, String.format("Block: %d %d %d", blockX, blockY, blockZ));
                } else if (line.startsWith("Chunk:")) {
                    leftText.set(i, String.format("Chunk: %d %d %d", chunkX, chunkY, chunkZ));
                }
            }
        }
        info.setReturnValue(leftText);
    }

    private double applyOperation(double original, String operator, double value) {
        switch (operator) {
            case "plus":
                return original + value;
            case "minus":
                return original - value;
            case "multiply":
                return original * value;
            case "divide":
                return original / value;
            case "power":
                return Math.pow(original, value);
            case "modulus":
                return original % value;
            default:
                return original;
        }
    }
}
