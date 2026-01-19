package be.raft.warfare.data;

import be.raft.warfare.CreateWarfare;
import com.google.gson.JsonElement;
import com.simibubi.create.foundation.utility.FilesHelper;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

public class WarfareDataGen {
    private static final String DEFAULT_LANG_PATH = "assets/warfare/lang/default/";

    public static void gatherData(GatherDataEvent event) {
        CreateWarfare.REGISTRATE.addDataGenerator(ProviderType.LANG, provider -> {
            loadDefaultLang("interfaces", provider);
        });

    }

    private static void loadDefaultLang(String filename, RegistrateLangProvider provider) {
        String path = DEFAULT_LANG_PATH + filename + ".json";
        JsonElement json = FilesHelper.loadJsonResource(path);

        if (json == null)
            throw new IllegalStateException("Could not find default lang file: " + path);

        json.getAsJsonObject().entrySet().forEach(entry -> provider.add(entry.getKey(), entry.getValue().getAsString()));
    }
}
