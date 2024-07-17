package net.runelite.client.plugins.microbot.crafting.scripts;

import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;
import net.runelite.client.plugins.microbot.crafting.CraftingConfig;
import net.runelite.client.plugins.microbot.util.bank.Rs2Bank;
import net.runelite.client.plugins.microbot.util.inventory.Rs2Inventory;
import net.runelite.client.plugins.microbot.util.keyboard.Rs2Keyboard;

import javax.inject.Inject;
import java.awt.event.KeyEvent;
import java.util.concurrent.TimeUnit;

public class CombiningScript extends Script {

    @Inject
    private CraftingConfig config;

    public static double version = 1.0;

    public void run(CraftingConfig config) {
        int keyCode = config.combinationDialogKey().getKeyCode();
        System.out.println("Combination Dialog Key Code: " + keyCode);

        // Convert ASCII value to the actual number
        int actualKey = convertKeyCode(keyCode);
        System.out.println("Actual Key: " + actualKey);

        Rs2Keyboard.keyPress(actualKey);

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            if (!super.run()) return;
            if (!Microbot.isLoggedIn()) return;

            try {
                if (Rs2Inventory.isFull()) {
                    Rs2Bank.depositAll();
                }

                if (!Rs2Inventory.hasItem(config.firstItemId()) || !Rs2Inventory.hasItem(config.secondItemId())) {
                    fetchItems();
                }

                combineItems();

            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }, 0, 600, TimeUnit.MILLISECONDS);
    }

    private int convertKeyCode(int keyCode) {
        if (keyCode >= 48 && keyCode <= 57) {
            return keyCode - 48;
        }
        return keyCode; // Return the original keyCode if it's not a digit
    }

    private void fetchItems() {
        Rs2Bank.openBank();
        sleepUntil(Rs2Bank::isOpen);

        Rs2Bank.depositAll();

        Rs2Bank.withdrawX(true, config.firstItemId(), config.firstItemQuantity());
        Rs2Bank.withdrawX(true, config.secondItemId(), config.secondItemQuantity());

        if (config.utilityItemId() != 0) {
            Rs2Bank.withdrawX(true, config.utilityItemId(), 1);
        }

        sleepUntil(() -> Rs2Inventory.hasItem(config.firstItemId()) && Rs2Inventory.hasItem(config.secondItemId()));
        Rs2Bank.closeBank();
    }

    private void combineItems() {
        Rs2Inventory.use(config.firstItemId());
        Rs2Inventory.use(config.secondItemId());

        if (config.hasCombinationDialog()) {
            Rs2Keyboard.keyPress(config.combinationDialogKey().getKeyCode());
        }

        sleepUntil(() -> Rs2Inventory.count(config.finishedItemId()) == config.firstItemQuantity() + config.secondItemQuantity());

        if (config.utilityItemId() != 0) {
            sleepUntil(() -> Rs2Inventory.count(config.finishedItemId()) == config.firstItemQuantity() + config.secondItemQuantity() && Rs2Inventory.hasItem(config.utilityItemId()));
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
    }
}