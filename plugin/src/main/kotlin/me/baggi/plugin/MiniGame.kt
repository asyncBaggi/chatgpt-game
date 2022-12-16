package me.baggi.plugin

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class MiniGame: JavaPlugin(), Listener {
    // Number of players needed to start the game
    private val PLAYER_COUNT = 12

    // Number of points needed to win
    private val WIN_POINTS = 15

    // Map of players and their scores
    private val scores = mutableMapOf<Player, Int>()

    // Flag to track if the game is in progress
    private var gameInProgress = false

    override fun onEnable() {
        // Register the event listener
        Bukkit.getPluginManager().registerEvents(this, this)

        // Start the player check task
        object: BukkitRunnable() {
            override fun run() {
                // If the game is already in progress, do nothing
                if (gameInProgress) return

                // If there are enough players, start the game
                if (Bukkit.getOnlinePlayers().size >= PLAYER_COUNT) {
                    startGame()
                }
            }
        }.runTaskTimer(this, 20, 20) // Run every second
    }

    private fun startGame() {
        gameInProgress = true

        // Teleport all players to the starting location and give them a diamond pickaxe
        val startLocation = Location(Bukkit.getWorld("world"), 0.0, 90.0, 0.0)
        for (player in Bukkit.getOnlinePlayers()) {
            player.teleport(startLocation)
            player.inventory.addItem(ItemStack(Material.DIAMOND_PICKAXE))
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        // If the game is not in progress, do nothing
        if (!gameInProgress) return

        // If the block is not diamond ore, do nothing
        if (event.block.type != Material.DIAMOND_ORE) return

        val player = event.player
        val currentScore = scores.getOrDefault(player, 0) + 1
        scores[player] = currentScore

        // If the player has reached the win points, end the game
        if (currentScore >= WIN_POINTS) {
            endGame(player)
        }
    }

    private fun endGame(winner: Player) {
        gameInProgress = false

        // Announce the winner in chat
        Bukkit.broadcastMessage("${winner.name} has won the game!")

        // Restart the server after a minute
        object: BukkitRunnable() {
            override fun run() {
                Bukkit.shutdown()
            }
        }.runTaskLater(this, 20 * 60) // Run in one minute
    }
}