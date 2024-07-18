package net.runelite.client.plugins.microbot.crafting;

import net.runelite.client.config.*;
import net.runelite.client.plugins.microbot.crafting.enums.*;

import java.awt.event.KeyEvent;

@ConfigGroup(CraftingConfig.GROUP)
public interface CraftingConfig extends Config {

    String GROUP = "Crafting";

    @ConfigSection(
            name = "General",
            description = "General",
            position = 0
    )
    String generalSection = "general";
    @ConfigSection(
            name = "Gems",
            description = "Config for gem cutting",
            position = 1,
            closedByDefault = true
    )
    String gemSection = "gem";
    @ConfigSection(
            name = "Glass",
            description = "Config for glass blowing",
            position = 2,
            closedByDefault = true
    )
    String glassSection = "glass";
    @ConfigItem(
            keyName = "fletchIntoBoltTips",
            name = "Fletch into Bolt Tips",
            description = "Fletch cut gems into bolt tips if possible",
            position = 1,
            section = gemSection
    )
    default boolean fletchIntoBoltTips() {
        return false;
    }
    @ConfigSection(
            name = "Staffs",
            description = "Config for staff making",
            position = 2,
            closedByDefault = true
    )
    String staffSection = "staff";
    @ConfigSection(
            name = "Flax",
            description = "Configure Settings for Flax Spinning activity",
            position = 3,
            closedByDefault = true
    )
    String flaxSpinSection = "flaxspin";

    @ConfigItem(
            keyName = "Activity",
            name = "Activity",
            description = "Choose the type of crafting activity to perform",
            position = 0,
            section = generalSection
    )
    default Activities activityType() {
        return Activities.NONE;
    }

    @ConfigItem(
            keyName = "Afk",
            name = "Random AFKs",
            description = "Randomy afks between 3 and 60 seconds",
            position = 1,
            section = generalSection
    )
    default boolean Afk() {
        return false;
    }

    @ConfigItem(
            keyName = "Gem",
            name = "Gem",
            description = "Choose the type of gem to cut",
            position = 0,
            section = gemSection
    )
    default Gems gemType() {
        return Gems.NONE;
    }

    @ConfigItem(
            keyName = "Glass",
            name = "Glass",
            description = "Choose the type of glass item to blow",
            position = 0,
            section = glassSection
    )
    default Glass glassType() {
        return Glass.NONE;
    }

    @ConfigItem(
            keyName = "Staffs",
            name = "Staffs",
            description = "Choose the type of battlestaff to make",
            position = 0,
            section = staffSection
    )
    default Staffs staffType() {
        return Staffs.NONE;
    }

    @ConfigItem(
            name = "Location",
            description = "Choose Location where to spin flax",
            keyName = "flaxSpinLocation",
            position = 0,
            section = flaxSpinSection
    )
    default FlaxSpinLocations flaxSpinLocation() {
        return FlaxSpinLocations.NONE;
    }

    @ConfigSection(
            name = "Custom",
            description = "Custom crafting settings",
            position = 100
    )
    String customSection = "customSection";

    @ConfigItem(
            keyName = "firstItemId",
            name = "Item ID (First Item)",
            description = "The ID of the first item to use",
            section = customSection,
            position = 0
    )
    default int firstItemId() {
        return 0;
    }

    @ConfigItem(
            keyName = "firstItemQuantity",
            name = "Quantity to Withdraw (First Item)",
            description = "The quantity of the first item to withdraw",
            section = customSection,
            position = 1
    )
    default int firstItemQuantity() {
        return 0;
    }

    @ConfigItem(
            keyName = "secondItemId",
            name = "Item ID (Second Item)",
            description = "The ID of the second item to use",
            section = customSection,
            position = 2
    )
    default int secondItemId() {
        return 0;
    }

    @ConfigItem(
            keyName = "secondItemQuantity",
            name = "Quantity to Withdraw (Second Item)",
            description = "The quantity of the second item to withdraw",
            section = customSection,
            position = 3
    )
    default int secondItemQuantity() {
        return 0;
    }

    @ConfigItem(
            keyName = "utilityItemId",
            name = "Item ID (Utility Item)",
            description = "The ID of the utility item, if any",
            section = customSection,
            position = 4
    )
    default int utilityItemId() {
        return 0;
    }

    @ConfigItem(
            keyName = "finishedItemId",
            name = "Item ID (Finished Item)",
            description = "The ID of the finished item",
            section = customSection,
            position = 5
    )
    default int finishedItemId() {
        return 0;
    }

    @ConfigItem(
            keyName = "hasCombinationDialog",
            name = "Has Combination Dialog",
            description = "Check if combining items produces a dialog",
            section = customSection,
            position = 7
    )
    default boolean hasCombinationDialog() {
        return false;
    }

    @ConfigItem(
            keyName = "combinationDialogKey",
            name = "Combination Dialog Key",
            description = "The key to press when the combination dialog appears",
            section = customSection,
            position = 8
    )
    default Keybind combinationDialogKey() {
        return Keybind.NOT_SET;
    }
}
