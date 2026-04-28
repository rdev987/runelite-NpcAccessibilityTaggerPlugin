package com.rymr.npcaccessibilitytagger;

import com.google.inject.Inject;

import java.awt.*;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

/*
 * Copyright (c) 2023, R-Y-M-R
 * Copyright (c) 2021, Xrio
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
@Slf4j
public class NpcAccessibilityTaggerOverlay extends Overlay {
    private final Client client;
    private final NpcAccessibilityTaggerConfig config;

    private boolean appendWordToNPC;
    private boolean enableCustomTextColor;
    private Color defaultFontColor;
    private Font font;
    private int npcHeightOffset;

    @Inject
    NpcAccessibilityTaggerOverlay(Client client, NpcAccessibilityTaggerConfig config) {
        this.client = client;
        this.config = config;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
    }

    public void readConfig() {
        this.appendWordToNPC = config.appendWordToNPC();
        this.enableCustomTextColor = config.enableCustomTextColor();
        this.defaultFontColor = config.defaultFontColor();
        this.font = config.fontStyle().getFont().deriveFont((float) config.fontSize());
        this.npcHeightOffset = config.heightAboveNPC();
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (appendWordToNPC) {
            for (NPC npc : client.getTopLevelWorldView().npcs()) {
                if (npc == null) {
                    continue;
                }
                TagEntry matchingEntry = NpcAccessibilityTaggerParser.getInstance().getEntries().get(npc.getId());
                if (matchingEntry == null) {
                    continue;
                }
                if (!shouldShow(npc)) {
                    continue;
                }
                renderNpcOverlay(graphics, npc, matchingEntry);
            }
        }
        return null;
    }

    private void renderNpcOverlay(Graphics2D graphics, NPC npc, TagEntry entry) {
        final Point textLocation = npc.getCanvasTextLocation(graphics, entry.getText(), npc.getLogicalHeight() + npcHeightOffset);
        if (textLocation != null) {
            graphics.setFont(font);
            Color color = enableCustomTextColor && entry.getColor() != null ? entry.getColor() : defaultFontColor;
            OverlayUtil.renderTextLocation(graphics, textLocation, entry.getText(), color);
        }
    }

    // Copied from Buchus.
    private boolean shouldShow(NPC npc) {
        if (npc.getComposition() == null) {
            return false;
        }
        if (npc.getName() == null || npc.getName().isEmpty() || npc.getName().equals("null")) {
            return false;
        }
        return !isInvisible(npc.getModel());
    }

    // Copied from Skretzo
    private boolean isInvisible(Model model) {
        // If all the values in model.getFaceColors3() are -1 then the model is invisible
        for (int value : model.getFaceColors3()) {
            if (value != -1) {
                return false;
            }
        }
        return true;
    }
}
