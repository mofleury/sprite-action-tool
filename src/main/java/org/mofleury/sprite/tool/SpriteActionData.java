package org.mofleury.sprite.tool;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class SpriteActionData {
    private String filename;
    private Point anchor;
    private Area attackbox;
    private Area lifebox;
}
