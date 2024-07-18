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

    public static final double VERSION = 1.0;

    private volatile boolean isCombining = false;

    public void run(CraftingConfig config) {
        this.config = config;

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                return;
            }

            try {
                handleInventory();

                if (!isCombining) {
                    prepareForCombining();
                    if (shouldCombine()) {
                        combineItems(config.combinationDialogKey());
                    }
                }
            } catch (Exception ex) {
                isCombining = false;
                Microbot.log("Error during combining: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void handleInventory() {
        if (Rs2Inventory.isFull()) {
            Rs2Bank.openBank();
            Rs2Bank.depositAll();
        }
    }

    private void prepareForCombining() {
        Microbot.log("Checking if items are available for combining.");

        if (config.utilityItemId() != 0 && !Rs2Inventory.hasItem(config.utilityItemId())) {
            withdrawUtilityItem();
        }

        if (!Rs2Inventory.hasItem(config.firstItemId()) || !Rs2Inventory.hasItem(config.secondItemId())) {
            fetchItems();
        }
    }

    private boolean shouldCombine() {
        return Rs2Inventory.hasItem(config.firstItemId()) && Rs2Inventory.hasItem(config.secondItemId()) && !Rs2Inventory.hasItem(config.finishedItemId());
    }

    private void withdrawUtilityItem() {
        Microbot.log("Utility item not found in inventory, withdrawing from bank.");
        Rs2Bank.openBank();
        Rs2Bank.withdrawX(true, config.utilityItemId(), 1);
        sleepUntil(() -> Rs2Inventory.hasItem(config.utilityItemId()), 5000);
        Microbot.log("Withdrew utility item: " + config.utilityItemId());
    }

    private void fetchItems() {
        Microbot.log("Fetching items from the bank.");
        Rs2Bank.openBank();

        if (config.utilityItemId() != 0) {
            Rs2Bank.withdrawX(true, config.utilityItemId(), 1);
            sleep(300, 1000);
            Microbot.log("Withdrew utility item: " + config.utilityItemId());
        }

        Rs2Bank.withdrawX(true, config.firstItemId(), config.firstItemQuantity());
        sleep(300, 1000);
        Microbot.log("Withdrew first item: " + config.firstItemId());

        Rs2Bank.withdrawX(true, config.secondItemId(), config.secondItemQuantity());
        sleep(300, 1000);
        Microbot.log("Withdrew second item: " + config.secondItemId());

        Rs2Bank.closeBank();
        sleep(300, 1000);
        Microbot.log("Closed the bank after fetching items.");
    }

    private void combineItems(Keybind combinationDialogKey) {
        if (isCombining) {
            Microbot.log("Already combining items, exiting combineItems method.");
            return;
        }

        isCombining = true;

        try {
            Microbot.log("Starting to combine items.");
            Rs2Inventory.use(config.firstItemId());
            sleep(300, 1000);

            Rs2Inventory.use(config.secondItemId());
            sleep(300, 1000);

            Rs2Keyboard.typeKey(combinationDialogKey);
            sleep(2500, 3300);
            Microbot.log("Finished combining items.");
        } finally {
            isCombining = false;
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (mainScheduledFuture != null && !mainScheduledFuture.isCancelled()) {
            mainScheduledFuture.cancel(true);
        }
    }
}