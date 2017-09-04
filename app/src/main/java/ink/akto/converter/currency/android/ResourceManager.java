package ink.akto.converter.currency.android;

import android.content.res.Resources;
import android.support.annotation.NonNull;

/**
 * Created by Ruben on 01.09.2017.
 */

public class ResourceManager implements AndroidContracts.IResourceManager
{
    @NonNull private Resources resources;

    public ResourceManager(@NonNull Resources resources)
    {
        this.resources = resources;
    }

    @Override
    public @NonNull String getStringErrorDownloading()
    {
        return resources.getString(R.string.download_error);
    }
}
