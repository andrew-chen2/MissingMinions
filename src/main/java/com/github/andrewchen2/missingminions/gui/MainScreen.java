package com.github.andrewchen2.missingminions.gui;

import com.github.andrewchen2.missingminions.config.ModConfig;
import com.github.andrewchen2.missingminions.network.ItemsApi;
import com.github.andrewchen2.missingminions.util.MinionLogic.Minion;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class MainScreen extends Screen {

    private List<Minion> allMissingMinions;
    private MinionListWidget listWidget;
    private TextFieldWidget searchBox;

    private boolean isLoading = true;

    public MainScreen() {
        super(Text.literal("Missing Minions"));
    }

    public void setData(List<Minion> missingMinions) {
        this.allMissingMinions = missingMinions;
        this.isLoading = false;
        this.filterList(this.searchBox != null ? this.searchBox.getText() : "");
    }

    @Override
    protected void init() {
        super.init();

        // Search bar
        this.searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 20, 200, 20, Text.literal("Search Minions"));
        this.searchBox.setChangedListener(this::filterList);
        this.addSelectableChild(this.searchBox);

        // Scrollable list
        int listHeight = this.height - 90; // Leave 50px at top, 40px at bottom
        this.listWidget = new MinionListWidget(this.client, this.width, listHeight, 50, 30);
        this.addSelectableChild(this.listWidget);
        filterList(""); // Populate the list initially

        // Config buttons
        int btnWidth = 80;
        int bottomY = this.height - 30;

        // Recursion
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Recursion: " + (ModConfig.useRecursiveCost ? "§aON" : "§cOFF")), button -> {
            ModConfig.setUseRecursiveCost(!ModConfig.useRecursiveCost);
            button.setMessage(Text.literal("Recursion: " + (ModConfig.useRecursiveCost ? "§aON" : "§cOFF")));
        }).dimensions(this.width / 2 - 125, bottomY, btnWidth, 20).build());

        // Collections filter
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Filter: " + (ModConfig.filterCollections ? "§aON" : "§cOFF")), button -> {
            ModConfig.setFilterCollections(!ModConfig.filterCollections);
            button.setMessage(Text.literal("Filter: " + (ModConfig.filterCollections ? "§aON" : "§cOFF")));
        }).dimensions(this.width / 2 - 40, bottomY, btnWidth, 20).build());

        // Buy orders
        this.addDrawableChild(ButtonWidget.builder(Text.literal(ModConfig.useBuyOrders ? "§eBuy Orders" : "§eInsta-Buy"), button -> {
            ModConfig.setUseBuyOrders(!ModConfig.useBuyOrders);
            button.setMessage(Text.literal(ModConfig.useBuyOrders ? "§eBuy Orders" : "§eInsta-Buy"));
        }).dimensions(this.width / 2 + 45, bottomY, btnWidth, 20).build());

        if (!isLoading) {
            filterList(this.searchBox.getText());
        }
    }

    private void filterList(String query) {
        if (this.listWidget == null || allMissingMinions == null) return;

        String lowerQuery = query.toLowerCase();
        List<MinionEntry> filtered = new ArrayList<>();

        for (Minion m : allMissingMinions) {
            ItemsApi.MinionData data = ItemsApi.MINIONS.get(m.id());

            String displayName = (data != null) ? data.displayName() : m.id();
            String texture = (data != null) ? data.textureValue() : null;

            if (displayName.toLowerCase().contains(lowerQuery)) {
                filtered.add(new MinionEntry(m, displayName, texture));
            }
        }

        this.listWidget.refreshEntries(filtered);
        this.listWidget.setScrollY(0.0); // Reset scroll to top
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, Colors.WHITE);

        if (isLoading) {
            context.drawCenteredTextWithShadow(this.textRenderer, "§eLoading...", this.width / 2, this.height / 2, Colors.WHITE);
        } else {
            this.listWidget.render(context, mouseX, mouseY, delta);
            this.searchBox.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public void setFocused(Element focused) {
        if (focused == this.searchBox || focused == this.listWidget) {
            super.setFocused(focused);
        } else {
            super.setFocused(null);
        }
    }


    private class MinionListWidget extends ElementListWidget<MinionEntry> {
        public MinionListWidget(MinecraftClient client, int width, int height, int y, int itemHeight) {
            super(client, width, height, y, itemHeight);
        }

        // Helper to safely clear and add entries
        public void refreshEntries(List<MinionEntry> entries) {
            this.clearEntries();
            for (MinionEntry entry : entries) {
                this.addEntry(entry);
            }
        }
    }

    private class MinionEntry extends ElementListWidget.Entry<MinionEntry> {
        private final Minion minion;
        private final String displayName;
        @Nullable private final String textureValue;

        public MinionEntry(Minion minion, String displayName, @Nullable String textureValue) {
            this.minion = minion;
            this.displayName = displayName;
            this.textureValue = textureValue;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int x = this.getX();
            int y = this.getY();

            // Minion icon
            ItemStack icon = createMinionHead(textureValue);
            context.drawItem(icon, x + 5, y + 2);

            String costText = String.format("%,.0f Coins", minion.cost());
            context.drawTextWithShadow(client.textRenderer, displayName, x + 30, y + 5, Colors.LIGHT_YELLOW);
            context.drawTextWithShadow(client.textRenderer, costText, x + 30, y + 16, Colors.GRAY);

            // Tooltip
            if (hovered) {
                List<Text> tooltip = new ArrayList<>();
                tooltip.add(Text.literal("§e" + displayName));
                tooltip.add(Text.literal("§7Total Cost: §6" + costText));
                tooltip.add(Text.literal(""));
                tooltip.add(Text.literal("§7(Click to view recipe)"));

                context.drawTooltip(client.textRenderer, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(Click click, boolean doubled) {
            // Open the recipe
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                player.networkHandler.sendChatCommand("viewrecipe " + minion.id());
            }
            return true; // Click handled
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return List.of();
        }

        @Override
        public List<? extends Element> children() {
            return List.of();
        }

        private ItemStack createMinionHead(@Nullable String textureBase64) {
            ItemStack head = new ItemStack(Items.PLAYER_HEAD);

            if (textureBase64 == null || textureBase64.isBlank()) {
                return head;
            }

            try {
                UUID id = UUID.nameUUIDFromBytes(textureBase64.getBytes());

                Multimap<String, Property> backingMap = HashMultimap.create();
                backingMap.put("textures", new Property("textures", textureBase64));

                PropertyMap properties = new PropertyMap(backingMap);

                GameProfile profile = new GameProfile(id, "Minion", properties);

                ProfileComponent profileComponent = ProfileComponent.ofStatic(profile);

                head.set(DataComponentTypes.PROFILE, profileComponent);
            } catch (Exception e) {
                System.err.println("[MissingMinions] Failed to process minion custom player head skull.");
            }

            return head;
        }
    }
}