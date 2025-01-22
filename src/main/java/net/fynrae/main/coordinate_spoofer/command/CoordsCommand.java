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

import java.util.Stack;

public class CoordsCommand {
    private static boolean spoofingEnabled = true;
    private static Double staticX = null;
    private static Double staticY = null;
    private static Double staticZ = null;
    private static String traditionalOperator = null;
    private static Double traditionalValueX = null;
    private static Double traditionalValueY = null;
    private static Double traditionalValueZ = null;

    private static Stack<Double> historyStaticX = new Stack<>();
    private static Stack<Double> historyStaticY = new Stack<>();
    private static Stack<Double> historyStaticZ = new Stack<>();
    private static Stack<String> historyTraditionalOperator = new Stack<>();
    private static Stack<Double> historyTraditionalValueX = new Stack<>();
    private static Stack<Double> historyTraditionalValueY = new Stack<>();
    private static Stack<Double> historyTraditionalValueZ = new Stack<>();

    private static Stack<Double> redoStaticX = new Stack<>();
    private static Stack<Double> redoStaticY = new Stack<>();
    private static Stack<Double> redoStaticZ = new Stack<>();
    private static Stack<String> redoTraditionalOperator = new Stack<>();
    private static Stack<Double> redoTraditionalValueX = new Stack<>();
    private static Stack<Double> redoTraditionalValueY = new Stack<>();
    private static Stack<Double> redoTraditionalValueZ = new Stack<>();

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommandManager.literal("cds")
                .then(ClientCommandManager.literal("coords")
                        .executes(context -> executeCoords(context.getSource())))
                .then(ClientCommandManager.literal("toggle")
                        .executes(context -> toggleSpoofing(context.getSource())))
                .then(ClientCommandManager.literal("reset")
                        .executes(context -> resetSpoofing(context.getSource())))
                .then(ClientCommandManager.literal("undo")
                        .executes(context -> undoSpoofing(context.getSource())))
                .then(ClientCommandManager.literal("redo")
                        .executes(context -> redoSpoofing(context.getSource())))
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
        saveCurrentSpoofValues();
        spoofingEnabled = !spoofingEnabled;
        Text message = Text.literal("Coordinate spoofing " + (spoofingEnabled ? "enabled" : "disabled") + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int resetSpoofing(FabricClientCommandSource source) {
        saveCurrentSpoofValues();
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

    private static int undoSpoofing(FabricClientCommandSource source) {
        if (historyStaticX.isEmpty() && historyTraditionalOperator.isEmpty()) {
            Text message = Text.literal("No undo actions available!").formatted(Formatting.RED);
            MinecraftClient.getInstance().player.sendMessage(message, false);
            return Command.SINGLE_SUCCESS;
        }
        if (!historyStaticX.isEmpty()) {
            redoStaticX.push(staticX);
            staticX = historyStaticX.pop();
        }
        if (!historyStaticY.isEmpty()) {
            redoStaticY.push(staticY);
            staticY = historyStaticY.pop();
        }
        if (!historyStaticZ.isEmpty()) {
            redoStaticZ.push(staticZ);
            staticZ = historyStaticZ.pop();
        }
        if (!historyTraditionalOperator.isEmpty()) {
            redoTraditionalOperator.push(traditionalOperator);
            traditionalOperator = historyTraditionalOperator.pop();
        }
        if (!historyTraditionalValueX.isEmpty()) {
            redoTraditionalValueX.push(traditionalValueX);
            traditionalValueX = historyTraditionalValueX.pop();
        }
        if (!historyTraditionalValueY.isEmpty()) {
            redoTraditionalValueY.push(traditionalValueY);
            traditionalValueY = historyTraditionalValueY.pop();
        }
        if (!historyTraditionalValueZ.isEmpty()) {
            redoTraditionalValueZ.push(traditionalValueZ);
            traditionalValueZ = historyTraditionalValueZ.pop();
        }
        Text message = Text.literal("Coordinate spoofing values have been reverted to the previous spoofed values!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int redoSpoofing(FabricClientCommandSource source) {
        if (redoStaticX.isEmpty() && redoTraditionalOperator.isEmpty()) {
            Text message = Text.literal("No redo actions available!").formatted(Formatting.RED);
            MinecraftClient.getInstance().player.sendMessage(message, false);
            return Command.SINGLE_SUCCESS;
        }
        if (!redoStaticX.isEmpty()) {
            historyStaticX.push(staticX);
            staticX = redoStaticX.pop();
        }
        if (!redoStaticY.isEmpty()) {
            historyStaticY.push(staticY);
            staticY = redoStaticY.pop();
        }
        if (!redoStaticZ.isEmpty()) {
            historyStaticZ.push(staticZ);
            staticZ = redoStaticZ.pop();
        }
        if (!redoTraditionalOperator.isEmpty()) {
            historyTraditionalOperator.push(traditionalOperator);
            traditionalOperator = redoTraditionalOperator.pop();
        }
        if (!redoTraditionalValueX.isEmpty()) {
            historyTraditionalValueX.push(traditionalValueX);
            traditionalValueX = redoTraditionalValueX.pop();
        }
        if (!redoTraditionalValueY.isEmpty()) {
            historyTraditionalValueY.push(traditionalValueY);
            traditionalValueY = redoTraditionalValueY.pop();
        }
        if (!redoTraditionalValueZ.isEmpty()) {
            historyTraditionalValueZ.push(traditionalValueZ);
            traditionalValueZ = redoTraditionalValueZ.pop();
        }
        Text message = Text.literal("Redo the last undone spoofing values!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static void saveCurrentSpoofValues() {
        historyStaticX.push(staticX);
        historyStaticY.push(staticY);
        historyStaticZ.push(staticZ);
        historyTraditionalOperator.push(traditionalOperator);
        historyTraditionalValueX.push(traditionalValueX);
        historyTraditionalValueY.push(traditionalValueY);
        historyTraditionalValueZ.push(traditionalValueZ);
    }

    private static int spoofStaticCoordinate(FabricClientCommandSource source, String coordinate, double value) {
        saveCurrentSpoofValues();
        redoStaticX.clear();
        redoStaticY.clear();
        redoStaticZ.clear();
        redoTraditionalOperator.clear();
        redoTraditionalValueX.clear();
        redoTraditionalValueY.clear();
        redoTraditionalValueZ.clear();
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
                Text message = Text.literal("Invalid coordinate! Use x, y, or z.").formatted(Formatting.RED);
                MinecraftClient.getInstance().player.sendMessage(message, false);
                return Command.SINGLE_SUCCESS;
        }
        Text message = Text.literal("Static " + coordinate.toUpperCase() + " coordinate set to " + value + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int spoofTraditionalCoordinate(FabricClientCommandSource source, String coordinate, String operator, double value) {
        saveCurrentSpoofValues();
        redoStaticX.clear();
        redoStaticY.clear();
        redoStaticZ.clear();
        redoTraditionalOperator.clear();
        redoTraditionalValueX.clear();
        redoTraditionalValueY.clear();
        redoTraditionalValueZ.clear();
        switch (coordinate.toLowerCase()) {
            case "x":
                traditionalValueX = applyOperation(traditionalValueX != null ? traditionalValueX : 0, operator, value);
                break;
            case "y":
                traditionalValueY = applyOperation(traditionalValueY != null ? traditionalValueY : 0, operator, value);
                break;
            case "z":
                traditionalValueZ = applyOperation(traditionalValueZ != null ? traditionalValueZ : 0, operator, value);
                break;
            default:
                Text message = Text.literal("Invalid coordinate! Use x, y, or z.").formatted(Formatting.RED);
                MinecraftClient.getInstance().player.sendMessage(message, false);
                return Command.SINGLE_SUCCESS;
        }
        traditionalOperator = operator;
        Text message = Text.literal("Traditional spoof for " + coordinate.toUpperCase() + " coordinate set to: " + coordinate + " " + operator + " " + value + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static double applyOperation(double original, String operator, double value) {
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
