package daripher.autoleveling.event;

import daripher.autoleveling.config.Config;
import daripher.autoleveling.config.Config.LevelingSettings;
import daripher.autoleveling.saveddata.GlobalLevelingData;
import daripher.autoleveling.saveddata.WorldLevelingData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.Vec2;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.CommandSource;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import daripher.autoleveling.reloader.DimensionsLevelingSettingsReloader;
import daripher.autoleveling.reloader.EntitiesLevelingSettingsReloader;
import java.util.Random;

@EventBusSubscriber(modid = "autoleveling")
public class MobsLevelingEvents {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        LevelAccessor world = event.getLevel();
        
        // --- FILTRO DE SEGURIDAD PARA MONSTRUOS HOSTILES (Protege animales pacíficos) ---
        if (entity instanceof Monster && entity instanceof LivingEntity livingEntity) {
            MinecraftServer server = livingEntity.getServer();
            if (server == null) return;

            double x = livingEntity.getX();
            double z = livingEntity.getZ();
            double y = livingEntity.getY();

            // --- 1. CÁLCULO DE SECTORES POR DISTANCIA (Pitágoras plano X y Z) ---
            double distancia = Math.sqrt((x * x) + (z * z));
            int levelFromDistance = (int) Math.round(distancia / 500.0);

            // --- 2. DETECTOR DE ZONAS RPG CON TÍTULOS EN ACTIONBAR Y SONIDOS ---
            server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                double pDist = Math.sqrt(serverPlayer.getX() * serverPlayer.getX() + serverPlayer.getZ() * serverPlayer.getZ());
                int playerSector = (int) Math.round(pDist / 1500.0);
                
                String tag = "last_sector_k32";
                if (!serverPlayer.getPersistentData().contains(tag) || serverPlayer.getPersistentData().getInt(tag) != playerSector) {
                    serverPlayer.getPersistentData().putInt(tag, playerSector);
                    
                    // Comando de Actionbar superior compatible
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                        "title " + serverPlayer.getGameProfile().getName() + " times 10 40 10");
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                        "title " + serverPlayer.getGameProfile().getName() + " actionbar [\"\",{\"text\":\"  Has entrado a la \",\"color\":\"gold\"},{\"text\":\"ZONA " + playerSector + "\",\"bold\":true,\"color\":\"dark_red\"},{\"text\":\"  \",\"color\":\"gold\"}]");
                    
                    // Sonido de logro medieval
                    server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), 
                        "playsound minecraft:ui.toast.challenge_complete ambient " + serverPlayer.getGameProfile().getName() + " ~ ~ ~ 1 1");
                }
            });

            // --- 3. SISTEMA DEL DÍA 3 (Frenar dificultad los días 1 y 2) ---
            long gameTime = livingEntity.level().getGameTime();
            long daysPassed = gameTime / 24000;
            int levelFromTime = 0;
            
            if (daysPassed >= 3) {
                levelFromTime = (int) ((daysPassed - 3) * 1); // 1 nivel extra por cada día que pase
                
                // Alerta cinematográfica global al iniciar el Día 3
                server.getPlayerList().getPlayers().forEach(serverPlayer -> {
                    if (daysPassed == 3 && gameTime % 24000  100) {
                nivel_final = 100;
            }

            // --- 6. LECTURA DINÁMICA DE ATRIBUTOS BASE (Soporta cualquier mod de monstruos) ---
            double vidaBase = livingEntity.getMaxHealth();
            double damageBase = 3.0; 
            AttributeInstance damageAttr = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageBase = damageAttr.getBaseValue();
            }

            // --- 7. CÁLCULO DEL MULTIPLICADOR NATIVO (+10% de fuerza por nivel) ---
            double nuevaVida = vidaBase * (1.0 + (nivel_final * 0.1));
            double nuevoDamage = damageBase * (1.0 + (nivel_final * 0.1));

            // --- 8. INYECCIÓN NATIVA POR COMANDOS (100% compatible con tus armas de TACZ) ---
            if (world instanceof ServerLevel _level) {
                // Modificar Vida Máxima
                _level.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                    "execute as @s run attribute @s minecraft:generic.max_health base set " + nuevaVida
                );
                // Modificar Daño de Golpe
                _level.getServer().getCommands().performPrefixedCommand(
                    new CommandSourceStack(CommandSource.NULL, new Vec3(x, y, z), Vec2.ZERO, _level, 4, "", Component.literal(""), _level.getServer(), null).withSuppressedOutput(),
                    "execute as @s run attribute @s minecraft:generic.attack_damage base set " + nuevoDamage
                );
            }

            // --- 9. CURACIÓN INSTANTÁNEA AL NACER ---
            livingEntity.setHealth(livingEntity.getMaxHealth());
        }
    }
}
