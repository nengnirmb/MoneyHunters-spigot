package su.nightexpress.moneyhunters.pro.command.base;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

import java.util.List;
import java.util.Map;

public class ResetCommand extends AbstractCommand<MoneyHunters> {

    public ResetCommand(@NotNull MoneyHunters plugin) {
        super(plugin, new String[]{"reset"}, Perms.COMMAND_RESET);
    }

    @Override
    @NotNull
    public String getUsage() {
        return plugin.getMessage(Lang.COMMAND_RESET_USAGE).getLocalized();
    }

    @Override
    @NotNull
    public String getDescription() {
        return plugin.getMessage(Lang.COMMAND_RESET_DESC).getLocalized();
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getJobManager().getJobIds();
        }
        if (arg == 2 && player.hasPermission(Perms.COMMAND_RESET_OTHERS)) {
            return CollectionsUtil.playerNames(player);
        }
        return super.getTab(player, arg, args);
    }

    @Override
    public boolean isPlayerOnly() {
        return false;
    }

    @Override
    public void onExecute(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args, @NotNull Map<String, String> flags) {
        if (args.length < 2) {
            this.printUsage(sender);
            return;
        }
        if (args.length >= 3 && !sender.hasPermission(Perms.COMMAND_RESET_OTHERS)) {
            this.errorPermission(sender);
            return;
        }

        IJob<?> job = plugin.getJobManager().getJobById(args[1]);
        if (job == null) {
            plugin.getMessage(Lang.JOB_ERROR_INVALID_JOB).send(sender);
            return;
        }

        String pName = args.length >= 3 ? args[2] : sender.getName();
        MoneyUser user = plugin.getUserManager().getUserData(pName);
        if (user == null) {
            this.errorPlayer(sender);
            return;
        }

        UserJobData data = user.getJobData(job);
        data.reset();

        if (!sender.getName().equalsIgnoreCase(user.getName())) {
            plugin.getMessage(Lang.COMMAND_RESET_DONE)
                .replace(data.replacePlaceholders())
                .replace("%player%", user.getName())
                .send(sender);
        }

        Player target = user.getPlayer();
        if (target != null) {
            plugin.getMessage(Lang.JOBS_RESET_SUCCESS).replace(data.replacePlaceholders()).send(target);
        }
    }
}
