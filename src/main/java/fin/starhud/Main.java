package fin.starhud;

import fin.starhud.config.Settings;
import fin.starhud.init.ConfigInit;
import fin.starhud.init.EventInit;
import fin.starhud.init.KeybindInit;
import fin.starhud.integration.ImmediatelyFastAPI;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("starhudforged")
public class Main {

    public static Settings settings;
    public static KeyMapping openEditHUDKey;

    public Main() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ConfigInit.init();
        KeybindInit.init(bus);
        EventInit.init();

        ImmediatelyFastAPI.init();
    }
}
