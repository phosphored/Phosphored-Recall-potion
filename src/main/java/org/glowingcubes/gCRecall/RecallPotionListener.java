package org.glowingcubes.gCRecall;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class RecallPotionListener implements Listener {
    private final JavaPlugin plugin;
    private final HashMap<Player, Integer> countdownTasks = new HashMap<>(); // Отслеживание обратного отсчета
    private final HashMap<Player, Location> playerLocations = new HashMap<>(); // Отслеживание местоположения игроков
    private final HashMap<Player, Boolean> isTeleporting = new HashMap<>(); // Отслеживание, телепортируется ли игрок

    public RecallPotionListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUsePotion(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Проверка, может ли игрок использовать зелье
        if (isTeleporting.getOrDefault(player, false)) {
            player.sendMessage("Вы не можете использовать другое зелье возвращения во время телепортации!");
            return;
        }

        if (event.getAction().toString().contains("RIGHT_CLICK") && item != null && item.getType() == Material.POTION) {
            ItemMeta meta = item.getItemMeta();

            // Проверка, имеет ли зелье нужное имя
            if (meta != null && "§aЗелье Возвращения".equals(meta.getDisplayName())) {
                // Получение местоположения игрока для спавна
                Location spawnLocation = player.getBedSpawnLocation();
                if (spawnLocation != null) {
                    isTeleporting.put(player, true); // Установить статус телепортации
                    startTeleportationCountdown(player, spawnLocation);
                    item.setAmount(item.getAmount() - 1); // Уменьшить количество зелий
                } else {
                    player.sendMessage("У вас нет установленной точки спавна!");
                }
            }
        }
    }

    private void startTeleportationCountdown(Player player, Location spawnLocation) {
        playerLocations.put(player, player.getLocation()); // Сохранить исходное местоположение

        // Запустить обратный отсчет
        new BukkitRunnable() {
            private int countdown = 15;

            @Override
            public void run() {
                // Проверка, переместился ли игрок или онлайн
                if (!player.isOnline() || !player.getLocation().equals(playerLocations.get(player))) {
                    player.sendMessage("Телепортация отменена из-за перемещения!");
                    cancel();
                    isTeleporting.remove(player); // Сбросить статус телепортации
                    return;
                }

                // Спавн частиц и воспроизведение звука
                player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 10);
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);

                if (countdown > 0) {
                    player.sendMessage("Телепортация через " + countdown + " секунд...");
                    countdown--;
                } else {
                    // Телепортация игрока в точку спавна
                    player.teleport(spawnLocation);
                    player.sendMessage("Вы были телепортированы на свою точку спавна!");
                    cancel(); // Завершить задачу
                    isTeleporting.remove(player); // Сбросить статус телепортации
                }
            }
        }.runTaskTimer(plugin, 0, 20); // Запуск каждую секунду (20 тиков = 1 секунда)
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // Отмена отсчета, если игрок переместился
        if (playerLocations.containsKey(player)) {
            player.sendMessage("Телепортация отменена из-за перемещения!");
            playerLocations.remove(player);
            isTeleporting.remove(player); // Сбросить статус телепортации
        }
    }
}
