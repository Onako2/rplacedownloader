/**
 * Copyright (C) 2025  Nebojša Majić (Onako2)
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see
 * <https://www.gnu.org/licenses/>.
 */

package rs.onako2.placedownloader.privacy;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import rs.onako2.placedownloader.Manager;

import java.io.IOException;

public class PrivacyScreen extends Screen {
    public ButtonWidget button1;
    public ButtonWidget button2;
    private Screen parent;
    
    public PrivacyScreen(Screen parent) {
        // The parameter is the title of the screen,
        // which will be narrated when you enter the screen.
        super(Text.translatable("placedownloader.privacy.screen"));
        this.parent = parent;
    }
    
    public PrivacyScreen() {
        super(Text.translatable("placedownloader.privacy.screen"));
        this.parent = null;
    }
    
    @Override
    protected void init() {
        button1 = ButtonWidget.builder(Text.translatable("placedownloader.privacy.screen.full"), button -> {
                    try {
                        PrivacyUtils.agree();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Manager.mc.setScreen(parent);
                })
                .dimensions(width / 2 - 205, 20, 200, 20)
                .tooltip(Tooltip.of(Text.translatable("placedownloader.privacy.screen.full.tooltip")))
                .build();
        button2 = ButtonWidget.builder(Text.translatable("placedownloader.privacy.screen.necessary"), button -> {
                    try {
                        PrivacyUtils.disAgree();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    Manager.mc.setScreen(parent);
                })
                .dimensions(width / 2 + 5, 20, 200, 20)
                .tooltip(Tooltip.of(Text.translatable("placedownloader.privacy.screen.necessary.tooltip")))
                .build();
        
        addDrawableChild(button1);
        addDrawableChild(button2);
    }
    
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("placedownloader.privacy.screen.text.1"), width / 2, height / 4, 0xffffff);
        
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("placedownloader.privacy.screen.text.2"), width / 2, height / 3, 0xffffff);
        
        context.drawCenteredTextWithShadow(textRenderer, Text.translatable("placedownloader.privacy.screen.text.3"), width / 2, height / 2, 0xffffff);
    }
    
    
    public PrivacyScreen setParent(@Nullable Screen parent) {
        if (parent == null || parent.getClass() != this.getClass()) {
            this.parent = parent;
        }
        
        return this;
    }
    
}
