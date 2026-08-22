package dev.wechirok.betterselectivecombat.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.wechirok.betterselectivecombat.BetterSelectiveCombat;
import dev.wechirok.betterselectivecombat.lang.Translations;
import dev.wechirok.betterselectivecombat.selection.WeaponId;
import dev.wechirok.betterselectivecombat.selection.WeaponSelectionService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public final class BscCommands {
    private static final int PAGE_SIZE = 8;

    private BscCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bsc")
                .executes(BscCommands::showVersion)
                .then(Commands.literal("help")
                        .executes(BscCommands::showHelp))
                .then(Commands.literal("disable")
                        .then(Commands.argument("weapon_id", StringArgumentType.word())
                                .executes(BscCommands::disable)))
                .then(Commands.literal("enable")
                        .then(Commands.argument("weapon_id", StringArgumentType.word())
                                .executes(BscCommands::enable)))
                .then(Commands.literal("status")
                        .then(Commands.argument("weapon_id", StringArgumentType.word())
                                .executes(BscCommands::status)))
                .then(Commands.literal("list")
                        .executes(context -> list(context, 1))
                        .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(context -> list(context, IntegerArgumentType.getInteger(context, "page")))))
                .then(Commands.literal("reload")
                        .executes(BscCommands::reload)));
    }

    private static int showVersion(CommandContext<CommandSourceStack> context) {
        success(context.getSource(), BetterSelectiveCombat.NAME + " " + BetterSelectiveCombat.VERSION);
        return 1;
    }

    private static int showHelp(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        success(source, text(source, "bsc.help.header"));
        success(source, "/bsc " + text(source, "bsc.help.root"));
        success(source, "/bsc help " + text(source, "bsc.help.help"));
        success(source, "/bsc disable <weapon_id> " + text(source, "bsc.help.disable"));
        success(source, "/bsc enable <weapon_id> " + text(source, "bsc.help.enable"));
        success(source, "/bsc status <weapon_id> " + text(source, "bsc.help.status"));
        success(source, "/bsc list [page] " + text(source, "bsc.help.list"));
        success(source, "/bsc reload " + text(source, "bsc.help.reload"));
        return 1;
    }

    private static int disable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!canManage(source)) {
            return failure(source, text(source, "bsc.error.permission"));
        }
        String id = WeaponId.normalize(StringArgumentType.getString(context, "weapon_id"));
        WeaponSelectionService.ChangeResult result = BetterSelectiveCombat.selections().disable(id);
        return switch (result) {
            case CHANGED -> changed(source, "bsc.disable.success", id);
            case UNCHANGED -> failure(source, text(source, "bsc.disable.unchanged", id));
            case INVALID_ID -> failure(source, text(source, "bsc.error.invalid_id", id));
            case WRITE_FAILED -> failure(source, text(source, "bsc.error.write"));
        };
    }

    private static int enable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!canManage(source)) {
            return failure(source, text(source, "bsc.error.permission"));
        }
        String id = WeaponId.normalize(StringArgumentType.getString(context, "weapon_id"));
        WeaponSelectionService.ChangeResult result = BetterSelectiveCombat.selections().enable(id);
        return switch (result) {
            case CHANGED -> changed(source, "bsc.enable.success", id);
            case UNCHANGED -> failure(source, text(source, "bsc.enable.unchanged", id));
            case INVALID_ID -> failure(source, text(source, "bsc.error.invalid_id", id));
            case WRITE_FAILED -> failure(source, text(source, "bsc.error.write"));
        };
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String id = WeaponId.normalize(StringArgumentType.getString(context, "weapon_id"));
        if (!WeaponId.isValid(id)) {
            return failure(source, text(source, "bsc.error.invalid_id", id));
        }
        String key = BetterSelectiveCombat.selections().isDisabled(id) ? "bsc.status.disabled" : "bsc.status.enabled";
        success(source, text(source, key, id));
        return 1;
    }

    private static int list(CommandContext<CommandSourceStack> context, int page) {
        CommandSourceStack source = context.getSource();
        List<String> weapons = BetterSelectiveCombat.selections().sortedSnapshot();
        if (weapons.isEmpty()) {
            success(source, text(source, "bsc.list.empty"));
            return 1;
        }
        int pages = (weapons.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (page > pages) {
            return failure(source, text(source, "bsc.list.invalid_page", page, pages));
        }
        success(source, text(source, "bsc.list.header", page, pages));
        int from = (page - 1) * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, weapons.size());
        for (String weapon : weapons.subList(from, to)) {
            success(source, weapon);
        }
        return 1;
    }

    private static int reload(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        if (!canManage(source)) {
            return failure(source, text(source, "bsc.error.permission"));
        }
        if (!BetterSelectiveCombat.selections().reload()) {
            return failure(source, text(source, "bsc.reload.failed"));
        }
        success(source, text(source, "bsc.reload.success"));
        success(source, text(source, "bsc.reconnect"));
        return 1;
    }

    private static int changed(CommandSourceStack source, String key, String weaponId) {
        success(source, text(source, key, weaponId));
        success(source, text(source, "bsc.reconnect"));
        return 1;
    }

    private static boolean canManage(CommandSourceStack source) {
        return PlatformPermissions.canManage(source);
    }

    private static String text(CommandSourceStack source, String key, Object... arguments) {
        Translations translations = BetterSelectiveCombat.translations();
        return translations.text(language(source), key, arguments);
    }

    private static String language(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        return player == null ? Translations.DEFAULT_LANGUAGE : player.clientInformation().language();
    }

    private static void success(CommandSourceStack source, String message) {
        source.sendSuccess(() -> Component.literal(message), false);
    }

    private static int failure(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message));
        return 0;
    }
}
