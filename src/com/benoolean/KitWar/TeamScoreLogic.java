package com.benoolean.KitWar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Set;

public class TeamScoreLogic implements Listener {

    static class KitTeam {
        String name;
        ChatColor teamColor;

        public KitTeam (String name, ChatColor teamColor) {
            this.teamColor = teamColor;
            this.name = name;
        }
    }

    public static HashMap<String, KitTeam> TeamMap = new HashMap<>();
    public static HashMap<Player, KitTeam> PlayerTeamMap = new HashMap<>();
    public static HashMap<Player, Integer> PlayerScoreMap = new HashMap<>();
    public static HashMap<Player, Integer> PlayerKillCountMap = new HashMap<>();

    public static Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    public static Objective objective = scoreboard.registerNewObjective("Kit War", "dummy", "Player Info");
    public static HashMap<String, Team> ScoreboardTeamMap = new HashMap<>();

    public TeamScoreLogic() {
        KitWar plugin = KitWar.getInstance();

        // team init
        ScoreboardTeamMap.put("Red", scoreboard.registerNewTeam("Red"));
        ScoreboardTeamMap.put("Blue", scoreboard.registerNewTeam("Blue"));
        ScoreboardTeamMap.put("Not Queued", scoreboard.registerNewTeam("Not Queued"));

        ScoreboardTeamMap.get("Red").setPrefix(ChatColor.RED + "" + ChatColor.BOLD + "RED " + ChatColor.RESET);
        ScoreboardTeamMap.get("Blue").setPrefix(ChatColor.AQUA + "" + ChatColor.BOLD + "BLUE " + ChatColor.RESET);
        ScoreboardTeamMap.get("Not Queued").setPrefix(ChatColor.GRAY + "" + ChatColor.BOLD + "Not Queued " + ChatColor.RESET);

        TeamMap.put("Red", new KitTeam("Red", ChatColor.RED));
        TeamMap.put("Blue", new KitTeam("Blue", ChatColor.AQUA));
        TeamMap.put("Not Queued", new KitTeam("Not Queued", ChatColor.GRAY));

        // scoreboard layout
        HashMap<Integer, String> scoreboardLayout = new HashMap<>();
        scoreboardLayout.put(13, new String(new char[15]).replace("\0", "▅"));
        scoreboardLayout.put(12, "      ");
        scoreboardLayout.put(11, "Author: " + ChatColor.GREEN + "benoolean");
        scoreboardLayout.put(10, "    ");
        scoreboardLayout.put(9, "   ");
        scoreboardLayout.put(8, "Kills: " + ChatColor.GREEN);
        scoreboardLayout.put(7, "Deaths: " + ChatColor.GREEN);
        scoreboardLayout.put(6, "Score: " + ChatColor.GREEN);
        scoreboardLayout.put(5, "  ");
        scoreboardLayout.put(4, " ");
        scoreboardLayout.put(3, ChatColor.GREEN + "Support me by following my");
        scoreboardLayout.put(2, ChatColor.GREEN + "Github or visit my website.");
        scoreboardLayout.put(1, ChatColor.GREEN + "More information at:");
        scoreboardLayout.put(0, ChatColor.GOLD + "" + ChatColor.BOLD + "www.benoolean.com");

        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Game Information");


        // setting default empty scoreboard
        Set<Integer> lines = scoreboardLayout.keySet();
        for (int line : lines) {
            Score score = objective.getScore(scoreboardLayout.get(line));
            score.setScore(line);
        }
    }


    /////////////////////////////////
    // 							   //
    //       Choosing Team         //
    //							   //
    /////////////////////////////////

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (player.teleport(new Location(player.getWorld(), 0.5F, 80F, 0.5F))) {
            JoinTeam(player, "Not Queued");
            return;
        }

        player.kickPlayer("Unable to spawn you in correctly!");
    }

    @EventHandler
    public void PlayerMoveEvent(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        KitTeam kitTeam = PlayerTeamMap.get(player);

        if (kitTeam == null) {
            player.kickPlayer("Unable to queue into selected and default teams!");
            return;
        }

        if (kitTeam.name == "Not Queued") {
            player.sendMessage("Please select a team first by /team Blue or /team Red");
            event.setCancelled(true);
        }
    }

    public static void JoinTeam(Player player, String teamName) {
        if (TeamScoreLogic.TeamMap.get(teamName) != null) {
            PlayerTeamMap.put(player, TeamMap.get(teamName));
            PlayerScoreMap.put(player, 0);
            PlayerKillCountMap.put(player, 0);
            player.setScoreboard(TeamScoreLogic.scoreboard);

            Set<String> ScoreboardTeamNames = ScoreboardTeamMap.keySet();
            System.out.print(ScoreboardTeamNames);
            Team team = ScoreboardTeamMap.get(teamName);
            if (team == null) {
                player.sendMessage(ChatColor.RED + "Error when choosing team. Team doesn't exist in HashMap");
                return;
            }

            if (team.hasEntry(player.getName())) {
                player.sendMessage(ChatColor.RED + "You are already on team " + team.getPrefix());
                return;
            }

            team.addEntry(player.getName());
            for (String ScoreboardTeamName : ScoreboardTeamNames) {
                if (!ScoreboardTeamName.equalsIgnoreCase(teamName)) {
                    Team teamNotEnrolled = ScoreboardTeamMap.get(ScoreboardTeamName);
                    if (teamNotEnrolled != null) {
                        teamNotEnrolled.removeEntry(player.getName());
                    }
                }
            }

            player.sendMessage("You are now on team: " + team.getPrefix());

            // init player kill custom scores
            Team playerKillCount =  PlayerRegisterScore(player, "Kills");
            Team playerDeathCount =  PlayerRegisterScore(player, "Deaths");
            Team playerScore =  PlayerRegisterScore(player, "Score");

            // setting player kill custom scores
            playerKillCount.addEntry("Kills: " + ChatColor.GREEN);
            playerKillCount.setSuffix("0");
            playerKillCount.setPrefix("");

            playerDeathCount.addEntry("Deaths: " + ChatColor.GREEN);
            playerDeathCount.setSuffix("0");
            playerDeathCount.setPrefix("");

            playerScore.addEntry("Score: " + ChatColor.GREEN);
            playerScore.setSuffix("0");
            playerScore.setPrefix("");

            // attaching custom scores to player
            player.setScoreboard(TeamScoreLogic.scoreboard);

            return;
        }

        player.sendMessage(ChatColor.RED + "Team name not valid!");
    }

    /////////////////////////////////
    // 							   //
    //         Score Board         //
    //							   //
    /////////////////////////////////

    public static void ScoreboardSet(Player player) {
        PlayerSetScore(player, "Kills");
    }

    /////////////////////////////////
    // 							   //
    //            Misc             //
    //							   //
    /////////////////////////////////

    public static void PlayerSetScore(Player player, String score) {
        String hashedPlayerScoreKey = (player.getName() + score).hashCode() + "";
        Team playerKills =  scoreboard.getTeam(hashedPlayerScoreKey);

        int playerKillCount = (PlayerKillCountMap.get(player) != null) ? PlayerKillCountMap.get(player) : 0;
        playerKills.setSuffix(Integer.toString(playerKillCount + 1));

        PlayerKillCountMap.put(player, playerKillCount + 1);
    }

    public static Team PlayerRegisterScore(Player player, String score) {
        String hashedPlayerScoreKey = (player.getName() + score).hashCode() + "";
        if (scoreboard.getTeam(hashedPlayerScoreKey) == null) {
            return scoreboard.registerNewTeam(hashedPlayerScoreKey);
        }

        return scoreboard.getTeam(hashedPlayerScoreKey);
    }
}
