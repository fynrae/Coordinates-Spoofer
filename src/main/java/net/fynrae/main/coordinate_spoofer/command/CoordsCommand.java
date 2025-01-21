package net.fynrae.main.coordinate_spoofer.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class CoordsCommand {
    private static boolean spoofingEnabled = true;
    private static Double staticX = null;
    private static Double staticY = null;
    private static Double staticZ = null;
    private static String traditionalOperator = null;
    private static Double traditionalValueX = null;
    private static Double traditionalValueY = null;
    private static Double traditionalValueZ = null;

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cds")
                .then(ClientCommandManager.literal("coords")
                        .executes(context -> executeCoords(context.getSource())))
                .then(ClientCommandManager.literal("toggle")
                        .executes(context -> toggleSpoofing(context.getSource())))
                .then(ClientCommandManager.literal("reset")
                        .executes(context -> resetSpoofing(context.getSource())))
                .then(ClientCommandManager.literal("spoof")
                        .then(ClientCommandManager.literal("static")
                                .then(ClientCommandManager.argument("coordinate", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg())
                                                .executes(context -> spoofStaticCoordinate(
                                                        context.getSource(),
                                                        StringArgumentType.getString(context, "coordinate"),
                                                        DoubleArgumentType.getDouble(context, "value"))))))
                        .then(ClientCommandManager.literal("traditional")
                                .then(ClientCommandManager.argument("coordinate", StringArgumentType.word())
                                        .then(ClientCommandManager.argument("operator", StringArgumentType.word())
                                                .suggests((context, builder) -> {
                                                    builder.suggest("plus");
                                                    builder.suggest("minus");
                                                    builder.suggest("multiply");
                                                    builder.suggest("divide");
                                                    builder.suggest("power");
                                                    builder.suggest("modulus");
                                                    return builder.buildFuture();
                                                })
                                                .then(ClientCommandManager.argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(context -> spoofTraditionalCoordinate(
                                                                context.getSource(),
                                                                StringArgumentType.getString(context, "coordinate"),
                                                                StringArgumentType.getString(context, "operator"),
                                                                DoubleArgumentType.getDouble(context, "value")))))))));
    }

    private static int executeCoords(FabricClientCommandSource source) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            Text message = Text.literal(String.format("Your true coordinates are: X: %.2f, Y: %.2f, Z: %.2f", x, y, z)).formatted(Formatting.GREEN);
            player.sendMessage(message, false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int toggleSpoofing(FabricClientCommandSource source) {
        spoofingEnabled = !spoofingEnabled;
        Text message = Text.literal("Coordinate spoofing " + (spoofingEnabled ? "enabled" : "disabled") + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetSpoofing(FabricClientCommandSource source) {
        staticX = null;
        staticY = null;
        staticZ = null;
        traditionalOperator = null;
        traditionalValueX = null;
        traditionalValueY = null;
        traditionalValueZ = null;
        Text message = Text.literal("Coordinate spoofing values have been reset to true values!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int spoofStaticCoordinate(FabricClientCommandSource source, String coordinate, double value) {
        switch (coordinate.toLowerCase()) {
            case "x":
                staticX = value;
                break;
            case "y":
                staticY = value;
                break;
            case "z":
                staticZ = value;
                break;
            default:
                return Command.SINGLE_SUCCESS;
        }
        Text message = Text.literal("Static " + coordinate.toUpperCase() + " coordinate set to " + value + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int spoofTraditionalCoordinate(FabricClientCommandSource source, String coordinate, String operator, double value) {
        switch (coordinate.toLowerCase()) {
            case "x":
                traditionalValueX = value;
                break;
            case "y":
                traditionalValueY = value;
                break;
            case "z":
                traditionalValueZ = value;
                break;
            default:
                return Command.SINGLE_SUCCESS;
        }
        traditionalOperator = operator;
        Text message = Text.literal("Traditional spoof for " + coordinate.toUpperCase() + " coordinate set to: " + coordinate + " " + operator + " " + value + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    public static boolean isSpoofingEnabled() {
        return spoofingEnabled;
    }

    public static Double getStaticX() {
        return staticX;
    }

    public static Double getStaticY() {
        return staticY;
    }

    public static Double getStaticZ() {
        return staticZ;
    }

    public static String getTraditionalOperator() {
        return traditionalOperator;
    }

    public static Double getTraditionalValueX() {
        return traditionalValueX;
    }

    public static Double getTraditionalValueY() {
        return traditionalValueY;
    }

    public static Double getTraditionalValueZ() {
        return traditionalValueZ;
    }
}
