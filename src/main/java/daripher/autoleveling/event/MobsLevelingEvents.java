package daripher.autoleveling.event;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.config.Config.LevelingSettings;
import daripher.autoleveling.init.AutolevelingAttributes;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.saveddata.WorldLevelingData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import daripher.autoleveling.reloader.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.reloader.EntitiesLevelingSettingsReloader;
import net.minecraft.network.chat.Component;
import java.util.UUID;

@EventBusSubscriber(modid = "autoleveling")
public class MobsLevelingEvents {
    private static final UUID HEALTH_BONUS_UUID = UUID.fromString("9df9800e-bc21-4f30-b1ff-92a11b22384a");
    private static final UUID DAMAGE_BONUS_UUID = UUID.fromString("0a64e16d-35bd-4414-87cf-cfd21f8a84eb");
    private static final UUID SPEED_BONUS_UUID = UUID.fromString("6d7b42b2-bf5a-4712-a162-84fc903bb2ba");

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity livingEntity && shouldSetLevel(livingEntity)) {
            MinecraftServer server = livingEntity.getServer();
            if (server == null) return;
            BlockPos spawnPos = server.getLevel(Level.OVERWORLD).getSharedSpawnPos();
            double distance = Math.sqrt(livingEntity.blockPosition().distSqr(spawnPos));
            int level = createLevelForEntity(livingEntity, distance);
            if (level > 0) {
                setLevel(livingEntity, level);
            }
        }
    }

    private static boolean shouldSetLevel(Entity entity) {
        if (entity.level().isClientSide) return false;
        return canHaveLevel(entity);
    }

    private static boolean canHaveLevel(Entity entity) {
        LevelingSettings settings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
        if (settings != null) return !settings.ignored();
        ResourceKey<Level> dimension = entity.level().dimension();
        settings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
        return settings != null && !settings.ignored();
    }

    private static void setLevel(LivingEntity entity, int level) {
        entity.getPersistentData().putInt("level", level);
        Config.getAttributeBonuses().forEach((attribute, bonus) -> {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance != null) {
                float totalBonus = bonus * level;
                UUID modifierUuid = attribute == Attributes.MAX_HEALTH ? HEALTH_BONUS_UUID : (attribute == Attributes.ATTACK_DAMAGE ? DAMAGE_BONUS_UUID : SPEED_BONUS_UUID);
                AttributeModifier modifier = new AttributeModifier(modifierUuid, "Auto Leveling Bonus", totalBonus, Operation.MULTIPLY_TOTAL);
                instance.addPermanentModifier(modifier);
                if (attribute == Attributes.MAX_HEALTH) {
                    entity.setHealth(entity.getMaxHealth());
                }
            }
        });
    }

    private static int createLevelForEntity(LivingEntity entity, double distance) {
        MinecraftServer server = entity.getServer();
        if (server == null) return 0;
        LevelingSettings levelingSettings = EntitiesLevelingSettingsReloader.getSettingsForEntity(entity.getType());
        if (levelingSettings == null) {
            ResourceKey<Level> dimension = entity.level().dimension();
            levelingSettings = DimensionsLevelingSettingsReloader.getSettingsForDimension(dimension);
        }
        
        // --- SISTEMA DE SECTORES INFINITOS (3 niveles cada 1,500 bloques) ---
        int sector = (int) (distance / 1500);
        int levelFromDistance = sector * 3;
        
        // --- DETECTOR DE ZONAS RPG CON TÍTULOS GIGANTES EN PANTALLA (Borde Superior) ---
        server.getPlayerList().getPlayers().forEach(serverPlayer -> {
            double pDist = Math.sqrt(serverPlayer.blockPosition().distSqr(server.getLevel(Level.OVERWORLD).getSharedSpawnPos()));
            int playerSector = (int) (pDist / 1500);
            
            String tag = "last_sector_k32";
            if (!serverPlayer.getPersistentData().contains(tag) || serverPlayer.getPersistentData().getInt(tag) != playerSector) {
                serverPlayer.getPersistentData().putInt(tag, playerSector);
                
                // Comando para lanzar el título en la parte superior (Actionbar)
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                    "title " + serverPlayer.getGameProfile().getName() + " times 10 40 10");
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                    "title " + serverPlayer.getGameProfile().getName() + " actionbar [\"\",{\"text\":\"  Has entrado a la \",\"color\":\"gold\"},{\"text\":\"ZONA " + playerSector + "\",\"bold\":true,\"color\":\"dark_red\"},{\"text\":\"  \",\"color\":\"gold\"}]");
                
                // Sonido épico de transición de zona
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                    "playsound minecraft:ui.toast.challenge_complete ambient " + serverPlayer.getGameProfile().getName() + " ~ ~ ~ 1 1");
            }
        });

        // --- SISTEMA DEL DÍA 3 (Frenar el tiempo los días 1 y 2) ---
        long gameTime = entity.level().getGameTime();
        long daysPassed = gameTime / 24000;
        int levelFromTime = 0;
        
        if (daysPassed >= 3) {
            levelFromTime = (int) ((daysPassed - 3) * levelingSettings.levelsPerDay());
            
            // Alerta cinematográfica en pantalla al iniciar el Día 3
            server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                if (daysPassed == 3 && gameTime % 24000  0) monsterlevel += entity.getRandom().nextInt(levelBonus);

        // Aplicamos multiplicadores extras del autor base
        monsterlevel = Math.abs(monsterlevel);
        monsterlevel += WorldLevelingData.get((ServerLevel) entity.level()).getLevelBonus();
        
        GlobalLevelingData globalLevelingData = GlobalLevelingData.get(server);
        monsterlevel += globalLevelingData.getLevelBonus();

        // --- EL TOPE DEFINITIVO (Límite absoluto de 100 niveles) ---
        int maxLevel = levelingSettings.maxLevel();
        if (maxLevel > 0) monsterlevel = Math.min(monsterlevel, 100);

        return monsterlevel;
    }

    @SubscribeEvent
    public static void applyDamageBonus(LivingHurtEvent event) {
        DamageSource damageSource = event.getSource();
        if (!(damageSource.getEntity() instanceof LivingEntity attacker)) return;
        if (damageSource.isProjectile()) {
            AttributeInstance projectileDamage = attacker.getAttribute(AutolevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
            if (attacker.getAttribute(AutolevelingAttributes.PROJECTILE_DAMAGE_BONUS.get()) == null) return;
            float damageBonus = (float) attacker.getAttributeValue(AutolevelingAttributes.PROJECTILE_DAMAGE_BONUS.get());
            event.setAmount(event.getAmount() * damageBonus);
        }
        if (damageSource.isExplosion()) {
            AttributeInstance explosionDamage = attacker.getAttribute(AutolevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
            if (attacker.getAttribute(AutolevelingAttributes.EXPLOSION_DAMAGE_BONUS.get()) == null) return;
            float damageBonus = (float) attacker.getAttributeValue(AutolevelingAttributes.EXPLOSION_DAMAGE_BONUS.get());
            event.setAmount(event.getAmount() * damageBonus);
        }
    }
}
