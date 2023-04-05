package su.nightexpress.moneyhunters.basic.api.booster;

import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.manager.IPlaceholder;
import su.nexmedia.engine.utils.Placeholders;
import su.nightexpress.moneyhunters.basic.api.job.IJob;

import java.util.Set;

public interface IBooster extends IPlaceholder {

    @NotNull String getId();

    @NotNull Set<String> getJobs();

    default boolean isApplicable(@NotNull IJob<?> job) {
        return this.getJobs().contains(job.getId()) || this.getJobs().contains(Placeholders.WILDCARD);
    }

    double getMoneyModifier();

    double getExpModifier();

    default double getMoneyPercent() {
        return this.getMoneyModifier() * 100D - 100D;
    }

    default double getExpPercent() {
        return this.getExpModifier() * 100D - 100D;
    }

    default boolean isActive() {
        return !this.isAwaiting() && !this.isExpired();
    }

    boolean isExpired();

    boolean isAwaiting();

    @NotNull BoosterType getType();

}
