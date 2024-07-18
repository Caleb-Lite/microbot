package net.runelite.client.plugins.microbot.crafting.scripts;

import net.runelite.client.config.Keybind;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.CraftingConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;
import net.runelite.client.plugins.microbot.util.player.Rs2Player;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class CombiningScript extends Script {

    @Inject
    private CraftingConfig config;

    public static final double VERSION = 1.0;

    private volatile boolean isCombining = false;

    public void run(CraftingConfig config) {
        this.config = config;
        Microbot.log("CombiningScript started");

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run() || !Microbot.isLoggedIn()) {
                Microbot.log("Script stopped or user is not logged in");
                return;
            }

            try {
                if (!isCombining) {
                    handleInventory();
                    prepareForCombining();
                    if (shouldCombine()) {
                        combineItems(config.combinationDialogueKey());
                    }
                }
            } catch (Exception ex) {
                isCombining = false;
                Microbot.log("Error during combining: " + ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private void handleInventory() {
        Microbot.log("Handling inventory");
        if (!isBusy()) {
            if (!isCombining) {
                Microbot.log("Opening bank to deposit all items");
                Rs2Bank.openBank();
                Rs2Bank.depositAll();
                Microbot.log("All items deposited");
            } else {
                Microbot.log("Currently combining items, skipping inventory handling");
            }
        }
    }

    private void prepareForCombining() {
        if (isCombining) {
            Microbot.log("Currently combining items, skipping prepareForCombining");
            return;
        }
        Microbot.log("Preparing for combining items");
        if (config.utilityItemId() != 0 && !Rs2Inventory.hasItem(config.utilityItemId())) {
            withdrawUtilityItem();
        }

        if (!Rs2Inventory.hasItem(config.firstItemId()) || !Rs2Inventory.hasItem(config.secondItemId())) {
            fetchItems();
        }
    }

    private boolean shouldCombine() {
        boolean shouldCombine = Rs2Inventory.hasItem(config.firstItemId()) && Rs2Inventory.hasItem(config.secondItemId()) && !Rs2Inventory.hasItem(config.finishedItemId());
        Microbot.log("Should combine: " + shouldCombine);
        return shouldCombine;
    }

    private void withdrawUtilityItem() {
        if (isCombining) {
            Microbot.log("Currently combining items, skipping withdrawUtilityItem");
            return;
        }
        Microbot.log("Utility item not found in inventory, withdrawing from bank.");
        Rs2Bank.openBank();
        Rs2Bank.withdrawX(true, config.utilityItemId(), 1);
        sleepUntil(() -> Rs2Inventory.hasItem(config.utilityItemId()), 2000);
        Microbot.log("Withdrew utility item: " + config.utilityItemId());
    }

    private void fetchItems() {
        if (isCombining) {
            Microbot.log("Currently combining items, skipping fetchItems");
            return;
        }

        Microbot.log("Fetching items from the bank.");
        if (!isBusy()) {
            Rs2Bank.openBank();
        }

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

            if (config.useUtilityItemOnFirstItem()) {
                Rs2Inventory.use(config.utilityItemId());
                sleep(300, 1000);

                Microbot.log("Using utility item on first item: " + config.firstItemId());
                Rs2Inventory.use(config.firstItemId());
                sleep(300, 1000);

                Rs2Keyboard.typeKey(combinationDialogKey);
                sleep(2500, 3300);
                Microbot.log("Pressed combination dialog key: " + combinationDialogKey);
                Microbot.log("Finished combining items.");
            } else {
                Rs2Inventory.use(config.firstItemId());
                sleep(300, 1000);
                Microbot.log("Using first item: " + config.firstItemId());

                Rs2Inventory.use(config.secondItemId());
                sleep(300, 1000);
                Microbot.log("Using second item: " + config.secondItemId());

                Rs2Keyboard.typeKey(combinationDialogKey);
                sleep(2500, 3300);
                Microbot.log("Pressed combination dialog key: " + combinationDialogKey);
                Microbot.log("Finished combining items.");
            }
        } catch (Exception e) {
            Microbot.log("Error during combining items: " + e.getMessage());
        } finally {
            isCombining = false;
        }
    }

    private boolean isBusy() {
        return Rs2Player.isAnimating() || Rs2Player.isInteracting() || !sleepUntil(() -> !Rs2Player.isAnimating() && !Rs2Player.isInteracting(), 3 * 600);
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (mainScheduledFuture != null && !mainScheduledFuture.isCancelled()) {
            mainScheduledFuture.cancel(true);
        }
        Microbot.log("CombiningScript shut down");
    }
}
