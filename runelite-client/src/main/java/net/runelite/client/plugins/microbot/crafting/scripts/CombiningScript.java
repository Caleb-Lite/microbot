package net.runelite.client.plugins.microbot.crafting.scripts;

import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.CraftingConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class CombiningScript extends Script {

    @Inject
    private CraftingConfig config;

    public static double version = 1.0;

    private boolean isCombining = false;

    public void run(CraftingConfig config) {
        this.config = config;

        Keybind combinationDialogKey = config.combinationDialogKey();

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;

            try {
                if (Rs2Inventory.isFull()) {
                    Rs2Bank.depositAll();
                }

                if (!isCombining && Rs2Inventory.hasItem(config.firstItemId()) && Rs2Inventory.hasItem(config.secondItemId())) {
                    isCombining = true;
                    combineItems(combinationDialogKey);

                    if (config.utilityItemId() != 0) {
                        sleepUntil(() -> Rs2Inventory.onlyContains(config.finishedItemId()) && Rs2Inventory.hasItem(config.utilityItemId()));
                    } else {
                        sleepUntil(() -> Rs2Inventory.onlyContains(config.finishedItemId()));
                    }
                    isCombining = false;
                } else if (!Rs2Inventory.hasItem(config.firstItemId()) || !Rs2Inventory.hasItem(config.secondItemId())) {
                    fetchItems();
                }

            } catch (Exception ex) {
                isCombining = false;
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void fetchItems() {
        Rs2Bank.openBank();
        sleepUntil(Rs2Bank::isOpen);
        sleep(300, 1000);

        Rs2Bank.depositAll();
        sleep(300, 1000);

        Rs2Bank.withdrawX(true, config.firstItemId(), config.firstItemQuantity());
        sleep(300, 1000);

        Rs2Bank.withdrawX(true, config.secondItemId(), config.secondItemQuantity());
        sleep(300, 1000);

        if (config.utilityItemId() != 0) {
            Rs2Bank.withdrawX(true, config.utilityItemId(), 1);
        }

        sleepUntil(() -> Rs2Inventory.hasItem(config.firstItemId()) && Rs2Inventory.hasItem(config.secondItemId()));
        Rs2Bank.closeBank();
    }

    private void combineItems(Keybind combinationDialogKey) {
        Rs2Inventory.use(config.firstItemId());
        sleep(300, 1000);

        Rs2Inventory.use(config.secondItemId());
        sleep(300, 1000);

        Rs2Keyboard.typeKey(combinationDialogKey);
        sleep(2500, 3300);
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}