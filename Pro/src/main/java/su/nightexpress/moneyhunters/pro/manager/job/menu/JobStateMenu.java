package su.nightexpress.moneyhunters.pro.manager.job.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenu;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.lang.LangManager;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.StringUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.api.job.JobState;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;
import su.nightexpress.moneyhunters.pro.manager.job.JobManager;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class JobStateMenu extends AbstractMenu<MoneyHunters> {

    private static final String PLACEHOLDER_AVAILABLE = "%available%";

    private final List<String> formatAvailable;
    private final List<String> formatUnavailable;
    private final Map<Player, UserJobData> cache;

    public JobStateMenu(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/job.state.yml"), "");
        this.formatAvailable = StringUtil.color(cfg.getStringList("Format.Available"));
        this.formatUnavailable = StringUtil.color(cfg.getStringList("Format.Unavailable"));
        this.cache = new WeakHashMap<>();

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                if (type2 == MenuItemType.RETURN) {
                    plugin.getJobManager().getJobListMenu().open(player, 1);
                }
                else this.onItemClickDefault(player, type2);
            }
            else if (type instanceof JobState state) {
                UserJobData data = this.cache.get(player);
                if (data == null) return;

                IJob<?> job = data.getJob();
                if (!job.isStateAllowed(state)) return;

                if (data.getState() == state) {
                    plugin.getJobManager().getJobListMenu().open(player, 1);
                    return;
                }

                int jobsMax = JobManager.getJobsAmountMax(player, state);
                int jobsHave = JobManager.getJobsAmount(player, state);
                if (jobsMax >= 0 && jobsHave >= jobsMax) {
                    plugin.getMessage(Lang.JOBS_STATE_CHANGE_ERROR_LIMIT)
                        .replace("%jobs_have%", jobsHave)
                        .replace(Placeholders.GENERIC_JOBS_LIMIT, jobsMax)
                        .replace(Placeholders.JOB_STATE, plugin.getLangManager().getEnum(state))
                        .send(player);
                    player.closeInventory();
                    return;
                }

                if (state != JobState.INACTIVE) {
                    if (data.getJobLevel() > job.getLevelMax(state)) {
                        plugin.getMessage(Lang.JOBS_STATE_CHANGE_ERROR_LEVEL)
                            .replace(Placeholders.JOB_NAME, job.getName())
                            .replace(Placeholders.JOB_STATE, plugin.getLangManager().getEnum(state))
                            .send(player);
                        player.closeInventory();
                        return;
                    }
                }

                data.setState(state);
                data.update();

                plugin.getMessage(Lang.JOBS_STATE_CHANGE_SUCCESS)
                    .replace(Placeholders.JOB_NAME, job.getName())
                    .replace(Placeholders.JOB_STATE, plugin.getLangManager().getEnum(state))
                    .send(player);
                player.closeInventory();
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }

        for (String sId : cfg.getSection("State")) {
            MenuItem menuItem = cfg.getMenuItem("State." + sId, JobState.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    public void open(@NotNull Player player, @NotNull UserJobData data) {
        this.cache.put(player, data);
        this.open(player, 1);
    }

    @Override
    public void onItemPrepare(@NotNull Player player, @NotNull MenuItem menuItem, @NotNull ItemStack item) {
        super.onItemPrepare(player, menuItem, item);

        UserJobData data = this.cache.get(player);
        if (data == null) return;

        MoneyUser user = plugin.getUserManager().getUserData(player);

        int jobsMax = JobManager.getJobsAmountMax(player, data.getState());
        if (menuItem.getType() instanceof JobState state) {
            ItemUtil.replaceLore(item, PLACEHOLDER_AVAILABLE, data.getJob().isStateAllowed(state) ? this.formatAvailable : this.formatUnavailable);
            ItemUtil.replace(item, str -> str.replace(Placeholders.GENERIC_JOBS_LIMIT, jobsMax >= 0 ? String.valueOf(jobsMax) : LangManager.getPlain(EngineLang.OTHER_INFINITY)));
        }
        ItemUtil.replace(item, user.replacePlaceholders());
        ItemUtil.replace(item, data.replacePlaceholders());
    }

    @Override
    public void onClose(@NotNull Player player, @NotNull InventoryCloseEvent e) {
        super.onClose(player, e);
        this.cache.remove(player);
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
