package su.nightexpress.moneyhunters.pro.manager.job.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.config.JYML;
import su.nexmedia.engine.api.menu.AbstractMenuAuto;
import su.nexmedia.engine.api.menu.MenuClick;
import su.nexmedia.engine.api.menu.MenuItem;
import su.nexmedia.engine.api.menu.MenuItemType;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.Colorizer;
import su.nexmedia.engine.utils.ItemUtil;
import su.nexmedia.engine.utils.TimeUtil;
import su.nightexpress.moneyhunters.pro.MoneyHunters;
import su.nightexpress.moneyhunters.pro.Perms;
import su.nightexpress.moneyhunters.pro.Placeholders;
import su.nightexpress.moneyhunters.pro.api.job.IJob;
import su.nightexpress.moneyhunters.pro.config.Lang;
import su.nightexpress.moneyhunters.pro.data.object.MoneyUser;
import su.nightexpress.moneyhunters.pro.data.object.UserJobData;

import java.util.ArrayList;
import java.util.List;

public class JobListMenu extends AbstractMenuAuto<MoneyHunters, UserJobData> {

    private final String       formatAvailableName;
    private final List<String> formatAvailableLore;
    private final String       formatLockedPermName;
    private final List<String> formatLockedPermLore;
    private final int[]        jobSlots;

    public JobListMenu(@NotNull MoneyHunters plugin) {
        super(plugin, JYML.loadOrExtract(plugin, "/menu/job.list.yml"), "");

        this.jobSlots = cfg.getIntArray("Job_Slots");
        this.formatAvailableName = Colorizer.apply(cfg.getString("Format.Available.Name", ""));
        this.formatAvailableLore = Colorizer.apply(cfg.getStringList("Format.Available.Lore"));
        this.formatLockedPermName = Colorizer.apply(cfg.getString("Format.Locked_Permission.Name", ""));
        this.formatLockedPermLore = Colorizer.apply(cfg.getStringList("Format.Locked_Permission.Lore"));

        MenuClick click = (player, type, e) -> {
            if (type instanceof MenuItemType type2) {
                this.onItemClickDefault(player, type2);
            }
        };

        for (String sId : cfg.getSection("Content")) {
            MenuItem menuItem = cfg.getMenuItem("Content." + sId, MenuItemType.class);

            if (menuItem.getType() != null) {
                menuItem.setClickHandler(click);
            }
            this.addItem(menuItem);
        }
    }

    @Override
    @NotNull
    protected List<UserJobData> getObjects(@NotNull Player player) {
        return new ArrayList<>(plugin.getUserManager().getUserData(player).getJobData().values());
    }

    @Override
    protected @NotNull ItemStack getObjectStack(@NotNull Player player, @NotNull UserJobData data) {
        IJob<?> job = data.getJob();
        ItemStack item = job.getIcon();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        boolean hasAccess = job.hasPermission(player);
        if (hasAccess) {
            meta.setDisplayName(this.formatAvailableName);
            meta.setLore(this.formatAvailableLore);
        }
        else {
            meta.setDisplayName(this.formatLockedPermName);
            meta.setLore(this.formatLockedPermLore);
        }
        item.setItemMeta(meta);

        MoneyUser user = plugin.getUserManager().getUserData(player);
        ItemUtil.replace(item, data.replacePlaceholders(user));

        return item;
    }

    @Override
    protected @NotNull MenuClick getObjectClick(@NotNull Player player, @NotNull UserJobData data) {
        return (player1, type, e) -> {
            IJob<?> job = data.getJob();
            if (!job.hasPermission(player1)) {
                plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(player1);
                return;
            }
            if (e.getClick() == ClickType.DROP) {
                if (!player1.hasPermission(Perms.COMMAND_RESET)) {
                    plugin.getMessage(EngineLang.ERROR_PERMISSION_DENY).send(player1);
                    return;
                }
                plugin.getJobManager().getJobResetConfirmMenu().open(player1, data);
                return;
            }
            if (e.isRightClick()) {
                if (job.getStateAllowed().isEmpty()) {
                    plugin.getMessage(Lang.JOBS_STATE_CHANGE_ERROR_NOTHING).send(player1);
                    return;
                }

                MoneyUser user = plugin.getUserManager().getUserData(player1);
                long cooldown = user.getJobStateCooldown(job);
                if (cooldown != 0) {
                    plugin.getMessage(Lang.JOBS_STATE_CHANGE_ERROR_COOLDOWN)
                        .replace(Placeholders.GENERIC_TIME, TimeUtil.formatTimeLeft(cooldown))
                        .send(player1);
                    return;
                }

                plugin.getJobManager().getJobStateMenu().open(player1, data);
                return;
            }
            job.getObjectivesMenu().open(player1, 1);
        };
    }

    @Override
    protected int[] getObjectSlots() {
        return this.jobSlots;
    }

    @Override
    public boolean cancelClick(@NotNull InventoryClickEvent e, @NotNull SlotType slotType) {
        return true;
    }
}
