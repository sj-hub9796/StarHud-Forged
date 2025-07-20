package fin.starhud;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;


public class Helper {

    private static final char[] superscripts = "⁰¹²³⁴⁵⁶⁷⁸⁹".toCharArray();
    private static final char[] subscripts = "₀₁₂₃₄₅₆₇₈₉".toCharArray();

    // only convert numbers.
    public static String toSuperscript(String str) {
        char[] chars = str.toCharArray();

        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = chars[i];

            if (c >= '0' && c <= '9')
                chars[i] = superscripts[c - '0'];
        }

        return String.valueOf(chars);
    }

    public static String toSubscript(String str) {
        char[] chars = str.toCharArray();

        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char c = chars[i];

            if (c >= '0' && c <= '9')
                chars[i] = subscripts[c - '0'];
        }

        return String.valueOf(chars);
    }

    // convert (modname:snake_case) into (Snake Case)
    public static String idNameFormatter(String id) {

        // trim every character from ':' until first index
        id = id.substring(id.indexOf(':') + 1);

        char[] chars = id.toCharArray();

        if (chars.length == 0) return "-";

        chars[0] = Character.toUpperCase(chars[0]);
        for (int i = 1; i < chars.length; ++i) {
            if (chars[i] != '_') continue;

            chars[i] = ' ';

            // capitalize the first character after spaces
            if (i + 1 < chars.length) {
                chars[i + 1] = Character.toUpperCase(chars[i + 1]);
            }
        }

        return new String(chars);
    }

    public static String getModName(ResourceLocation id) {
        String nameSpace = id.getNamespace();
        ModContainer container = ModList.get().getModContainerById(nameSpace).orElse(null);
        return container == null ? nameSpace : container.getModInfo().getDisplayName();
    }

    public static int getStep(int curr, int max, int maxStep) {
        return Mth.clamp(Math.round((float) curr * maxStep / (float) max), 0, maxStep);
    }

    public static boolean isModPresent(String id) {
        return ModList.get().isLoaded(id);
    }
}