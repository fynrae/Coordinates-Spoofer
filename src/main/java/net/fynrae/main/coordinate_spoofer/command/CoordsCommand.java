package net.fynrae.main.coordinate_spoofer.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Random;
import java.util.Stack;

public class CoordsCommand {
    private static boolean spoofingEnabled = true;
    private static boolean updateWhenPlayerMove = false;
    private static Double staticX = null;
    private static Double staticY = null;
    private static Double staticZ = null;
    private static String traditionalOperator = null;
    private static Double traditionalValueX = null;
    private static Double traditionalValueY = null;
    private static Double traditionalValueZ = null;
    private static Double randomFromValueX = null;
    private static Double randomToValueX = null;
    private static Double randomFromValueY = null;
    private static Double randomToValueY = null;
    private static Double randomFromValueZ = null;
    private static Double randomToValueZ = null;

    private static Stack<Double> historyStaticX = new Stack<>();
    private static Stack<Double> historyStaticY = new Stack<>();
    private static Stack<Double> historyStaticZ = new Stack<>();
    private static Stack<String> historyTraditionalOperator = new Stack<>();
    private static Stack<Double> historyTraditionalValueX = new Stack<>();
    private static Stack<Double> historyTraditionalValueY = new Stack<>();
    private static Stack<Double> historyTraditionalValueZ = new Stack<>();
    private static Stack<Double> historyRandomFromValueX = new Stack<>();
    private static Stack<Double> historyRandomToValueX = new Stack<>();
    private static Stack<Double> historyRandomFromValueY = new Stack<>();
    private static Stack<Double> historyRandomToValueY = new Stack<>();
    private static Stack<Double> historyRandomFromValueZ = new Stack<>();
    private static Stack<Double> historyRandomToValueZ = new Stack<>();

    private static Stack<Double> redoStaticX = new Stack<>();
    private static Stack<Double> redoStaticY = new Stack<>();
    private static Stack<Double> redoStaticZ = new Stack<>();
    private static Stack<String> redoTraditionalOperator = new Stack<>();
    private static Stack<Double> redoTraditionalValueX = new Stack<>();
    private static Stack<Double> redoTraditionalValueY = new Stack<>();
    private static Stack<Double> redoTraditionalValueZ = new Stack<>();
    private static Stack<Double> redoRandomFromValueX = new Stack<>();
    private static Stack<Double> redoRandomToValueX = new Stack<>();
    private static Stack<Double> redoRandomFromValueY = new Stack<>();
    private static Stack<Double> redoRandomToValueY = new Stack<>();
    private static Stack<Double> redoRandomFromValueZ = new Stack<>();
    private static Stack<Double> redoRandomToValueZ = new Stack<>();

    private static final Random random = new Random();

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
                                                                DoubleArgumentType.getDouble(context, "value")))))))
                        .then(ClientCommandManager.literal("random")
                                .then(ClientCommandManager.argument("updateWhenPlayerMove", BoolArgumentType.bool())
                                        .then(ClientCommandManager.argument("coordinate", StringArgumentType.word())
                                                .then(ClientCommandManager.argument("fromValue", DoubleArgumentType.doubleArg())
                                                        .then(ClientCommandManager.argument("toValue", DoubleArgumentType.doubleArg())
                                                                .executes(context -> spoofRandomCoordinate(
                                                                        context.getSource(),
                                                                        BoolArgumentType.getBool(context, "updateWhenPlayerMove"),
                                                                        StringArgumentType.getString(context, "coordinate"),
                                                                        DoubleArgumentType.getDouble(context, "fromValue"),
                                                                        DoubleArgumentType.getDouble(context, "toValue"))))))))));
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
        randomFromValueX = null;
        randomToValueX = null;
        randomFromValueY = null;
        randomToValueY = null;
        randomFromValueZ = null;
        randomToValueZ = null;
        updateWhenPlayerMove = false;
        // Clear undo and redo stacks
        historyStaticX.clear();
        historyStaticY.clear();
        historyStaticZ.clear();
        historyTraditionalOperator.clear();
        historyTraditionalValueX.clear();
        historyTraditionalValueY.clear();
        historyTraditionalValueZ.clear();
        historyRandomFromValueX.clear();
        historyRandomToValueX.clear();
        historyRandomFromValueY.clear();
        historyRandomToValueY.clear();
        historyRandomFromValueZ.clear();
        historyRandomToValueZ.clear();
        redoStaticX.clear();
        redoStaticY.clear();
        redoStaticZ.clear();
        redoTraditionalOperator.clear();
        redoTraditionalValueX.clear();
        redoTraditionalValueY.clear();
        redoTraditionalValueZ.clear();
        redoRandomFromValueX.clear();
        redoRandomToValueX.clear();
        redoRandomFromValueY.clear();
        redoRandomToValueY.clear();
        redoRandomFromValueZ.clear();
        redoRandomToValueZ.clear();
        Text message = Text.literal("Coordinate spoofing values and history have been reset to true values!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int undoSpoofing(FabricClientCommandSource source) {
        if (historyStaticX.isEmpty() && historyTraditionalOperator.isEmpty() && historyRandomFromValueX.isEmpty()) {
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
        if (!historyRandomFromValueX.isEmpty()) {
            redoRandomFromValueX.push(randomFromValueX);
            randomFromValueX = historyRandomFromValueX.pop();
        }
        if (!historyRandomToValueX.isEmpty()) {
            redoRandomToValueX.push(randomToValueX);
            randomToValueX = historyRandomToValueX.pop();
        }
        if (!historyRandomFromValueY.isEmpty()) {
            redoRandomFromValueY.push(randomFromValueY);
            randomFromValueY = historyRandomFromValueY.pop();
        }
        if (!historyRandomToValueY.isEmpty()) {
            redoRandomToValueY.push(randomToValueY);
            randomToValueY = historyRandomToValueY.pop();
        }
        if (!historyRandomFromValueZ.isEmpty()) {
            redoRandomFromValueZ.push(randomFromValueZ);
            randomFromValueZ = historyRandomFromValueZ.pop();
        }
        if (!historyRandomToValueZ.isEmpty()) {
            redoRandomToValueZ.push(randomToValueZ);
            randomToValueZ = historyRandomToValueZ.pop();
        }
        Text message = Text.literal("Redo the last undone spoofing values!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int redoSpoofing(FabricClientCommandSource source) {
        if (redoStaticX.isEmpty() && redoTraditionalOperator.isEmpty() && redoRandomFromValueX.isEmpty()) {
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
        if (!redoRandomFromValueX.isEmpty()) {
            historyRandomFromValueX.push(randomFromValueX);
            randomFromValueX = redoRandomFromValueX.pop();
        }
        if (!redoRandomToValueX.isEmpty()) {
            historyRandomToValueX.push(randomToValueX);
            randomToValueX = redoRandomToValueX.pop();
        }
        if (!redoRandomFromValueY.isEmpty()) {
            historyRandomFromValueY.push(randomFromValueY);
            randomFromValueY = redoRandomFromValueY.pop();
        }
        if (!redoRandomToValueY.isEmpty()) {
            historyRandomToValueY.push(randomToValueY);
            randomToValueY = redoRandomToValueY.pop();
        }
        if (!redoRandomFromValueZ.isEmpty()) {
            historyRandomFromValueZ.push(randomFromValueZ);
            randomFromValueZ = redoRandomFromValueZ.pop();
        }
        if (!redoRandomToValueZ.isEmpty()) {
            historyRandomToValueZ.push(randomToValueZ);
            randomToValueZ = redoRandomToValueZ.pop();
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
        historyRandomFromValueX.push(randomFromValueX);
        historyRandomToValueX.push(randomToValueX);
        historyRandomFromValueY.push(randomFromValueY);
        historyRandomToValueY.push(randomToValueY);
        historyRandomFromValueZ.push(randomFromValueZ);
        historyRandomToValueZ.push(randomToValueZ);
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
        redoRandomFromValueX.clear();
        redoRandomToValueX.clear();
        redoRandomFromValueY.clear();
        redoRandomToValueY.clear();
        redoRandomFromValueZ.clear();
        redoRandomToValueZ.clear();
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
        redoRandomFromValueX.clear();
        redoRandomToValueX.clear();
        redoRandomFromValueY.clear();
        redoRandomToValueY.clear();
        redoRandomFromValueZ.clear();
        redoRandomToValueZ.clear();
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

    private static int spoofRandomCoordinate(FabricClientCommandSource source, boolean update, String coordinate, double fromValue, double toValue) {
        saveCurrentSpoofValues();
        redoStaticX.clear();
        redoStaticY.clear();
        redoStaticZ.clear();
        redoTraditionalOperator.clear();
        redoTraditionalValueX.clear();
        redoTraditionalValueY.clear();
        redoTraditionalValueZ.clear();
        redoRandomFromValueX.clear();
        redoRandomToValueX.clear();
        redoRandomFromValueY.clear();
        redoRandomToValueY.clear();
        redoRandomFromValueZ.clear();
        redoRandomToValueZ.clear();

        updateWhenPlayerMove = update;

        switch (coordinate.toLowerCase()) {
            case "x":
                randomFromValueX = fromValue;
                randomToValueX = toValue;
                break;
            case "y":
                randomFromValueY = fromValue;
                randomToValueY = toValue;
                break;
            case "z":
                randomFromValueZ = fromValue;
                randomToValueZ = toValue;
                break;
            default:
                Text message = Text.literal("Invalid coordinate! Use x, y, or z.").formatted(Formatting.RED);
                MinecraftClient.getInstance().player.sendMessage(message, false);
                return Command.SINGLE_SUCCESS;
        }

        Text message = Text.literal("Random spoof for " + coordinate.toUpperCase() + " coordinate set to random value between " + fromValue + " and " + toValue + (update ? " (updates when player moves)" : "") + "!").formatted(Formatting.GREEN);
        MinecraftClient.getInstance().player.sendMessage(message, false);
        return Command.SINGLE_SUCCESS;
    }

    private static double getRandomValue(double fromValue, double toValue) {
        return fromValue + (toValue - fromValue) * random.nextDouble();
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

    public static Double getRandomFromValueX() {
        return randomFromValueX;
    }

    public static Double getRandomToValueX() {
        return randomToValueX;
    }

    public static Double getRandomFromValueY() {
        return randomFromValueY;
    }

    public static Double getRandomToValueY() {
        return randomToValueY;
    }

    public static Double getRandomFromValueZ() {
        return randomFromValueZ;
    }

    public static Double getRandomToValueZ() {
        return randomToValueZ;
    }

    public static boolean isUpdateWhenPlayerMove() {
        return updateWhenPlayerMove;
    }
}
