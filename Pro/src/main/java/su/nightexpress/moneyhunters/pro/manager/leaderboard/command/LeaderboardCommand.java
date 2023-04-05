package su.nightexpress.moneyhunters.pro.manager.leaderboard.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.GeneralCommand;
import su.nexmedia.engine.command.list.HelpSubCommand;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.MessageUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardManager;
import su.nightexpress.moneyhunters.pro.manager.leaderboard.LeaderboardType;

import java.util.List;
import java.util.Map;

public class LeaderboardCommand extends GeneralCommand<MoneyHunters> {

    private final LeaderboardManager leaderboardManager;

    public LeaderboardCommand(@NotNull LeaderboardManager leaderboardManager) {
        super(leaderboardManager.plugin(), new String[]{"leaderboard"}, Perms.COMMAND_LEADERBOARD);
        this.leaderboardManager = leaderboardManager;

        this.addDefaultCommand(new HelpSubCommand<>(plugin));
        if (this.leaderboardManager.getHologramHandler() != null) {
            this.addChildren(new LeaderboardHologramCommand(plugin, this.leaderboardManager.getHologramHandler()));
        }
        this.addChildren(new ListCommand(plugin));
    }

    @Override
    @NotNull
    public String getUsage() {
        return "";
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_LEADERBOARD_DESC).getLocalized();
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {

    }

    class ListCommand extends AbstractCommand<MoneyHunters> {

        public ListCommand(@NotNull MoneyHunters plugin) {
            super(plugin, new String[]{"list"}, Perms.COMMAND_LEADERBOARD_LIST);
        }

        @Override
        @NotNull
        public String getUsage() {
            return plugin.getMessage(Lang.COMMAND_LEADERBOARD_LIST_USAGE).getLocalized();
        }

        @Override
        @NotNull
        public String getDescription() {
            return plugin.getMessage(Lang.COMMAND_LEADERBOARD_LIST_DESC).getLocalized();
        }

        @Override
        public boolean isPlayerOnly() {
            return false;
        }

        @Override
        @NotNull
        public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
            if (arg == 2) {
                return CollectionsUtil.getEnumsList(LeaderboardType.class);
            }
            if (arg == 3) {
                return plugin.getJobManager().getJobIds();
            }
            return super.getTab(player, arg, args);
        }

        @Override
        protected void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
            if (args.length != 4) {
                this.printUsage(sender);
                return;
            }

            LeaderboardType boardType = CollectionsUtil.getEnum(args[2], LeaderboardType.class);
            if (boardType == null) {
                this.printUsage(sender);
                return;
            }

            IJob<?> job = plugin.getJobManager().getJobById(args[3]);
            if (job == null) {
                plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
                return;
            }

            leaderboardManager.formatLeaderList(
                plugin.getMessage(Lang.COMMAND_LEADERBOARD_LIST_FORMAT).asList(), boardType, job)
                .forEach(line -> MessageUtil.sendWithJSON(sender, job.replacePlaceholders().apply(line)));
        }
    }
}
