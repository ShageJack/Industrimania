package shagejack.minecraftology.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import shagejack.minecraftology.api.internal.ItemModelProvider;
import shagejack.minecraftology.client.ClientUtil;

public class ItemBase extends Item implements ItemModelProvider {
    protected final String name;

    public ItemBase(String name) {
        this.name = name;
        this.setRegistryName(name);
        this.setTranslationKey(getRegistryName().toString().replace(':', '.'));
    }

    @Override
    public void initItemModel() {
        if (this instanceof IAdvancedModelProvider) {
            String[] subNames = ((IAdvancedModelProvider) this).getSubNames();
            for (int i = 0; i < subNames.length; i++) {
                String sub = subNames[i];
                ModelLoader.setCustomModelResourceLocation(this, i, new ModelResourceLocation(getRegistryName() + "_" + sub, "inventory"));
            }
            return;
        }
        if (!getHasSubtypes())
            ClientUtil.registerModel(this, getRegistryName().toString());
        else {
            NonNullList<ItemStack> sub = NonNullList.create();
            getSubItems(CreativeTabs.SEARCH, sub);
            for (ItemStack stack : sub) {
                ModelLoader.setCustomModelResourceLocation(stack.getItem(), stack.getMetadata(), new ModelResourceLocation(getRegistryName(), "inventory"));
            }
        }
    }
}
