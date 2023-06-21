package mgi.tools.parser.readers;

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import mgi.tools.parser.TypeProperty;
import mgi.tools.parser.TypeReader;
import mgi.types.Definitions;
import mgi.types.sprite.SpriteGroupDefinitions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Tommeh | 01/02/2020 | 14:42
 * @see <a href="https://www.rune-server.ee/members/tommeh/">Rune-Server profile</a>
 */
public class SpriteReader implements TypeReader {
    @Override
    public ArrayList<Definitions> read(final Map<String, Object> properties) throws NoSuchFieldException, IllegalAccessException {
        final java.util.ArrayList<mgi.types.Definitions> defs = new ArrayList<Definitions>();
        if (properties.containsKey("inherit")) {
            final java.lang.Object inherit = properties.get("inherit");
            defs.add(SpriteGroupDefinitions.get(((Long) inherit).intValue()));
        } else {
            defs.add(new SpriteGroupDefinitions());
        }
        for (final mgi.types.Definitions definition : defs) {
            final mgi.types.sprite.SpriteGroupDefinitions sprite = (SpriteGroupDefinitions) definition;
            TypeReader.setFields(sprite, properties);
            if (properties.containsKey(TypeProperty.IMAGES.getIdentifier())) {
                final java.util.Map<java.lang.String, java.lang.Object> map = (Map<String, Object>) properties.get(TypeProperty.IMAGES.getIdentifier());
                boolean clear = false;
                if (map.containsKey("clear")) {
                    clear = (Boolean) map.get("clear");
                }
                map.remove("clear");
                final it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap<java.awt.image.BufferedImage> images = new Int2ObjectAVLTreeMap<BufferedImage>();
                for (final java.util.Map.Entry<java.lang.String, java.lang.Object> entry : map.entrySet()) {
                    final int id = Integer.parseInt(entry.getKey());
                    final java.lang.String path = entry.getValue().toString();
                    try {
                        final java.awt.image.BufferedImage image = ImageIO.read(new File("./assets/sprites/" + path));
                        //Exception for emote tab emotes transparency; easier to do this programmatically. Higher revision emotes have a darker "locked" sprite.
                        if (path.startsWith("emotes/")) {
                            final int colourToSearch = 42 << 16 | 37 << 8 | 27 | -16777216;
                            final int colourToReplace = 64 << 16 | 57 << 8 | 40 | -16777216;
                            final int borderColour = 51 << 16 | 46 << 8 | 32 | -16777216;
                            final int width = image.getWidth();
                            final int height = image.getHeight();
                            for (int x = 0; x < width; x++) {
                                for (int y = 0; y < height; y++) {
                                    final int colour = image.getRGB(x, y);
                                    if (colour == colourToSearch) {
                                        //Lets identify if this pixel is a border pixel and create a thin 1px inner border out of the image to match the consistency.
                                        final boolean isBorderPixel = x == 0 || y == 0 || x == width - 1 || y == height - 1 || image.getRGB(x - 1, y) == 0 || image.getRGB(x + 1, y) == 0 || image.getRGB(x, y - 1) == 0 || image.getRGB(x, y + 1) == 0;
                                        image.setRGB(x, y, isBorderPixel ? borderColour : colourToReplace);
                                    }
                                }
                            }
                        }
                        images.put(id, image);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (clear || sprite.getImages() == null) {
                    sprite.setImages(images);
                } else {
                    for (final it.unimi.dsi.fastutil.ints.Int2ObjectMap.Entry<java.awt.image.BufferedImage> entry : images.int2ObjectEntrySet()) {
                        sprite.getImages().put(entry.getIntKey(), entry.getValue());
                        sprite.setImage(entry.getIntKey(), entry.getValue());
                    }
                }
            }
        }
        return defs;
    }

    @Override
    public String getType() {
        return "sprite";
    }
}
