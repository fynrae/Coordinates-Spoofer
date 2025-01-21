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
    @Inject(method = "getLeftText", at = @At("RETURN"), cancellable = true)
    private void onGetLeftText(CallbackInfoReturnable<List<String>> info) {
        List<String> leftText = info.getReturnValue();
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player != null && CoordsCommand.isSpoofingEnabled()) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();

            if (CoordsCommand.getStaticX() != null) {
                x = CoordsCommand.getStaticX();
            } else if (CoordsCommand.getTraditionalValueX() != null) {
                x = applyOperation(x, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueX());
            }

            if (CoordsCommand.getStaticY() != null) {
                y = CoordsCommand.getStaticY();
            } else if (CoordsCommand.getTraditionalValueY() != null) {
                y = applyOperation(y, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueY());
            }

            if (CoordsCommand.getStaticZ() != null) {
                z = CoordsCommand.getStaticZ();
            } else if (CoordsCommand.getTraditionalValueZ() != null) {
                z = applyOperation(z, CoordsCommand.getTraditionalOperator(), CoordsCommand.getTraditionalValueZ());
            }

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
        return switch (operator) {
            case "plus" -> original + value;
            case "minus" -> original - value;
            case "multiply" -> original * value;
            case "divide" -> original / value;
            case "power" -> Math.pow(original, value);
            case "modulus" -> original % value;
            default -> original;
        };
    }
}
