package com.parzivail.pswg.client.species;

import com.google.common.base.Suppliers;
import com.parzivail.pswg.Client;
import com.parzivail.pswg.Resources;
import com.parzivail.pswg.client.model.EmptyPlayerModel;
import com.parzivail.pswg.container.SwgSpeciesRegistry;
import com.parzivail.pswg.species.SpeciesGender;
import com.parzivail.pswg.species.SwgSpecies;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.function.Supplier;

public class SwgSpeciesModels
{
	public static final HashMap<Identifier, SwgSpeciesModel> MODELS = new HashMap<>();

	private static final Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> EMPTY_MODEL = EmptyPlayerModel::new;

	static
	{
//		register(SwgSpeciesRegistry.SPECIES_AQUALISH, new ModelAqualish<>(true, 0), new ModelAqualish<>(false, 0));
//		register(SwgSpeciesRegistry.SPECIES_BITH, new ModelBith<>(true, 0), new ModelBith<>(false, 0));
//		register(SwgSpeciesRegistry.SPECIES_BOTHAN, new ModelBothan<>(true, 0), new ModelBothan<>(false, 0));
//		register(SwgSpeciesRegistry.SPECIES_CHAGRIAN, new ModelChagrian<>(true, 0), new ModelChagrian<>(false, 0));
//		register(SwgSpeciesRegistry.SPECIES_KAMINOAN, new ModelKaminoan<>(true, 0), new ModelKaminoan<>(false, 0));
//		register(SwgSpeciesRegistry.SPECIES_JAWA, new ModelJawa<>(0), new ModelJawa<>(0));
//		register(SwgSpeciesRegistry.SPECIES_TOGRUTA, new ModelTogrutaM<>(0), new ModelTogrutaF<>(0));
//		register(SwgSpeciesRegistry.SPECIES_TWILEK, new ModelTwilek<>(true, 0), new ModelTwilek<>(false, 0));
		register(SwgSpeciesRegistry.SPECIES_AQUALISH, nemSource(Resources.id("species/aqualish")));
		register(SwgSpeciesRegistry.SPECIES_BITH, nemSource(Resources.id("species/bith")));
		register(SwgSpeciesRegistry.SPECIES_BOTHAN, EMPTY_MODEL);
		register(SwgSpeciesRegistry.SPECIES_CHAGRIAN, EMPTY_MODEL);
		register(SwgSpeciesRegistry.SPECIES_KAMINOAN, nemSource(Resources.id("species/kaminoan")));
		register(SwgSpeciesRegistry.SPECIES_JAWA, nemSource(Resources.id("species/jawa")));
		register(SwgSpeciesRegistry.SPECIES_TOGRUTA, nemSource(Resources.id("species/togruta_m")), nemSource(Resources.id("species/togruta_f")));
		register(SwgSpeciesRegistry.SPECIES_TWILEK, nemSource(Resources.id("species/twilek")));
		//		register(new SwgSpeciesModel(SwgSpeciesRegistry.SPECIES_WOOKIEE_M, new ModelWookiee<>(true, 0)));
		//		register(new SwgSpeciesModel(SwgSpeciesRegistry.SPECIES_WOOKIEE_F, new ModelWookiee<>(false, 0)));
	}

	private static Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> nemSource(Identifier id)
	{
		return Client.ResourceManagers.getNemManager().getPlayerModel(id, true);
	}

	private static void register(SwgSpeciesModel model)
	{
		MODELS.put(model.identifier(), model);
	}

	private static void register(Identifier speciesSlug, SpeciesGender gender, Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> model)
	{
		register(new SwgSpeciesModel(SpeciesGender.toModel(speciesSlug, gender), Suppliers.memoize(model::get)));
	}

	private static void register(Identifier speciesSlug, Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> male, Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> female)
	{
		register(speciesSlug, SpeciesGender.MALE, male);
		register(speciesSlug, SpeciesGender.FEMALE, female);
	}

	private static void register(Identifier speciesSlug, Supplier<PlayerEntityModel<AbstractClientPlayerEntity>> androgynousModel)
	{
		register(speciesSlug, SpeciesGender.MALE, androgynousModel);
		register(speciesSlug, SpeciesGender.FEMALE, androgynousModel);
	}

	public static Identifier getTexture(SwgSpecies species)
	{
		var hashCode = species.hashCode();

		// TODO: sensible fallback texture instead of just black for a frame?
		return Client.stackedTextureProvider.loadTexture("species/" + hashCode, () -> new Identifier(""), species::getTextureStack);
	}
}
